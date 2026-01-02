import { useContext, useState } from 'react';
import { executeCommand, parseCommand } from '../../hooks/commandRouter';
import { AuthContext } from '../../context/AuthContext';

// No props needed now, consumes Context directly
export default function VegaStatusBar() {
    // Guaranteed non-null context
    const status = useContext(AuthContext);

    // Default GREEN
    let bgColor = "#2e7d32";
    let faultReason = "SYSTEM NORMAL";

    // "Safe" defaults 
    const stateText = status.state;

    if (status.status === "loading") {
        bgColor = "#f0c808"; // AMBER - Initializing
        faultReason = "CONNECTING...";
    } else if (status.status === "unauthenticated") {
        bgColor = "#f0c808"; // AMBER - Waiting
    } else if (status.status === "expired" || status.status === "error") {
        bgColor = "#c62828"; // RED - Error
        faultReason = "CONNECTION LOST";
    } else if (status.state === "COOLDOWN") {
        bgColor = "#c62828"; // RED - Cooldown
        faultReason = "RATE LIMIT";
    } else if (status.status === "authenticated") {
        // Warning State (Bloomberg AMBER)
        // If tokens are valid but close to expiry (< 5 mins), show warning 
        // DO NOT REDIRECT OR LOGOUT. Just visual warning.
        if (status.remainingSeconds !== null && status.remainingSeconds < 300 && status.remainingSeconds > 0) {
            bgColor = "#ef6c00"; // Deep Orange / Amber
            faultReason = `EXPIRING (${Math.floor(status.remainingSeconds / 60)}m)`;
        } else {
            bgColor = "#2e7d32"; // GREEN
        }
    }

    // Command Bar State
    const [command, setCommand] = useState("");

    // Leader indicator
    const isLeader = status.isLeader;

    return (
        <div style={{
            height: 28,
            backgroundColor: bgColor,
            color: "#fff",
            display: "flex",
            alignItems: "center",
            padding: "0 12px",
            fontFamily: "monospace",
            fontSize: 13,
            width: "100%",
            boxSizing: "border-box"
        }}>
            <span style={{ fontWeight: "bold" }}>VEGA</span>
            <span style={{ marginLeft: 12, opacity: 0.8 }}>
                {isLeader ? "[LEADER]" : "[FOLLOWER]"}
            </span>
            <span style={{ marginLeft: 12 }}>
                {stateText}
            </span>

            {/* Command Input Area (Center-Right) */}
            <input
                style={{
                    marginLeft: "24px",
                    background: "rgba(0,0,0,0.3)",
                    color: "#fff",
                    fontFamily: "monospace",
                    border: "none",
                    padding: "2px 8px",
                    outline: "none",
                    flex: 1, // Take available space
                    maxWidth: "300px"
                }}
                id="command-input"
                placeholder="GO <SYMBOL>"
                value={command}
                onChange={(e) => setCommand(e.target.value)}
                onKeyDown={(e) => {
                    if (e.key === "Enter") {
                        executeCommand(parseCommand(command));
                        setCommand("");
                    }
                }}
            />

            <span style={{ marginLeft: "auto" }}>
                {faultReason}
            </span>
        </div>
    );
}
