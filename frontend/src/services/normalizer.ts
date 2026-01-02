/**
 * Market Data Normalizer
 * 
 * Transforms raw WebSocket JSON from backend into the canonical VegaTick format.
 * This is the ONLY place where raw JSON is parsed.
 * No React component should ever touch raw feed data.
 */

import { VegaTick, PriceLevel, OHLC, OptionGreeks } from '../types/VegaTick';

/**
 * Raw market update from backend WebSocket.
 * This matches the JSON structure sent by MarketBroadcaster.
 */
interface RawMarketUpdate {
    instrumentKey: string;
    ltp?: number;
    open?: number;
    high?: number;
    low?: number;
    close?: number;
    volume?: number;
    oi?: number;
    atp?: number;
    tbq?: number;
    tsq?: number;
    change?: number;
    changePercent?: number;
    exchangeTimestamp?: number;
    receiveTimestamp?: number;

    // Depth data (if FULL/FULL_D30 mode)
    bids?: Array<{ price: number; quantity: number; orders?: number }>;
    asks?: Array<{ price: number; quantity: number; orders?: number }>;

    // Greeks (for options)
    delta?: number;
    gamma?: number;
    theta?: number;
    vega?: number;
    rho?: number;
    iv?: number;

    // OHLC
    ohlcDaily?: { open: number; high: number; low: number; close: number; volume: number; ts: number };
    ohlcIntraday?: { open: number; high: number; low: number; close: number; volume: number; ts: number };

    // Last trade details
    ltt?: number;
    ltq?: number;

    // Circuit limits
    upperCircuit?: number;
    lowerCircuit?: number;
}

/**
 * Normalize a single price level
 */
function normalizePriceLevel(raw: { price: number; quantity: number; orders?: number }): PriceLevel {
    return {
        price: raw.price ?? 0,
        qty: raw.quantity ?? 0,
        orders: raw.orders
    };
}

/**
 * Normalize OHLC data
 */
function normalizeOHLC(raw?: { open: number; high: number; low: number; close: number; volume: number; ts: number }): OHLC | undefined {
    if (!raw) return undefined;
    return {
        open: raw.open ?? 0,
        high: raw.high ?? 0,
        low: raw.low ?? 0,
        close: raw.close ?? 0,
        volume: raw.volume ?? 0,
        ts: raw.ts ?? Date.now()
    };
}

/**
 * Normalize Greeks data
 */
function normalizeGreeks(raw: RawMarketUpdate): OptionGreeks | undefined {
    // Only include Greeks if at least delta is present
    if (raw.delta === undefined) return undefined;

    return {
        delta: raw.delta ?? 0,
        gamma: raw.gamma ?? 0,
        theta: raw.theta ?? 0,
        vega: raw.vega ?? 0,
        rho: raw.rho ?? 0,
        iv: raw.iv ?? 0
    };
}

/**
 * Main normalizer function.
 * Converts raw WebSocket JSON to canonical VegaTick.
 */
export function normalize(raw: RawMarketUpdate): VegaTick {
    const now = Date.now();
    const closePrice = raw.close ?? 0;
    const ltp = raw.ltp ?? 0;

    return {
        instrumentKey: raw.instrumentKey,
        exchangeTs: raw.exchangeTimestamp ?? now,
        receiveTs: raw.receiveTimestamp ?? now,

        ltpc: {
            ltp: ltp,
            ltt: raw.ltt ?? 0,
            ltq: raw.ltq ?? 0,
            cp: closePrice
        },

        orderBook: {
            bids: (raw.bids ?? []).map(normalizePriceLevel),
            asks: (raw.asks ?? []).map(normalizePriceLevel),
            depth: raw.bids?.length ?? 0
        },

        greeks: normalizeGreeks(raw),

        ohlc: {
            daily: normalizeOHLC(raw.ohlcDaily) ?? {
                open: raw.open ?? 0,
                high: raw.high ?? 0,
                low: raw.low ?? 0,
                close: closePrice,
                volume: raw.volume ?? 0,
                ts: now
            },
            intraday: normalizeOHLC(raw.ohlcIntraday)
        },

        metrics: {
            atp: raw.atp ?? 0,
            volumeTraded: raw.volume ?? 0,
            openInterest: raw.oi ?? 0,
            totalBidQty: raw.tbq ?? 0,
            totalAskQty: raw.tsq ?? 0,
            impliedVolatility: raw.iv,
            upperCircuit: raw.upperCircuit,
            lowerCircuit: raw.lowerCircuit
        },

        change: raw.change ?? (ltp - closePrice),
        changePercent: raw.changePercent ?? (closePrice > 0 ? ((ltp - closePrice) / closePrice) * 100 : 0)
    };
}

/**
 * Batch normalize multiple updates
 */
export function normalizeBatch(rawUpdates: RawMarketUpdate[]): VegaTick[] {
    return rawUpdates.map(normalize);
}

export type { RawMarketUpdate };
