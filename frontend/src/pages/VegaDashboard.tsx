import React, { useEffect, useState } from "react";
import { useAuthStatus } from "../hooks/useAuthStatus";
import MultiTokenCountdown from "../components/MultiTokenCountdown";

interface TokenStatus {
    name: string;
    remainingSeconds: number;
    totalSeconds: number;
    state: string;
}

const VegaDashboard: React.FC = () => {
    const status = useAuthStatus();
    const [tokens, setTokens] = useState<TokenStatus[]>([]);

    useEffect(() => {
        if (status) {
            // Map valid tokens to TokenStatus objects
            // If validTokens is empty but we have generated count, mock some names or show "Master"
            const tokenNames = status.validTokens && status.validTokens.length > 0
                ? status.validTokens
                : (status.status === 'authenticated' ? ["PRIMARY_SESSION"] : []);

            const updatedTokens: TokenStatus[] = tokenNames.map((t: string) => ({
                name: t,
                remainingSeconds: status.remainingSeconds,
                totalSeconds: 86400, // Assuming 24h for now, or fetch from config
                state: status.state,
            }));
            setTokens(updatedTokens);
        }
    }, [status]);

    if (!status) {
        return (
            <div style={{
                backgroundColor: "#111",
                minHeight: "100vh",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                color: "#fff",
                fontFamily: "monospace"
            }}>
                <p>Connecting to Authentication Server...</p>
            </div>
        );
    }

    return (
        <div style={{ backgroundColor: "#111", minHeight: "100vh", padding: "20px", color: "white" }}>
            <header style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                marginBottom: "30px",
                borderBottom: "1px solid #333",
                paddingBottom: "15px"
            }}>
                <h1 style={{ margin: 0, fontFamily: "Orbitron, sans-serif" }}>VEGA MANAGER <span style={{ color: "cyan", fontSize: "0.5em" }}>v2.0</span></h1>
                <div style={{ textAlign: "right", fontFamily: "monospace", fontSize: "0.9em", color: "#888" }}>
                    <div>System Status: <span style={{ color: status.fullyReady ? "#4caf50" : "#ff9800" }}>{status.state}</span></div>
                    <div>Tokens: {status.generatedTokens} / {status.requiredTokens}</div>
                </div>
            </header>

            {tokens.length === 0 ? (
                <div style={{ textAlign: "center", marginTop: "50px", color: "#666" }}>
                    <h2>No Active Sessions</h2>
                    <p>Please initiate login from the Automation Panel.</p>
                </div>
            ) : (
                <MultiTokenCountdown tokens={tokens} columns={4} />
            )}

            {/* Debug Info for Verification */}
            {/* 
      <pre style={{ marginTop: 50, color: "#333", fontSize: "0.8em" }}>
          {JSON.stringify(status, null, 2)}
      </pre> 
      */}
        </div>
    );
};

export default VegaDashboard;
