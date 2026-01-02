/**
 * Metrics Store - Volume, OI, ATP, Imbalance
 * 
 * Stores trading metrics per instrument.
 */

import { create } from 'zustand';
import { VegaTick } from '../types/VegaTick';

interface MetricsData {
    atp: number;
    volumeTraded: number;
    openInterest: number;
    totalBidQty: number;
    totalAskQty: number;
    impliedVolatility?: number;
    upperCircuit?: number;
    lowerCircuit?: number;

    // Computed
    imbalance: number;        // (tbq - tsq) / (tbq + tsq)
    imbalancePercent: number;
    oiChange?: number;        // vs previous

    lastUpdate: number;
}

interface MetricsState {
    // Instrument key -> Metrics data
    data: Map<string, MetricsData>;

    // Previous OI for change tracking
    prevOI: Map<string, number>;

    // Actions
    update: (tick: VegaTick) => void;
    get: (instrumentKey: string) => MetricsData | undefined;
}

export const useMetricsStore = create<MetricsState>((set, get) => ({
    data: new Map(),
    prevOI: new Map(),

    update: (tick: VegaTick) => {
        set(state => {
            const newData = new Map(state.data);
            const newPrevOI = new Map(state.prevOI);

            const m = tick.metrics;
            const totalQty = m.totalBidQty + m.totalAskQty;
            const imbalance = totalQty > 0 ? (m.totalBidQty - m.totalAskQty) / totalQty : 0;

            const prevOI = state.prevOI.get(tick.instrumentKey);
            const oiChange = prevOI !== undefined ? m.openInterest - prevOI : undefined;

            newData.set(tick.instrumentKey, {
                atp: m.atp,
                volumeTraded: m.volumeTraded,
                openInterest: m.openInterest,
                totalBidQty: m.totalBidQty,
                totalAskQty: m.totalAskQty,
                impliedVolatility: m.impliedVolatility,
                upperCircuit: m.upperCircuit,
                lowerCircuit: m.lowerCircuit,
                imbalance,
                imbalancePercent: imbalance * 100,
                oiChange,
                lastUpdate: Date.now()
            });

            // Update prev OI for next comparison
            newPrevOI.set(tick.instrumentKey, m.openInterest);

            return { data: newData, prevOI: newPrevOI };
        });
    },

    get: (instrumentKey: string) => get().data.get(instrumentKey)
}));
