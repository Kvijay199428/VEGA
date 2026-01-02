import { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { deriveAuthPhase, AuthPhase } from '../auth/authPhase';

// Bloomberg color canon
export const TERMINAL_COLORS = {
    GREEN: "#00C176",
    AMBER: "#F0C808",
    RED: "#C62828",
};

export type TerminalFault = {
    level: "NONE" | "WARN" | "CRITICAL";
    message: string;
    latched: boolean;
};

export function useTerminalStatus() {
    const status = useContext(AuthContext);

    // Derive Auth Phase
    const authPhase = deriveAuthPhase({
        authenticated: status.status === 'authenticated',
        primaryReady: status.primaryReady,
        fullyReady: status.fullyReady,
        state: status.state
    });

    const isLeader = status.isLeader;

    // Derive Fault
    const deriveFault = (): TerminalFault => {
        if (status.state === "COOLDOWN") {
            return { level: "CRITICAL", message: "RATE LIMIT", latched: true };
        }
        if (status.status === "expired" || status.status === "error") {
            return { level: "CRITICAL", message: "CONNECTION LOST", latched: true };
        }
        if (status.status === 'unauthenticated' || authPhase === AuthPhase.UNAUTHENTICATED) {
            // Basic state, maybe not a "fault" if just starting app, but for terminal it is RED
            return { level: "CRITICAL", message: "NOT LOGGED IN", latched: false };
        }
        if (status.status === 'authenticated' && !status.fullyReady) {
            return {
                level: "WARN",
                message: `DEGRADED (${status.missingApis?.length || 0} API missing)`,
                latched: false
            };
        }
        if (status.remainingSeconds && status.remainingSeconds < 300) {
            return {
                level: "WARN",
                message: `EXPIRING (${Math.floor(status.remainingSeconds / 60)}m)`,
                latched: false
            };
        }
        return { level: "NONE", message: "SYSTEM NORMAL", latched: false };
    };

    const fault = deriveFault();

    return {
        authPhase,
        isLeader,
        fault,
        status // return raw status if needed
    };
}
