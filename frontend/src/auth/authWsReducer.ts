import { AuthContextValue } from "../context/AuthContext";

export interface AuthEvent {
    seq: number;
    ts: string;
    type: "TOKEN_READY" | "TOKEN_FAILED" | "TOKEN_PROGRESS" | "SESSION_INVALIDATED" | "HEARTBEAT";
    payload: any;
}

export interface AuthState extends AuthContextValue {
    lastSeq: number;
}

/**
 * Pure reducer for Auth WebSocket events.
 * Handles DELTA updates only.
 */
export function authWsReducer(state: AuthState, event: AuthEvent): AuthState {
    // 1. Monotonicity check
    if (event.seq <= state.lastSeq) {
        console.warn(`[AUTH REDUCER] Ignored out-of-order event. State seq: ${state.lastSeq}, Event seq: ${event.seq}`);
        return state;
    }

    const nextState = { ...state, lastSeq: event.seq };

    switch (event.type) {
        case "TOKEN_READY": {
            const api = event.payload.api;
            // Idempotent add
            const validTokens = state.validTokens.includes(api)
                ? state.validTokens
                : [...state.validTokens, api];

            // Check if primary is ready
            const primaryReady = state.primaryReady || api === "PRIMARY";

            // Check if fully ready
            const fullyReady = validTokens.length >= state.requiredTokens;

            return {
                ...nextState,
                status: "authenticated",
                validTokens,
                primaryReady,
                fullyReady,
                generatedTokens: validTokens.length,
                missingApis: state.missingApis.filter(a => a !== api),
                expiresAt: event.payload.expiresAt || state.expiresAt
            };
        }

        case "TOKEN_FAILED": {
            return {
                ...nextState,
                missingApis: [...state.missingApis, event.payload.api]
            };
        }

        case "TOKEN_PROGRESS": {
            return {
                ...nextState,
                generatedTokens: event.payload.ready,
                requiredTokens: event.payload.total || state.requiredTokens
            };
        }

        case "SESSION_INVALIDATED": {
            return {
                ...nextState,
                status: event.payload.reason === "EXPIRED" ? "expired" : "unauthenticated",
                // 'authenticated' boolean not in AuthContextValue, relies on status
                primaryReady: false,
                fullyReady: false,
                validTokens: [],
                generatedTokens: 0
            };
        }

        case "HEARTBEAT": {
            // Just update sequence
            return nextState;
        }

        default:
            return nextState;
    }
}
