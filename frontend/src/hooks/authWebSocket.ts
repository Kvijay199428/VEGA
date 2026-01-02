import { amLeader } from "./leaderElection";
import { AuthEvent } from "../auth/authWsReducer";

let socket: WebSocket | null = null;
let listeners: ((event: AuthEvent) => void)[] = [];
let reconnectTimer: number | null = null;
let currentSessionId: string | null = null;

// internal broadcast channel for sync between tabs
const syncChannel = new BroadcastChannel("vega-state-sync");

// Handle incoming sync messages from the Leader (for followers)
syncChannel.onmessage = (e) => {
    if (!amLeader()) {
        const { type, payload } = e.data;
        if (type === "AUTH_EVENT") {
            listeners.forEach(l => l(payload));
        }
    }
};

export function connectAuthWS(sessionId: string) {
    if (!sessionId) {
        console.error("[AUTH WS] Cannot connect: No sessionId provided");
        return;
    }

    currentSessionId = sessionId;

    if (!amLeader()) {
        console.log("[AUTH WS] I am a follower. Waiting for updates via BroadcastChannel.");
        return;
    }

    if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) return;

    // Strict session binding
    const wsUrl = `ws://localhost:28020/ws/auth/status?sessionId=${sessionId}`;
    console.log("[AUTH WS] I am LEADER. Connecting to", wsUrl);

    socket = new WebSocket(wsUrl);

    socket.onopen = () => {
        console.log("[AUTH WS] Connected");
    };

    socket.onmessage = (e) => {
        try {
            const event: AuthEvent = JSON.parse(e.data);

            // 1. Notify local listeners (my own UI)
            listeners.forEach(l => l(event));

            // 2. Relay to followers
            syncChannel.postMessage({ type: "AUTH_EVENT", payload: event });

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
                if (currentSessionId) {
                    connectAuthWS(currentSessionId);
                }
            }, 2000);
        }
    };
}

export function subscribeAuthEvent(cb: (event: AuthEvent) => void) {
    listeners.push(cb);
    return () => {
        listeners = listeners.filter(l => l !== cb);
    };
}

// Periodically check if we became leader (e.g. if leader died)
setInterval(() => {
    if (amLeader() && !socket && currentSessionId) {
        connectAuthWS(currentSessionId);
    }
}, 1000);

