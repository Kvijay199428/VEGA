export interface LiveMarketSnapshot {
    instrumentKey: string;
    ltp: number;
    open: number;
    high: number;
    low: number;
    close: number;
    volume: number;
    oi: number;
    atp: number;
    tbq: number;
    tsq: number;
    change: number;
    changePercent: number;
    exchangeTimestamp: number;
    receiveTimestamp: number;
}

export interface DepthLevel {
    price: number;
    quantity: number;
    orders: number;
}

export interface OrderBookSnapshot {
    instrumentKey: string;
    timestamp: number;
    bids: DepthLevel[];
    asks: DepthLevel[];
}

export enum FeedMode {
    LTPC = "LTPC",
    FULL = "FULL",
    FULL_D30 = "FULL_D30"
}

export interface SubscriptionRequest {
    instrumentKeys: string[];
    mode: FeedMode;
}

export interface SubscriptionResponse {
    subscribed: string[];
    newSubscriptions: string[];
    mode: string;
    totalActive: number;
}
