import { createContext } from 'react';

export interface AuthContextValue {
    // State machine status
    status: "loading" | "authenticated" | "unauthenticated" | "expired" | "error";

    // Backend payload fields
    state: string;
    primaryReady: boolean;
    fullyReady: boolean;
    generatedTokens: number;
    requiredTokens: number;
    validTokens: string[];
    missingApis: string[];
    cooldownActive: boolean;
    remainingSeconds: number;
    expiresAt: number | null;

    // Tab leadership
    isLeader: boolean;
    connected: boolean;
}

export const INITIAL_AUTH_CONTEXT: AuthContextValue = {
    status: "loading",
    state: "INITIALIZING",
    primaryReady: false,
    fullyReady: false,
    generatedTokens: 0,
    requiredTokens: 0,
    validTokens: [],
    missingApis: [],
    cooldownActive: false,
    remainingSeconds: 0,
    expiresAt: null,
    isLeader: false,
    connected: false
};

export const AuthContext = createContext<AuthContextValue>(INITIAL_AUTH_CONTEXT);
