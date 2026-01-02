/**
 * OHLC Store - Open/High/Low/Close Data
 * 
 * Stores daily and intraday OHLC data per instrument.
 */

import { create } from 'zustand';
import { VegaTick, OHLC } from '../types/VegaTick';

interface OHLCData {
    daily: OHLC;
    intraday?: OHLC;
    lastUpdate: number;
}

interface OHLCState {
    // Instrument key -> OHLC data
    data: Map<string, OHLCData>;

    // Historical candles for charting (instrument -> candle array)
    history: Map<string, OHLC[]>;

    // Actions
    update: (tick: VegaTick) => void;
    get: (instrumentKey: string) => OHLCData | undefined;
    getHistory: (instrumentKey: string) => OHLC[];
    appendCandle: (instrumentKey: string, candle: OHLC) => void;
}

export const useOhlcStore = create<OHLCState>((set, get) => ({
    data: new Map(),
    history: new Map(),

    update: (tick: VegaTick) => {
        if (!tick.ohlc.daily) return;

        set(state => {
            const newData = new Map(state.data);

            newData.set(tick.instrumentKey, {
                daily: tick.ohlc.daily!,
                intraday: tick.ohlc.intraday,
                lastUpdate: Date.now()
            });

            return { data: newData };
        });
    },

    get: (instrumentKey: string) => get().data.get(instrumentKey),

    getHistory: (instrumentKey: string) => get().history.get(instrumentKey) ?? [],

    appendCandle: (instrumentKey: string, candle: OHLC) => {
        set(state => {
            const newHistory = new Map(state.history);
            const existing = newHistory.get(instrumentKey) ?? [];

            // Update last candle if same timestamp, otherwise append
            if (existing.length > 0 && existing[existing.length - 1].ts === candle.ts) {
                existing[existing.length - 1] = candle;
            } else {
                existing.push(candle);
                // Keep last 500 candles
                if (existing.length > 500) {
                    existing.shift();
                }
            }

            newHistory.set(instrumentKey, existing);
            return { history: newHistory };
        });
    }
}));
