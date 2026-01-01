import { useState, useEffect } from 'react';
import { httpClient } from '../api/httpClient';

export interface SystemHealth {
    database: { status: string; message: string };
    cache: { status: string; message: string };
    api: { status: string; message: string };
    websocket: { status: string; connections: number };
    auth: { primaryReady: boolean; fullyReady: boolean; tokensGenerated: number; tokensRequired: number };
}

export function useSystemHealth(pollIntervalMs = 5000) {
    const [health, setHealth] = useState<SystemHealth | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let mounted = true;

        const fetchHealth = async () => {
            try {
                const res = await httpClient.get<SystemHealth>('/api/system/health');
                if (mounted) {
                    setHealth(res.data);
                    setLoading(false);
                }
            } catch (err) {
                console.error("Failed to fetch system health", err);
                if (mounted) setLoading(false);
            }
        };

        fetchHealth();
        const interval = setInterval(fetchHealth, pollIntervalMs);

        return () => {
            mounted = false;
            clearInterval(interval);
        };
    }, [pollIntervalMs]);

    return { health, loading };
}
