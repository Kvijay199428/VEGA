import { useEffect, useState, useRef } from "react";
import { connectAuthWS, subscribeAuthEvent } from "./authWebSocket";
import { amLeader } from "./leaderElection";
import { AuthContextValue, INITIAL_AUTH_CONTEXT } from "../context/AuthContext";
import { authWsReducer, AuthState } from "../auth/authWsReducer";

/**
 * Derives the correct frontend status from backend data.
 */
function deriveStatus(data: any): AuthContextValue["status"] {
    // Check explicit authenticated boolean first (legacy/http support)
    if (data.authenticated === true || data.primaryReady === true) {
        return "authenticated";
    }

    // Check state machine states
    if (data.state === "AUTH_CONFIRMED" || data.state === "PRIMARY_VALIDATED") {
        return "authenticated";
    }

    if (data.state === "EXPIRED") {
        return "expired";
    }

    if (data.state === "ERROR") {
        return "error";
    }

    if (!data.state || data.state === "INITIALIZING") {
        return "loading";
    }

    return "unauthenticated";
}

// Generate session ID once per tab load
const SESSION_ID = crypto.randomUUID();

export function useAuthStatus(): AuthContextValue {
    // We extend AuthContextValue with lastSeq for reducer
    const [state, setState] = useState<AuthState>({
        ...INITIAL_AUTH_CONTEXT,
        lastSeq: 0
    });

    const stateRef = useRef(state);
    stateRef.current = state; // Keep ref updated for callbacks

    useEffect(() => {
        // Helper to map backend HTTP data to AuthState
        const mapToAuthState = (data: any): AuthState => ({
            status: deriveStatus(data),
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
            connected: true,
            lastSeq: stateRef.current.lastSeq // Keep sequence
        });

        // 1. HTTP Bootstrap (Authoritative)
        async function bootstrapFromHttp() {
            try {
                console.log('[useAuthStatus] Bootstrapping initial state via HTTP...');
                const response = await fetch('/api/auth/status', { credentials: 'include' });
                if (response.ok) {
                    const data = await response.json();
                    console.log('[useAuthStatus] HTTP bootstrap received:', data);
                    setState(mapToAuthState(data));
                }
            } catch (err) {
                console.warn('[useAuthStatus] HTTP bootstrap failed:', err);
            }
        }

        bootstrapFromHttp();

        // 2. Connect WebSocket with strict session binding
        connectAuthWS(SESSION_ID);

        // 3. Subscribe to DELTA events
        const unsubscribe = subscribeAuthEvent((event: any) => {
            if (!event || !event.type) return;

            console.log('[useAuthStatus] WS Event:', event.type, event.seq, event.payload);

            // Apply reducer
            setState(prevState => authWsReducer(prevState, event));
        });

        return unsubscribe;
    }, []);

    return state;
}
