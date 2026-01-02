/**
 * Greeks Store - Option Greeks Data
 * 
 * Stores delta, gamma, theta, vega, rho for options.
 * Tracks previous values for comparison.
 */

import { create } from 'zustand';
import { VegaTick, OptionGreeks } from '../types/VegaTick';

interface GreeksData extends OptionGreeks {
    prevDelta?: number;
    prevGamma?: number;
    prevTheta?: number;
    lastUpdate: number;
}

interface GreeksState {
    // Instrument key -> Greeks data
    data: Map<string, GreeksData>;

    // Frozen instruments
    frozen: Set<string>;

    // Actions
    update: (tick: VegaTick) => void;
    freeze: (instrumentKey: string) => void;
    resume: (instrumentKey: string) => void;
    isFrozen: (instrumentKey: string) => boolean;
    get: (instrumentKey: string) => GreeksData | undefined;
}

export const useGreeksStore = create<GreeksState>((set, get) => ({
    data: new Map(),
    frozen: new Set(),

    update: (tick: VegaTick) => {
        // Skip if no Greeks data or frozen
        if (!tick.greeks || get().frozen.has(tick.instrumentKey)) {
            return;
        }

        set(state => {
            const newData = new Map(state.data);
            const prev = newData.get(tick.instrumentKey);

            newData.set(tick.instrumentKey, {
                ...tick.greeks!,
                prevDelta: prev?.delta,
                prevGamma: prev?.gamma,
                prevTheta: prev?.theta,
                lastUpdate: Date.now()
            });

            return { data: newData };
        });
    },

    freeze: (instrumentKey: string) => {
        set(state => {
            const newFrozen = new Set(state.frozen);
            newFrozen.add(instrumentKey);
            return { frozen: newFrozen };
        });
    },

    resume: (instrumentKey: string) => {
        set(state => {
            const newFrozen = new Set(state.frozen);
            newFrozen.delete(instrumentKey);
            return { frozen: newFrozen };
        });
    },

    isFrozen: (instrumentKey: string) => get().frozen.has(instrumentKey),

    get: (instrumentKey: string) => get().data.get(instrumentKey)
}));
