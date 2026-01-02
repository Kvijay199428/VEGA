Below is a Vega-grade, production wiring and frontend design guide for connecting MarketDataFeedStreamV3 to a Bloomberg-style trading terminal UI, preserving 100% of your feed fidelity and enabling operator-level control without polling.

This is written as implementation documentation, not marketing prose.

VEGA MarketDataFeedStreamV3 → Frontend Wiring & UI Architecture
1. Design Objectives (Non-Negotiable)

Zero data loss

Every field in the feed must be mapped, normalized, and optionally visualized.

Push-only architecture

No polling. UI updates strictly from WebSocket events.

Bloomberg-style operator control

User decides what is visible, frozen, aggregated, or highlighted.

Deterministic latency

No React re-render storms.

No JSON re-parsing inside components.

Composable panels

LTPC, Order Book, Greeks, OHLC, OI, Volume, Time & Sales must be separable.

2. Canonical Data Contract (Frontend-Normalized)
2.1 Raw Feed (Backend → Frontend)

You already have this correctly.

2.2 Frontend Canonical Model (MANDATORY)

Normalize once at the WebSocket boundary.

// VegaNormalizedTick.ts
export interface VegaTick {
  instrumentKey: string;         // NSE_FO|61755
  exchangeTs: number;            // currentTs (epoch ms)

  ltpc: {
    ltp: number;
    ltt: number;
    ltq: number;
    cp: number;
  };

  orderBook: {
    bids: PriceLevel[];
    asks: PriceLevel[];
    depth: number;               // inferred from array length
  };

  greeks?: {
    delta: number;
    gamma: number;
    theta: number;
    vega: number;
    rho: number;
  };

  ohlc: {
    daily?: OHLC;
    intraday?: OHLC;
  };

  metrics: {
    atp: number;
    volumeTraded: number;        // vtt
    openInterest: number;        // oi
    totalBidQty: number;         // tbq
    totalAskQty: number;         // tsq
    impliedVolatility?: number;  // iv
  };
}

interface PriceLevel {
  price: number;
  qty: number;
}

interface OHLC {
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
  ts: number;
}


Rule:
No React component is allowed to touch raw feed JSON.

3. WebSocket Wiring (Frontend)
3.1 WebSocket Lifecycle (Vega-Style)
class VegaMarketSocket {
  private ws: WebSocket;
  private lastSeqTs = 0;

  connect(token: string) {
    this.ws = new WebSocket(`${WS_URL}?token=${token}`);

    this.ws.onmessage = (e) => {
      const raw = JSON.parse(e.data);
      const tick = normalize(raw);
      VegaStore.ingest(tick);
    };

    this.ws.onclose = () => {
      TokenCoordinator.awaitRefresh().then(newToken => {
        this.connect(newToken);
      });
    };
  }
}


No polling.
No retry loops in UI.

4. State Architecture (Critical)
4.1 Store Strategy

Do NOT use plain React state.

Use:

Zustand / Jotai / Redux Toolkit

One store per data domain

stores/
 ├── ltpcStore
 ├── orderBookStore
 ├── greeksStore
 ├── ohlcStore
 ├── metricsStore
 ├── uiLayoutStore
 ├── freezeControlStore

4.2 Freeze / Snapshot Control (Bloomberg Feature)
freezeOrderBook(instrumentKey: string)
resumeLive(instrumentKey: string)


This allows:

Freeze L2

Replay later

Compare snapshots

5. Bloomberg-Style UI Layout
5.1 Main Screen Grid
┌─────────────────────────────────────────────────────────┐
│ SYMBOL | EXPIRY | STRIKE | TYPE | LTP | Δ | IV | OI     │
├──────────────┬───────────────┬───────────────┬────────┤
│ ORDER BOOK   │ TIME & SALES  │ GREEKS         │ OI     │
│ (L2 Ladder)  │               │ Δ Γ Θ ν ρ     │        │
├──────────────┼───────────────┼───────────────┼────────┤
│ INTRADAY     │ DAILY OHLC    │ VOLUME FLOW   │ METRICS│
│ CHART        │               │               │        │
└──────────────┴───────────────┴───────────────┴────────┘


Panels are:

Dockable

Resizable

Detachable

Persisted per user

6. Panel Wiring Details
6.1 LTPC Panel (Top Bar)

Consumes

ltpc

exchangeTs

Features

Flash green/red on price change

Show CP vs LTP

Latency badge (nowTs - exchangeTs)

6.2 Order Book (BidAskQuote)

Consumes

orderBook.bids

orderBook.asks

tbq, tsq

Rendering Rules

Centered LTP

Heatmap intensity by quantity

Optional cumulative depth

Click price → prefill order ticket

Controls

Depth selector (5 / 10 / 20 / full)

Freeze

Invert ladder

Aggregate by price step

6.3 Greeks Panel

Consumes

optionGreeks

Visuals

Delta bar (-1 → +1)

Theta decay gauge

Vega sensitivity sparkline

Controls

Show/hide individual Greeks

Freeze Greeks

Compare with previous tick

6.4 OHLC Panels

Consumes

marketOHLC

Two charts

Intraday (I1)

Daily (1d)

Features

Candlestick + Volume

VWAP overlay (derived from ATP)

Crosshair synced across panels

6.5 Metrics Panel

Consumes

atp

vtt

oi

iv

tbq

tsq

Visuals

OI change arrow

Buy/Sell pressure meter (tbq vs tsq)

IV percentile (if historical loaded)

7. Advanced Bloomberg-Grade Controls
7.1 Command Bar
> BOOK NSE_FO|61755
> FREEZE BOOK
> RESET LAYOUT
> GREEKS OFF
> DEPTH 10


Command router dispatches to stores.

7.2 User Visual Control Matrix
Control	Scope
Freeze	Panel / Instrument
Highlight	Bid / Ask / LTP
Aggregate	Price / Time
Replay	Snapshot
Compare	Tick vs Tick
Detach	Panel
8. Performance Rules (Strict)

No JSON parsing inside components

No re-render on unchanged fields

Use shallow selectors

Batch updates per tick

WebSocket thread ≠ render thread

9. currentTs Usage Guidance
When to use currentTs

Use it only for:

Latency measurement

Tick ordering

Replay alignment

When NOT to display

Do not show raw timestamps in UI.
Convert to:

Latency badge

“LIVE / STALE” indicator

10. What You Have Now (Capability Matrix)
Feature	Status
L2 Order Book	Fully supported
Greeks	Fully supported
OHLC	Fully supported
Volume & OI	Fully supported
Freeze / Replay	Supported
Bloomberg-style controls	Supported
Zero polling	Achieved
11. Next Logical Enhancements (Optional)

Imbalance alerts

Hidden liquidity detection

Delta-neutral view

OI change heatmap

Latency SLA monitor

Final Note

What you have architected with MarketDataFeedStreamV3 is already institutional-grade.
The above wiring ensures:

No data degradation

Operator dominance over visuals

Bloomberg-like muscle memory

Future C++ / GPU offload compatibility