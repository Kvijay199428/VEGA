/**
 * Order Book Store - L2/L3 Depth Data
 * 
 * Stores order book snapshots per instrument with freeze/resume capability.
 * Supports depth level selection (5, 10, 20, full).
 */

import { create } from 'zustand';
import { VegaTick, PriceLevel } from '../types/VegaTick';

interface OrderBookData {
    bids: PriceLevel[];
    asks: PriceLevel[];
    depth: number;
    totalBidQty: number;
    totalAskQty: number;
    spread: number;
    spreadPercent: number;
    lastUpdate: number;
}

interface OrderBookState {
    // Instrument key -> Order book data
    data: Map<string, OrderBookData>;

    // Frozen instruments (won't update until resumed)
    frozen: Set<string>;

    // Display depth preference per instrument
    displayDepth: Map<string, number>;

    // Actions
    update: (tick: VegaTick) => void;
    freeze: (instrumentKey: string) => void;
    resume: (instrumentKey: string) => void;
    isFrozen: (instrumentKey: string) => boolean;
    setDisplayDepth: (instrumentKey: string, depth: number) => void;
    getDisplayDepth: (instrumentKey: string) => number;
    get: (instrumentKey: string) => OrderBookData | undefined;
    getSliced: (instrumentKey: string) => OrderBookData | undefined;
}

export const useOrderBookStore = create<OrderBookState>((set, get) => ({
    data: new Map(),
    frozen: new Set(),
    displayDepth: new Map(),

    update: (tick: VegaTick) => {
        const state = get();

        // Skip update if frozen
        if (state.frozen.has(tick.instrumentKey)) {
            return;
        }

        set(state => {
            const newData = new Map(state.data);

            const bestBid = tick.orderBook.bids[0]?.price ?? 0;
            const bestAsk = tick.orderBook.asks[0]?.price ?? 0;
            const spread = bestAsk - bestBid;
            const mid = (bestBid + bestAsk) / 2;

            newData.set(tick.instrumentKey, {
                bids: tick.orderBook.bids,
                asks: tick.orderBook.asks,
                depth: tick.orderBook.depth,
                totalBidQty: tick.metrics.totalBidQty,
                totalAskQty: tick.metrics.totalAskQty,
                spread,
                spreadPercent: mid > 0 ? (spread / mid) * 100 : 0,
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

    setDisplayDepth: (instrumentKey: string, depth: number) => {
        set(state => {
            const newDepth = new Map(state.displayDepth);
            newDepth.set(instrumentKey, depth);
            return { displayDepth: newDepth };
        });
    },

    getDisplayDepth: (instrumentKey: string) => get().displayDepth.get(instrumentKey) ?? 5,

    get: (instrumentKey: string) => get().data.get(instrumentKey),

    getSliced: (instrumentKey: string) => {
        const state = get();
        const book = state.data.get(instrumentKey);
        if (!book) return undefined;

        const depth = state.displayDepth.get(instrumentKey) ?? 5;
        return {
            ...book,
            bids: book.bids.slice(0, depth),
            asks: book.asks.slice(0, depth)
        };
    }
}));
