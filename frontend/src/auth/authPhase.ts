/**
 * Auth Phase FSM - Canonical state machine for authentication phases.
 * 
 * This separates AUTHENTICATION from READINESS:
 * - UNAUTHENTICATED: not logged in
 * - INITIALIZING: logged in, primary token not ready
 * - DEGRADED: logged in, primary ready, but some APIs missing
 * - READY: fully operational
 */

export enum AuthPhase {
    UNAUTHENTICATED = 'UNAUTHENTICATED',
    INITIALIZING = 'INITIALIZING',
    DEGRADED = 'DEGRADED',
    READY = 'READY'
}

export interface AuthStatus {
    authenticated?: boolean;
    primaryReady?: boolean;
    fullyReady?: boolean;
    generatedTokens?: number;
    requiredTokens?: number;
    validTokens?: string[];
    missingApis?: string[];
    state?: string;
}

/**
 * Derives the correct AuthPhase from backend auth status.
 * 
 * CRITICAL: fullyReady=false does NOT mean unauthenticated!
 * 
 * Mapping:
 * - authenticated=false → UNAUTHENTICATED
 * - authenticated=true, primaryReady=false → INITIALIZING
 * - authenticated=true, primaryReady=true, fullyReady=false → DEGRADED
 * - authenticated=true, fullyReady=true → READY
 */
export function deriveAuthPhase(status: AuthStatus): AuthPhase {
    // Not authenticated at all
    if (!status.authenticated) {
        // Also check state for backend state machine
        if (status.state === 'PRIMARY_VALIDATED' || status.state === 'AUTH_CONFIRMED') {
            // Backend says authenticated via state
            if (!status.primaryReady) {
                return AuthPhase.INITIALIZING;
            }
            if (!status.fullyReady) {
                return AuthPhase.DEGRADED;
            }
            return AuthPhase.READY;
        }
        return AuthPhase.UNAUTHENTICATED;
    }

    // Authenticated but primary not ready
    if (!status.primaryReady) {
        return AuthPhase.INITIALIZING;
    }

    // Primary ready but not fully ready (some APIs missing)
    if (!status.fullyReady) {
        return AuthPhase.DEGRADED;
    }

    // Fully ready
    return AuthPhase.READY;
}

/**
 * Get color for auth phase (Bloomberg-style)
 */
export function getAuthPhaseColor(phase: AuthPhase): string {
    switch (phase) {
        case AuthPhase.UNAUTHENTICATED:
            return 'text-red-500';
        case AuthPhase.INITIALIZING:
            return 'text-amber-500';
        case AuthPhase.DEGRADED:
            return 'text-amber-500';
        case AuthPhase.READY:
            return 'text-green-500';
    }
}

/**
 * Get background color for auth phase
 */
export function getAuthPhaseBgColor(phase: AuthPhase): string {
    switch (phase) {
        case AuthPhase.UNAUTHENTICATED:
            return 'bg-red-500/20';
        case AuthPhase.INITIALIZING:
            return 'bg-amber-500/20';
        case AuthPhase.DEGRADED:
            return 'bg-amber-500/20';
        case AuthPhase.READY:
            return 'bg-green-500/20';
    }
}
