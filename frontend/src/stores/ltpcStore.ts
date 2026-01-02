/**
 * LTPC Store - Last Traded Price & Close
 * 
 * Stores LTPC data per instrument with flash state tracking for price changes.
 * Uses Zustand for optimal re-render performance.
 */

import { create } from 'zustand';
import { VegaTick } from '../types/VegaTick';

interface LTPCData {
    ltp: number;
    ltt: number;
    ltq: number;
    cp: number;
    change: number;
    changePercent: number;
    flash: 'up' | 'down' | null;
    lastUpdate: number;
}

interface LTPCState {
    // Instrument key -> LTPC data
    data: Map<string, LTPCData>;

    // Actions
    update: (tick: VegaTick) => void;
    clearFlash: (instrumentKey: string) => void;
    get: (instrumentKey: string) => LTPCData | undefined;
    getAll: () => Map<string, LTPCData>;
}

export const useLtpcStore = create<LTPCState>((set, get) => ({
    data: new Map(),

    update: (tick: VegaTick) => {
        set(state => {
            const newData = new Map(state.data);
            const prev = newData.get(tick.instrumentKey);
            const prevLtp = prev?.ltp ?? tick.ltpc.cp;

            // Determine flash direction
            let flash: 'up' | 'down' | null = null;
            if (tick.ltpc.ltp > prevLtp) flash = 'up';
            else if (tick.ltpc.ltp < prevLtp) flash = 'down';

            newData.set(tick.instrumentKey, {
                ltp: tick.ltpc.ltp,
                ltt: tick.ltpc.ltt,
                ltq: tick.ltpc.ltq,
                cp: tick.ltpc.cp,
                change: tick.change,
                changePercent: tick.changePercent,
                flash,
                lastUpdate: Date.now()
            });

            return { data: newData };
        });

        // Clear flash after animation
        setTimeout(() => get().clearFlash(tick.instrumentKey), 300);
    },

    clearFlash: (instrumentKey: string) => {
        set(state => {
            const newData = new Map(state.data);
            const entry = newData.get(instrumentKey);
            if (entry) {
                newData.set(instrumentKey, { ...entry, flash: null });
            }
            return { data: newData };
        });
    },

    get: (instrumentKey: string) => get().data.get(instrumentKey),

    getAll: () => get().data
}));
