import React from "react";
import CircularCountdown from "./CircularCountdown";

interface TokenStatus {
    name: string;
    remainingSeconds: number;
    totalSeconds: number;
    state: string;
}

interface MultiTokenCountdownProps {
    tokens: TokenStatus[];
    columns?: number; // grid columns
}

const MultiTokenCountdown: React.FC<MultiTokenCountdownProps> = ({
    tokens,
    columns = 3
}) => {
    return (
        <div
            style={{
                display: "grid",
                gridTemplateColumns: `repeat(${columns}, minmax(200px, 1fr))`,
                gap: "20px",
                padding: "20px",
                backgroundColor: "#111",
                color: "#fff",
            }}
        >
            {tokens.map((token) => (
                <div
                    key={token.name}
                    style={{
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "center",
                        justifyContent: "center",
                        backgroundColor: "#1e1e1e",
                        padding: "15px",
                        borderRadius: "12px",
                        boxShadow: "0 0 15px rgba(0,0,0,0.3)",
                        border: "1px solid #333"
                    }}
                >
                    <CircularCountdown
                        size={100}
                        strokeWidth={8}
                        totalSeconds={token.totalSeconds}
                        remainingSeconds={token.remainingSeconds}
                        status={token.state}
                    />
                    <div style={{ marginTop: 15, fontFamily: "monospace", textAlign: "center" }}>
                        <strong style={{ fontSize: "1.1em", color: "#ddd" }}>{token.name}</strong>
                        <br />
                        <div style={{ marginTop: 5, fontSize: "0.9em" }}>
                            {token.state === "RECONNECTING" && <span style={{ color: "#ff9800" }}>Refreshing...</span>}
                            {token.state === "EXPIRED" && <span style={{ color: "#f44336" }}>Expired</span>}
                            {token.state === "AUTH_CONFIRMED" && <span style={{ color: "#4caf50" }}>Active</span>}
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default MultiTokenCountdown;
