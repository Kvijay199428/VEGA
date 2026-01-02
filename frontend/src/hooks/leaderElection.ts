
const channel = new BroadcastChannel("vega-ws-control");
let isLeader = false;

type Message = { type: "ELECTION" } | { type: "LEADER" };

/**
 * Initiates the leader election process.
 * If no one claims leadership within 300ms, this tab becomes the leader.
 */
export function electLeader() {
    // Announce we are holding an election
    channel.postMessage({ type: "ELECTION" });

    channel.onmessage = (e) => {
        const msg = e.data as Message;

        if (msg.type === "LEADER") {
            // Someone else is leader, so I am not
            console.log("[LeaderElection] Leader exists. I am a follower.");
            isLeader = false;
        } else if (msg.type === "ELECTION") {
            // Someone else is asking for an election.
            // If I am already the leader, I assert my dominance.
            if (isLeader) {
                channel.postMessage({ type: "LEADER" });
            }
        }
    };

    // Wait to see if a leader responds
    setTimeout(() => {
        if (!isLeader) {
            console.log("[LeaderElection] No leader found. I am taking leadership.");
            isLeader = true;
            channel.postMessage({ type: "LEADER" });
        }
    }, 300);

    // Also handle window unload to yield leadership (optional refined logic could go here)
    window.addEventListener("beforeunload", () => {
        // We don't explicitly broadcast "I quit", but the silence will trigger new election 
        // when other tabs eventually notice socket death or keep-alive (if implemented).
        // For now, simple election on load is sufficient.
    });
}

/**
 * Returns true if this tab is the currently elected leader.
 */
export function amLeader() {
    return isLeader;
}

// Start election on module load (or call explicitly in App initialization)
electLeader();
