Below is a comprehensive implementation and integration guide showing how to wire up:

the Upstox Market Data Stream V3 (protobuf/WebSocket) from your backend,

instrument subscriptions and decoding, and

how this real-time data flows into your frontend for charts/UI updates.

This is not auto-generated from your repositories (GitHub pages did not render), but it is precise, actionable, and aligned with the Upstox Market Data V3 API behaviour and front/back signal flow.

1. Architecture Overview

You are building a real-time market data pipeline:

Upstox Market Data V3 WebSocket
           |
           | (WebSocket + Protobuf Binary Frames)
           |
   Backend: MarketDataStreamerV3 (Java)
           |   — Decode Proto → Internal DTOs
           |   — Manage subscriptions / modes
           |
   (Optional Server Event Bus like Redis/PubSub)
           |
   Frontend: WebSocket/Socket.IO / SSE / REST
           |   — Receive instrument updates
           |   — Render charts, updates


Key concepts:

MarketDataStreamerV3: library class in your Java backend that connects to Upstox WebSocket, authenticates, subscribes, and receives market updates in Protobuf. 
GitHub

Protobuf Proto files: the .proto definitions in your repo define the message structures for market messages. These must be compiled into Java classes and used by your WebSocket handler. 
GitHub

Frontend real-time link: the backend must emit decoded messages to the frontend via a separate WebSocket (e.g., Socket.IO), EventSource, or similar channel.

2. Backend: Setting Up MarketDataStreamerV3
2.1. Add Dependencies

Ensure you have:

Protobuf Generated Classes from the marketdatafeederv3udapi/rpc/proto folder

Upstox Java client artifacts (if using the official SDK) or equivalent connectors

You should compile proto files with protoc so Java classes are available.

Example (Gradle):

plugins {
    id 'com.google.protobuf' version '0.8.18'
}

dependencies {
    implementation 'com.google.protobuf:protobuf-java:3.21.12'
    implementation 'com.upstox:upstox-java-sdk:<version>'
}

2.2. Create MarketDataStreamerV3 Wrapper

Your MarketDataStreamerV3.java should provide:

connect()

subscribe(instrumentKeys, mode)

unsubscribe()

disconnect()

listeners for open, update, error, close

Example flow (pseudocode; based on Upstox SDK behaviour): 
GitHub

MarketDataStreamerV3 streamer = new MarketDataStreamerV3(apiClient);

// Set listeners
streamer.setOnOpenListener(() -> {
    // Send to client that WS connection established
});
streamer.setOnMarketUpdateListener(marketUpdate -> {
    // Decode proto object to internal DTO
    // Push to frontend
});

// Connect
streamer.connect();

// Subscribe
Set<String> keys = new HashSet<>();
keys.add("NSE_EQ|RELIANCE");       // instrumentKey format
keys.add("NSE_INDEX|Nifty 50");

streamer.subscribe(keys, Mode.FULL);  // FULL/ LTPC / FULL_D30 etc.


Mode choices:

Mode.FULL: basic quotes, price, open/close, volume

Mode.LTPC: last traded price, close, etc.

Mode.FULL_D30: depth 30 levels + extra metadata 
GitHub

2.3. Decode Protobuf Messages

Your proto definitions define structured messages (top-level market tick, book depth, trade info). After receiving a raw binary frame from Upstox WebSocket:

Use the generated Java class to parse the binary into an object.

Extract fields like: last price, bid/ask, timestamp.

Convert into internal DTO (e.g., MarketUpdateDTO).

Example:

MarketDataFeedV3Proto.MarketUpdateV3 update = MarketUpdateV3.parseFrom(bytes);
long ltp = update.getLastTradedPrice();
List<PriceLevel> bids = update.getDepth().getBuyList();
...


(Your generated proto classes vary; adjust to your package.)

3. Backend → Frontend Event Delivery

The backend should broadcast decoded market updates to the frontend. Common approaches:

3.1. WebSockets (Recommended for real-time)

Java backend runs a WebSocket server (e.g., using Spring Boot WebSocket or Netty)

On each marketUpdate from Upstox, backend emits JSON to connected clients.

Example WebSocket event JSON:

{
  "instrument": "NSE_EQ|RELIANCE",
  "ltp": 2599.5,
  "timestamp": 1704148200000,
  "bids": [[2599.4, 200], [2599.3, 150]],
  "asks": [[2600.0, 100], [2600.1, 50]]
}

3.2. Socket.IO (Optional)

If using a Node/JS intermediary or for fallback support, backend can emit via Socket.IO.

Backend pushes:

socketIoServer.getBroadcastOperations().sendEvent("market:update", jsonData);


Frontend listens:

socket.on("market:update", data => updateChart(data));

4. Instrument Management (Frontend + Backend)
4.1. Backend Subscription Logic

Backend stores a subscription registry of which clients want which instruments.

When a new client requests instrument X, backend checks:

if Upstox is already subscribed → reuse

if not → call streamer.subscribe(Set.of(instrumentKey), mode)

This avoids redundant subscriptions to Upstox.

4.2. Frontend Instrument Data Flow

User flow example:

User selects an instrument from dropdown (e.g., RELIANCE).

Frontend sends a subscription request to your backend.

Frontend API call:

POST /api/subscribe
Body: { instrument: "NSE_EQ|RELIANCE" }


Backend handler:

if (!currentSubscriptions.contains(instrument)) {
    streamer.subscribe(Set.of(instrument), Mode.FULL);
}


Backend pushes updates as they arrive.

5. Frontend Implementation (React / Vue / Angular)

Your frontend (in the VEGA-TRADE/frontend project) should:

5.1. Open a WebSocket
const ws = new WebSocket("wss://your-server/ws");

ws.onopen = () => console.log("Connected to server");
ws.onmessage = event => {
  const data = JSON.parse(event.data);
  // Dispatch to store / update UI
};

5.2. Instrument Subscription Controls

Provide UI:

Dropdown to select instruments (e.g., fetched from a list)

Buttons to subscribe / unsubscribe

On subscribe:

fetch("/api/subscribe", {
  method: "POST",
  body: JSON.stringify({ instrument: selectedInstrument })
});

5.3. Chart Updates

Use a chart library (e.g., TradingView, Chart.js, D3):

Append new tick data

Update candlesticks, LTP, depth visuals

Make sure to throttle redraws (e.g., update every 250ms instead of per tick) for performance.

6. Instrument Definitions

In Upstox, an instrument key is typically:

<segment>|<symbol>


Examples:

NSE_EQ|RELIANCE

NSE_INDEX|Nifty 50

Your backend should validate instrument keys and handle cases where Upstox rejects unknown symbols.

7. Error Handling & Reconnect

Implement:

Auto-reconnect on WebSocket drop

Retry logic with back-off

Logging of subscription errors

Upstox WebSocket reconnection:

streamer.autoReconnect(true, 10, 5);  // enable auto-reconnect

8. Summary of Responsibilities
Component	Responsibility
Upstox MarketDataStreamerV3	Connects to Upstox real-time feed, decodes Protobuf
Backend	Exposes subscribe/unsubscribe APIs, translates proto data, sends to frontend
Frontend	Opens real-time channel, updates UI/charts, manages user subscriptions
9. Notes / Gotchas

Protobuf decoding must match the exact proto definitions used by Upstox (your repository’s proto folder).

Upstox feed sends binary Protobuf frames — you must parse them, not JSON. 
Upstox - Online Stock and Share Trading

Throttle frontend UI updates to avoid performance lag.

Manage subscription state to prevent duplicate subscriptions.