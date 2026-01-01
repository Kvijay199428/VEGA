import { useState, useEffect } from 'react';
import { httpClient } from '../api/httpClient';

export interface PortfolioKpis {
    netPnl: number;
    netPnlPercent: number;
    grossExposure: number;
    netExposure: number;
    marginUsed: number;
    marginAvailable: number;
    marginUtilization: number;
    openPositions: number;
    dayTrades: number;
}

export function usePortfolioKpis(pollIntervalMs = 3000) {
    const [kpis, setKpis] = useState<PortfolioKpis | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let mounted = true;

        const fetchKpis = async () => {
            try {
                const res = await httpClient.get<PortfolioKpis>('/api/portfolio/kpis');
                if (mounted) {
                    setKpis(res.data);
                    setLoading(false);
                }
            } catch (err) {
                console.error("Failed to fetch portfolio KPIs", err);
                if (mounted) setLoading(false);
            }
        };

        fetchKpis();
        const interval = setInterval(fetchKpis, pollIntervalMs);

        return () => {
            mounted = false;
            clearInterval(interval);
        };
    }, [pollIntervalMs]);

    return { kpis, loading };
}
