import { amLeader } from "./leaderElection";

let socket: WebSocket | null = null;
let listeners: ((data: any) => void)[] = [];
let reconnectTimer: number | null = null;

// internal broadcast channel for sync between tabs
const syncChannel = new BroadcastChannel("vega-state-sync");

// Ensure this URL matches your backend config
const WS_URL = "ws://localhost:28020/ws/auth/status"; // Port 28020 for backend

// Handle incoming sync messages from the Leader (for followers)
syncChannel.onmessage = (e) => {
    if (!amLeader()) {
        const { type, payload } = e.data;
        if (type === "AUTH_UPDATE") {
            listeners.forEach(l => l(payload));
        }
    }
};

export function connectAuthWS() {
    if (!amLeader()) {
        console.log("[AUTH WS] I am a follower. Waiting for updates via BroadcastChannel.");
        return;
    }

    if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) return;

    console.log("[AUTH WS] I am LEADER. Connecting to", WS_URL);
    socket = new WebSocket(WS_URL);

    socket.onopen = () => {
        console.log("[AUTH WS] Connected");
    };

    socket.onmessage = (e) => {
        try {
            const data = JSON.parse(e.data);
            // 1. Notify local listeners (my own UI)
            listeners.forEach(l => l(data));

            // 2. Relay to followers
            syncChannel.postMessage({ type: "AUTH_UPDATE", payload: data });

        } catch (err) {
            console.error("[AUTH WS] Parse error", err);
        }
    };

    socket.onerror = (e) => {
        console.error("[AUTH WS] Error", e);
    };

    socket.onclose = (e) => {
        console.warn("[AUTH WS] Closed", e.code, e.reason);
        socket = null;

        if (!reconnectTimer && amLeader()) {
            reconnectTimer = window.setTimeout(() => {
                reconnectTimer = null;
                console.log("[AUTH WS] Attempting reconnect...");
                connectAuthWS();
            }, 2000);
        }
    };
}

export function subscribeAuthStatus(cb: (data: any) => void) {
    listeners.push(cb);
    // If we are a follower and subscribe, we might want to ask for the latest state?
    // For now, we wait for the next push.
    return () => {
        listeners = listeners.filter(l => l !== cb);
    };
}

// Periodically check if we became leader (e.g. if leader died)
setInterval(() => {
    if (amLeader() && !socket) {
        connectAuthWS();
    }
}, 1000);

// Initial kick
connectAuthWS();
