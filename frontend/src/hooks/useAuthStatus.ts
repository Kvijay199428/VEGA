import { useEffect, useState } from "react";
import { connectAuthWS, subscribeAuthStatus } from "./authWebSocket";
import { amLeader } from "./leaderElection";
import { AuthContextValue, INITIAL_AUTH_CONTEXT } from "../context/AuthContext";

export function useAuthStatus(): AuthContextValue {
    const [state, setState] = useState<AuthContextValue>(INITIAL_AUTH_CONTEXT);

    useEffect(() => {
        // Ensure connection (singleton logic in authWebSocket)
        connectAuthWS();

        // Subscribe to updates (either direct WS or BroadcastChannel)
        const unsubscribe = subscribeAuthStatus((data: any) => {
            if (!data) return;

            // Map backend raw state to high-level lifecycle status
            let frontendStatus: AuthContextValue["status"] = "unauthenticated";

            if (data.state === "AUTH_CONFIRMED" || data.state === "PRIMARY_VALIDATED") frontendStatus = "authenticated";
            else if (data.state === "EXPIRED") frontendStatus = "expired";
            else if (data.state === "ERROR") frontendStatus = "error";
            else if (!data.state || data.state === "INITIALIZING") frontendStatus = "loading";

            setState({
                status: frontendStatus,
                state: data.state || "INITIALIZING",
                primaryReady: data.primaryReady || false,
                fullyReady: data.fullyReady || false,
                generatedTokens: data.generatedTokens || 0,
                requiredTokens: data.requiredTokens || 0,
                validTokens: data.validTokens || [],
                missingApis: data.missingApis || [],
                cooldownActive: data.cooldownActive || false,
                remainingSeconds: data.remainingSeconds || 0,
                expiresAt: data.expiresAt || null,
                isLeader: amLeader(),
                connected: true
            });
        });

        return unsubscribe;
    }, []);

    return state;
}
