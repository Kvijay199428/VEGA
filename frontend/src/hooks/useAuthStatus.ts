import { useState, useEffect } from 'react';
import { httpClient } from '../api/httpClient';

export interface AuthStatusResponse {
    state: string;
    authenticated: boolean;
    primaryReady: boolean;
    fullyReady: boolean;
    generatedTokens: number;
    requiredTokens: number;
    validTokens: string[];
    missingApis: string[];
    cooldownActive?: boolean;
    remainingSeconds?: number;
    // Dashboard compatibility fields
    dbLocked?: boolean;
    inProgress?: boolean;
    currentApi?: string;
    pendingInCache?: number;
    recoveryInProgress?: boolean;
}

export function useAuthStatus(pollIntervalMs = 3000) {
    const [status, setStatus] = useState<AuthStatusResponse | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let mounted = true;

        const fetchStatus = async () => {
            try {
                const res = await httpClient.get<AuthStatusResponse>('/api/auth/status');
                if (mounted) {
                    setStatus(res.data);
                    setLoading(false);
                }
            } catch (err) {
                console.error("Failed to fetch auth status", err);
                if (mounted) setLoading(false);
            }
        };

        fetchStatus();
        const interval = setInterval(fetchStatus, pollIntervalMs);

        return () => {
            mounted = false;
            clearInterval(interval);
        };
    }, [pollIntervalMs]);

    return { status, loading };
}
