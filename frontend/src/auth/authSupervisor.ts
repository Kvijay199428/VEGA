/**
 * Auth Supervisor - Singleton, Process-Level Auth State
 * 
 * This runs ONCE per browser (not per component mount).
 * It is the single source of truth for authentication.
 * 
 * Immune to:
 * - StrictMode double-invocation
 * - Component unmount/remount
 * - HMR
 * - Multi-tab conflicts
 */

export type AuthState = {
    status: 'booting' | 'authenticated' | 'unauthenticated';
    userId?: string;
    userName?: string;
    expiresAt?: number;
    primaryReady?: boolean;
    generatedTokens?: number;
    requiredTokens?: number;
};

let authPromise: Promise<AuthState> | null = null;
let currentState: AuthState = { status: 'booting' };

const AUTH_CHANNEL_NAME = 'vega-auth-supervisor';
let channel: BroadcastChannel | null = null;

// Initialize channel safely
function getChannel(): BroadcastChannel {
    if (!channel) {
        channel = new BroadcastChannel(AUTH_CHANNEL_NAME);
        channel.onmessage = (e) => {
            if (e.data?.type === 'AUTH_STATE') {
                currentState = e.data.state;
                console.log('[AuthSupervisor] Received state from another tab:', currentState.status);
            }
        };
    }
    return channel;
}

/**
 * Boot auth - runs exactly ONCE per browser session.
 * Subsequent calls return the same cached promise.
 */
export function bootAuth(): Promise<AuthState> {
    if (authPromise) {
        console.log('[AuthSupervisor] Returning cached auth promise');
        return authPromise;
    }

    console.log('[AuthSupervisor] Booting auth (first call)...');

    authPromise = fetch('/api/auth/session', { credentials: 'include' })
        .then(res => {
            if (!res.ok) {
                throw new Error(`Session check failed: ${res.status}`);
            }
            return res.json();
        })
        .then(session => {
            console.log('[AuthSupervisor] Session response:', session);

            // Map session response to auth state
            if (session.status === 'SUCCESS' && session.primaryReady) {
                currentState = {
                    status: 'authenticated',
                    userId: session.user?.email || 'unknown',
                    userName: session.user?.name || 'Vega User',
                    expiresAt: session.expiresAt,
                    primaryReady: session.primaryReady,
                    generatedTokens: session.generatedTokens,
                    requiredTokens: session.configuredApis?.length || 6,
                };
                console.log('[AuthSupervisor] ✓ Authenticated:', currentState.userName);
            } else {
                currentState = { status: 'unauthenticated' };
                console.log('[AuthSupervisor] ✗ Not authenticated');
            }

            // Broadcast to other tabs
            getChannel().postMessage({ type: 'AUTH_STATE', state: currentState });

            return currentState;
        })
        .catch(err => {
            console.error('[AuthSupervisor] Session check error:', err);
            currentState = { status: 'unauthenticated' };
            return currentState;
        });

    return authPromise;
}

/**
 * Get current auth state (synchronous).
 * Returns 'booting' if bootAuth() hasn't resolved yet.
 */
export function getAuthState(): AuthState {
    return currentState;
}

/**
 * Force re-check session (e.g., after login).
 */
export function invalidateAuth(): void {
    console.log('[AuthSupervisor] Invalidating auth cache');
    authPromise = null;
    currentState = { status: 'booting' };
}

/**
 * Subscribe to auth state changes from other tabs.
 */
export function subscribeToAuthChanges(callback: (state: AuthState) => void): () => void {
    const ch = getChannel();
    const handler = (e: MessageEvent) => {
        if (e.data?.type === 'AUTH_STATE') {
            callback(e.data.state);
        }
    };
    ch.addEventListener('message', handler);
    return () => ch.removeEventListener('message', handler);
}
