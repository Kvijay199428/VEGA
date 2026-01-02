/**
 * VegaTick - Canonical normalized market data model.
 * 
 * This is the ONLY data shape that UI components are allowed to consume.
 * Raw WebSocket JSON is normalized to this format at the boundary.
 */

export interface PriceLevel {
    price: number;
    qty: number;
    orders?: number;
}

export interface OHLC {
    open: number;
    high: number;
    low: number;
    close: number;
    volume: number;
    ts: number;
}

export interface OptionGreeks {
    delta: number;
    gamma: number;
    theta: number;
    vega: number;
    rho: number;
    iv: number;
}

export interface VegaTick {
    instrumentKey: string;         // e.g., NSE_FO|61755
    exchangeTs: number;            // Exchange timestamp (epoch ms)
    receiveTs: number;             // Local receive timestamp

    // Last Traded Price + Close
    ltpc: {
        ltp: number;
        ltt: number;               // Last trade time
        ltq: number;               // Last trade quantity
        cp: number;                // Close price (previous day)
    };

    // Order Book (L2/L3 depth)
    orderBook: {
        bids: PriceLevel[];
        asks: PriceLevel[];
        depth: number;             // Number of levels
    };

    // Option Greeks (optional - only for derivatives)
    greeks?: OptionGreeks;

    // OHLC data
    ohlc: {
        daily?: OHLC;
        intraday?: OHLC;
    };

    // Trading metrics
    metrics: {
        atp: number;               // Average traded price
        volumeTraded: number;      // Total volume traded (vtt)
        openInterest: number;      // OI
        totalBidQty: number;       // Total bid quantity
        totalAskQty: number;       // Total ask quantity
        impliedVolatility?: number;
        upperCircuit?: number;
        lowerCircuit?: number;
    };

    // Computed fields
    change: number;
    changePercent: number;
}

/**
 * Partial tick for incremental updates.
 * Only changed fields are populated.
 */
export type PartialVegaTick = Partial<VegaTick> & { instrumentKey: string };
