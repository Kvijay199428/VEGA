# 📡 UPSTOX MARKET DATA FEED V3 - INSTRUMENT GUIDE

**Complete Guide for Instrument Download, Key Mapping, and WebSocket Integration**

---

## 📋 TABLE OF CONTENTS

1. [Instrument Download Methods](#instrument-download)
2. [Instrument Key Format & Mapping](#instrument-key-format)
3. [WebSocket Connection Setup](#websocket-setup)
4. [Market Data Feed V3 Protocol](#market-data-protocol)
5. [Protobuf Decoding](#protobuf-decoding)
6. [Backend Integration](#backend-integration)
7. [Complete Implementation](#complete-implementation)

---

## 🔄 INSTRUMENT DOWNLOAD

### **Method 1: Using Upstox API - Get All Instruments**

```
Endpoint: GET /api/v1/market/instruments
Authorization: Bearer {access_token}
```

**Response Structure**:
```json
{
  "status": "success",
  "data": {
    "instrumentType": "EQ",
    "exchanges": {
      "NSE": {
        "NSE_EQ|Infy-EQ": {
          "instrumentKey": "NSE_EQ|Infy-EQ",
          "segment": "EQ",
          "exchange": "NSE",
          "tradingSymbol": "INFY",
          "name": "Infosys Limited",
          "instrumentToken": "123456",
          "isin": "INE009A01021",
          "lotSize": "1",
          "tickSize": "1"
        },
        "NSE_EQ|TCS-EQ": {...}
      }
    }
  }
}
```

### **Method 2: Search Specific Instrument**

```
Endpoint: GET /api/v1/market/search?query=INFY
Authorization: Bearer {access_token}
```

**Response**:
```json
{
  "status": "success",
  "data": [
    {
      "instrumentKey": "NSE_EQ|Infy-EQ",
      "tradingSymbol": "INFY",
      "name": "Infosys Limited",
      "exchange": "NSE",
      "segment": "EQ"
    }
  ]
}
```

### **Method 3: Get Market Instruments (Complete List)**

```python
# Backend Implementation: market.py

from fastapi import APIRouter, Depends
from services.upstox_service import upstox_service
from middleware.auth import get_current_user

router = APIRouter(prefix="/api/v1/market", tags=["Market"])

@router.get("/instruments")
async def get_instruments(
    exchange: str = None,  # NSE, BSE, MCX, NCDEX
    segment: str = None,   # EQ, FO, COM
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Get all available instruments from Upstox
    
    Query Parameters:
    - exchange: NSE, BSE, MCX, NCDEX
    - segment: EQ (Equity), FO (Futures & Options), COM (Commodity)
    
    Returns: List of instruments with keys
    """
    try:
        # Get access token from user
        access_token = decrypt_token(current_user['upstox_access_token'])
        
        # Call Upstox API
        instruments_data = await upstox_service.get_instruments(
            access_token=access_token,
            exchange=exchange,
            segment=segment
        )
        
        # Format response with instrument keys
        formatted_instruments = format_instrument_response(instruments_data)
        
        # Cache in database for quick lookup
        cache_instruments(formatted_instruments, current_user['id'], db)
        
        return {
            "status": "success",
            "data": formatted_instruments,
            "total_count": len(formatted_instruments)
        }
    
    except Exception as e:
        logger.error(f"Failed to get instruments: {e}")
        raise HTTPException(status_code=500, detail=str(e))
```

---

## 🔑 INSTRUMENT KEY FORMAT & MAPPING

### **Instrument Key Structure**

```
Format: {EXCHANGE}_{SEGMENT}|{TRADING_SYMBOL}

Examples:
├─ NSE_EQ|Infy-EQ          (Infosys - NSE Equity)
├─ NSE_FO|NIFTY25JAN2600PE (Nifty Option - Futures & Options)
├─ BSE_EQ|TCS-EQ           (TCS - BSE Equity)
├─ MCX_FO|CRUDEOIL25JAN    (Crude Oil Futures)
├─ NSE_INDEX|Nifty50       (Nifty 50 Index)
└─ BSE_INDEX|SENSEX        (Sensex Index)
```

### **Exchange & Segment Mapping**

```python
EXCHANGE_CODES = {
    "NSE": "National Stock Exchange",
    "BSE": "Bombay Stock Exchange",
    "MCX": "Multi Commodity Exchange",
    "NCDEX": "National Commodity & Derivatives Exchange"
}

SEGMENT_CODES = {
    "EQ": "Equity",
    "FO": "Futures & Options",
    "COM": "Commodity",
    "INDEX": "Index"
}

MARKET_TYPES = {
    "NSE_EQ": "NSE Equities",
    "NSE_FO": "NSE Futures & Options",
    "NSE_INDEX": "NSE Indices",
    "BSE_EQ": "BSE Equities",
    "BSE_FO": "BSE Futures & Options",
    "BSE_INDEX": "BSE Indices",
    "MCX_FO": "MCX Commodities",
    "MCX_INDEX": "MCX Index",
    "NCDEX": "NCDEX Commodities"
}
```

### **Database Schema for Instruments**

```python
# models/instrument.py

from sqlalchemy import Column, String, Integer, Float, DateTime, Boolean
from database.connection import Base

class Instrument(Base):
    __tablename__ = "instruments"
    
    id = Column(String, primary_key=True)
    instrument_key = Column(String, unique=True, nullable=False, index=True)
    exchange = Column(String, nullable=False, index=True)  # NSE, BSE, MCX
    segment = Column(String, nullable=False, index=True)   # EQ, FO, COM, INDEX
    trading_symbol = Column(String, nullable=False, index=True)
    name = Column(String, nullable=False)
    instrument_token = Column(String, unique=True, nullable=False)
    isin = Column(String, nullable=True)
    lot_size = Column(Integer, default=1)
    tick_size = Column(Float, default=0.05)
    multiplier = Column(Integer, default=1)
    
    # Option specific fields
    expiry = Column(DateTime, nullable=True)
    strike_price = Column(Float, nullable=True)
    option_type = Column(String, nullable=True)  # CE, PE
    
    # Futures specific fields
    contract_expiry = Column(DateTime, nullable=True)
    
    last_updated = Column(DateTime, default=datetime.utcnow)
    is_active = Column(Boolean, default=True)
    
    # Timestamps
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


class InstrumentToken(Base):
    """
    Maps instrument key to token for quick lookup
    Used in WebSocket subscriptions and market data
    """
    __tablename__ = "instrument_tokens"
    
    id = Column(String, primary_key=True)
    instrument_key = Column(String, ForeignKey("instruments.instrument_key"))
    token = Column(String, unique=True, index=True)
    updated_at = Column(DateTime, default=datetime.utcnow)
```

---

## 🌐 WEBSOCKET CONNECTION SETUP

### **Step 1: Authenticate & Get WebSocket URL**

```python
# Backend: market.py - Get WebSocket Connection Details

@router.get("/market-feed/auth")
async def get_market_feed_auth(
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Get authenticated WebSocket connection details for Market Data Feed V3
    """
    try:
        # Get user's Upstox access token
        access_token = decrypt_token(current_user['upstox_access_token'])
        
        # Get market feed auth from Upstox
        auth_data = await upstox_service.get_market_feed_auth(access_token)
        
        # Extract WebSocket details
        websocket_url = auth_data['data']['authorizedRedirectUri']
        client_id = auth_data['data']['clientId']
        
        return {
            "status": "success",
            "data": {
                "websocket_url": websocket_url,  # wss://...
                "client_id": client_id,
                "mode": "market_data_feed_v3",
                "proto_file": "/middleware/helper/MarketDataFeed.proto"
            }
        }
    
    except Exception as e:
        logger.error(f"Failed to get market feed auth: {e}")
        raise HTTPException(status_code=500, detail=str(e))
```

### **Step 2: Frontend WebSocket Connection**

```typescript
// Frontend: services/marketDataFeed.ts

class MarketDataFeedV3 {
  private ws: WebSocket | null = null;
  private messageQueue: any[] = [];
  private subscriptions: Map<string, string> = new Map(); // key -> mode
  private guid: string = generateGUID();

  async connect(authToken: string, wsUrl: string) {
    try {
      // Get authenticated WebSocket URL from backend
      const authResponse = await fetch('http://localhost:8000/api/v1/market/market-feed/auth', {
        headers: { 'Authorization': `Bearer ${authToken}` }
      });
      
      const { data } = await authResponse.json();
      
      // Connect to WebSocket with authorization
      this.ws = new WebSocket(data.websocket_url, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Accept': '*/*'
        }
      });
      
      this.ws.binaryType = 'arraybuffer'; // Important: for Protobuf binary data
      
      this.ws.onopen = () => {
        console.log('✅ Connected to Market Data Feed V3');
        this.processQueue();
      };
      
      this.ws.onmessage = (event) => {
        this.handleMessage(event.data);
      };
      
      this.ws.onerror = (error) => {
        console.error('❌ WebSocket error:', error);
      };
      
      this.ws.onclose = () => {
        console.log('⚠️  WebSocket disconnected');
        setTimeout(() => this.reconnect(authToken, wsUrl), 5000);
      };
      
    } catch (error) {
      console.error('Connection error:', error);
      throw error;
    }
  }

  /**
   * Subscribe to instrument keys
   * 
   * @param instrumentKeys - Array of instrument keys
   * @param mode - 'ltpc', 'option_greeks', 'full', or 'full_d30'
   */
  subscribe(instrumentKeys: string[], mode: 'ltpc' | 'option_greeks' | 'full' | 'full_d30' = 'ltpc') {
    const request = {
      guid: this.generateGUID(),
      method: 'sub',
      data: {
        mode: mode,
        instrumentKeys: instrumentKeys
      }
    };
    
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      // Convert to binary and send
      const binary = this.encodeRequest(request);
      this.ws.send(binary);
      
      // Track subscription
      instrumentKeys.forEach(key => {
        this.subscriptions.set(key, mode);
      });
    } else {
      this.messageQueue.push(request);
    }
  }

  /**
   * Change subscription mode for existing instruments
   */
  changeMode(instrumentKeys: string[], newMode: string) {
    const request = {
      guid: this.generateGUID(),
      method: 'change_mode',
      data: {
        mode: newMode,
        instrumentKeys: instrumentKeys
      }
    };
    
    if (this.ws?.readyState === WebSocket.OPEN) {
      const binary = this.encodeRequest(request);
      this.ws.send(binary);
      
      instrumentKeys.forEach(key => {
        this.subscriptions.set(key, newMode);
      });
    }
  }

  /**
   * Unsubscribe from instruments
   */
  unsubscribe(instrumentKeys: string[]) {
    const request = {
      guid: this.generateGUID(),
      method: 'unsub',
      data: {
        instrumentKeys: instrumentKeys
      }
    };
    
    if (this.ws?.readyState === WebSocket.OPEN) {
      const binary = this.encodeRequest(request);
      this.ws.send(binary);
      
      instrumentKeys.forEach(key => {
        this.subscriptions.delete(key);
      });
    }
  }
}
```

---

## 📊 MARKET DATA FEED V3 PROTOCOL

### **Request Format (Binary/Protobuf)**

```
{
  "guid": "13syxu852ztodyqncwt0",
  "method": "sub",
  "data": {
    "mode": "full",
    "instrumentKeys": [
      "NSE_EQ|Infy-EQ",
      "NSE_EQ|TCS-EQ",
      "NSE_INDEX|Nifty50"
    ]
  }
}

Method Values:
├─ "sub"          → Subscribe (default mode: ltpc)
├─ "change_mode"  → Change subscription mode
└─ "unsub"        → Unsubscribe

Mode Values:
├─ "ltpc"         → Latest Trading Price & Close (5000 keys max)
├─ "option_greeks"→ Option Greeks (3000 keys max)
├─ "full"         → LTPC + 5 market levels + metadata (2000 keys max)
└─ "full_d30"     → LTPC + 30 market levels (Plus only, 50 keys max)
```

### **Response Format - Market Status (First Message)**

```json
{
  "type": "market_info",
  "currentTs": "1732775008661",
  "marketInfo": {
    "segmentStatus": {
      "NSE_EQ": "NORMAL_OPEN",
      "NSE_FO": "NORMAL_OPEN",
      "NSE_INDEX": "NORMAL_OPEN",
      "BSE_EQ": "NORMAL_OPEN",
      "BSE_FO": "NORMAL_OPEN",
      "BSE_INDEX": "NORMAL_OPEN",
      "MCX_FO": "NORMAL_OPEN",
      "MCX_INDEX": "NORMAL_OPEN",
      "NCDEX_FO": "NORMAL_OPEN"
    }
  }
}

Market Status Values:
├─ "NORMAL_OPEN"        → Market is open
├─ "NORMAL_CLOSED"      → Market is closed
├─ "AFTER_HOURS_OPEN"   → After-hours trading
├─ "AUCTION"            → Auction in progress
├─ "HALT"               → Trading halted
├─ "CIRCUIT_LIMIT_UP"   → Upper circuit limit
├─ "CIRCUIT_LIMIT_DOWN" → Lower circuit limit
└─ "CLOSE"              → Market close
```

### **Response Format - Live Feed Data (LTPC Mode)**

```json
{
  "type": "live_feed",
  "feeds": {
    "NSE_EQ|Infy-EQ": {
      "ltpc": {
        "ltp": 2845.50,           // Last Traded Price
        "ltt": "1740729552723",   // Last Traded Time (ms)
        "ltq": "75",              // Last Traded Quantity
        "cp": 2840.15             // Close Price
      }
    },
    "NSE_EQ|TCS-EQ": {
      "ltpc": {
        "ltp": 3920.25,
        "ltt": "1740729552790",
        "ltq": "100",
        "cp": 3915.80
      }
    }
  },
  "currentTs": "1740729566039"
}
```

### **Response Format - Full Mode Data**

```json
{
  "type": "live_feed",
  "feeds": {
    "NSE_EQ|Infy-EQ": {
      "ltpc": {
        "ltp": 2845.50,
        "ltt": "1740729552723",
        "ltq": "75",
        "cp": 2840.15
      },
      "depth": {
        "buy": [
          { "price": 2845.45, "quantity": "1000", "orders": "25" },
          { "price": 2845.40, "quantity": "500", "orders": "12" },
          { "price": 2845.35, "quantity": "750", "orders": "18" },
          { "price": 2845.30, "quantity": "1200", "orders": "30" },
          { "price": 2845.25, "quantity": "600", "orders": "15" }
        ],
        "sell": [
          { "price": 2845.55, "quantity": "900", "orders": "22" },
          { "price": 2845.60, "quantity": "1100", "orders": "28" },
          { "price": 2845.65, "quantity": "650", "orders": "16" },
          { "price": 2845.70, "quantity": "1300", "orders": "32" },
          { "price": 2845.75, "quantity": "500", "orders": "12" }
        ]
      },
      "metadata": {
        "oi": "1500000",           // Open Interest
        "iv": "18.5",              // Implied Volatility
        "volume": "12500000",      // Volume
        "valueTraded": "35625000"  // Value Traded
      }
    }
  },
  "currentTs": "1740729566039"
}
```

### **Response Format - Option Greeks**

```json
{
  "type": "live_feed",
  "feeds": {
    "NSE_FO|NIFTY25JAN2600PE": {
      "option_greeks": {
        "delta": "-0.45",
        "gamma": "0.012",
        "theta": "-0.08",
        "vega": "0.25",
        "iv": "18.5"
      }
    }
  },
  "currentTs": "1740729566039"
}
```

---

## 🔐 PROTOBUF DECODING

### **Proto File Location**

```
Path: backend/middleware/helper/MarketDataFeed.proto

This file defines the binary message structure for all market data
```

### **Setup Protobuf for Python Backend**

```bash
# Install protobuf compiler
pip install protobuf

# Generate Python classes from proto file
python -m grpc_tools.grpc_python_codegen -i . \
  backend/middleware/helper/MarketDataFeed.proto
```

### **Protobuf Decoder Implementation**

```python
# backend/middleware/helper/market_data_decoder.py

import struct
from typing import Dict, Any, List
from google.protobuf import message
import logging

logger = logging.getLogger(__name__)

class MarketDataDecoder:
    """
    Decodes Protobuf binary messages from Upstox Market Data Feed V3
    """
    
    @staticmethod
    def decode_message(binary_data: bytes) -> Dict[str, Any]:
        """
        Decode binary Protobuf message to JSON
        
        Args:
            binary_data: Raw binary data from WebSocket
            
        Returns:
            Decoded message as dictionary
        """
        try:
            # Import generated protobuf classes
            from middleware.helper import MarketDataFeed_pb2
            
            # Create message object
            feed_message = MarketDataFeed_pb2.FeedMessage()
            
            # Parse binary data
            feed_message.ParseFromString(binary_data)
            
            # Convert to dictionary
            decoded = MessageToDict(
                feed_message,
                preserving_proto_field_name=True
            )
            
            return decoded
            
        except Exception as e:
            logger.error(f"Failed to decode message: {e}")
            return None
    
    @staticmethod
    def decode_ltpc(message_dict: Dict) -> Dict[str, Any]:
        """
        Extract and format LTPC data
        """
        try:
            feed_data = {
                "type": "live_feed",
                "feeds": {},
                "currentTs": message_dict.get('currentTs')
            }
            
            if 'feeds' in message_dict:
                for instrument_key, feed in message_dict['feeds'].items():
                    if 'ltpc' in feed:
                        ltpc = feed['ltpc']
                        feed_data['feeds'][instrument_key] = {
                            "ltpc": {
                                "ltp": float(ltpc.get('ltp', 0)),
                                "ltt": ltpc.get('ltt'),
                                "ltq": str(ltpc.get('ltq')),
                                "cp": float(ltpc.get('cp', 0))
                            }
                        }
            
            return feed_data
            
        except Exception as e:
            logger.error(f"Failed to decode LTPC: {e}")
            return None
    
    @staticmethod
    def decode_full(message_dict: Dict) -> Dict[str, Any]:
        """
        Extract full market data (LTPC + Depth + Metadata)
        """
        try:
            feed_data = {
                "type": "live_feed",
                "feeds": {},
                "currentTs": message_dict.get('currentTs')
            }
            
            if 'feeds' in message_dict:
                for instrument_key, feed in message_dict['feeds'].items():
                    instrument_data = {}
                    
                    # LTPC
                    if 'ltpc' in feed:
                        instrument_data['ltpc'] = {
                            "ltp": float(feed['ltpc'].get('ltp', 0)),
                            "ltt": feed['ltpc'].get('ltt'),
                            "ltq": str(feed['ltpc'].get('ltq')),
                            "cp": float(feed['ltpc'].get('cp', 0))
                        }
                    
                    # Depth data
                    if 'depth' in feed:
                        instrument_data['depth'] = MarketDataDecoder._extract_depth(
                            feed['depth']
                        )
                    
                    # Metadata
                    if 'metadata' in feed:
                        instrument_data['metadata'] = MarketDataDecoder._extract_metadata(
                            feed['metadata']
                        )
                    
                    feed_data['feeds'][instrument_key] = instrument_data
            
            return feed_data
            
        except Exception as e:
            logger.error(f"Failed to decode full feed: {e}")
            return None
    
    @staticmethod
    def _extract_depth(depth_data: Dict) -> Dict[str, List]:
        """Extract market depth (bid/ask)"""
        return {
            "buy": [
                {
                    "price": float(bid.get('price', 0)),
                    "quantity": str(bid.get('quantity')),
                    "orders": str(bid.get('orders'))
                }
                for bid in depth_data.get('buy', [])
            ],
            "sell": [
                {
                    "price": float(ask.get('price', 0)),
                    "quantity": str(ask.get('quantity')),
                    "orders": str(ask.get('orders'))
                }
                for ask in depth_data.get('sell', [])
            ]
        }
    
    @staticmethod
    def _extract_metadata(metadata: Dict) -> Dict[str, Any]:
        """Extract market metadata"""
        return {
            "oi": str(metadata.get('oi')),
            "iv": float(metadata.get('iv', 0)),
            "volume": str(metadata.get('volume')),
            "valueTraded": str(metadata.get('valueTraded'))
        }
    
    @staticmethod
    def decode_option_greeks(message_dict: Dict) -> Dict[str, Any]:
        """
        Extract option greeks data
        """
        try:
            feed_data = {
                "type": "live_feed",
                "feeds": {},
                "currentTs": message_dict.get('currentTs')
            }
            
            if 'feeds' in message_dict:
                for instrument_key, feed in message_dict['feeds'].items():
                    if 'option_greeks' in feed:
                        greeks = feed['option_greeks']
                        feed_data['feeds'][instrument_key] = {
                            "option_greeks": {
                                "delta": float(greeks.get('delta', 0)),
                                "gamma": float(greeks.get('gamma', 0)),
                                "theta": float(greeks.get('theta', 0)),
                                "vega": float(greeks.get('vega', 0)),
                                "iv": float(greeks.get('iv', 0))
                            }
                        }
            
            return feed_data
            
        except Exception as e:
            logger.error(f"Failed to decode option greeks: {e}")
            return None
```

---

## 🔧 BACKEND INTEGRATION

### **Complete Market Data Service Implementation**

```python
# backend/services/market_data_service.py

import asyncio
import json
import logging
from typing import List, Dict, Any, Callable
from datetime import datetime
import websockets
from middleware.helper.market_data_decoder import MarketDataDecoder
from database.connection import SessionLocal
from models.instrument import Instrument, InstrumentToken

logger = logging.getLogger(__name__)

class MarketDataService:
    """
    Handles WebSocket connection to Upstox Market Data Feed V3
    Decodes Protobuf messages and broadcasts to connected clients
    """
    
    def __init__(self):
        self.ws_connection = None
        self.is_connected = False
        self.subscriptions: Dict[str, str] = {}  # instrument_key -> mode
        self.callbacks: List[Callable] = []
        self.message_buffer = []
        self.decoder = MarketDataDecoder()
        
    async def initialize(self):
        """Initialize market data service"""
        logger.info("Initializing Market Data Service V3")
        # Initialize database cache of instruments
        await self._cache_instruments()
    
    async def _cache_instruments(self):
        """Cache all instruments in database for quick lookup"""
        try:
            db = SessionLocal()
            instruments = db.query(Instrument).all()
            logger.info(f"Cached {len(instruments)} instruments")
            db.close()
        except Exception as e:
            logger.error(f"Failed to cache instruments: {e}")
    
    async def connect(self, websocket_url: str, access_token: str):
        """
        Connect to Upstox Market Data Feed V3 WebSocket
        
        Args:
            websocket_url: Authorized WebSocket URL from Upstox
            access_token: User's access token
        """
        try:
            logger.info("Connecting to Market Data Feed V3...")
            
            # Connect with authorization header
            self.ws_connection = await websockets.connect(
                websocket_url,
                subprotocols=['authorization', access_token],
                extra_headers={
                    'Authorization': f'Bearer {access_token}',
                    'Accept': '*/*'
                }
            )
            
            self.is_connected = True
            logger.info("✅ Connected to Market Data Feed V3")
            
            # Start listening for messages
            await self._listen()
            
        except Exception as e:
            logger.error(f"❌ Connection failed: {e}")
            self.is_connected = False
            await self._reconnect(websocket_url, access_token)
    
    async def subscribe(
        self,
        instrument_keys: List[str],
        mode: str = 'ltpc'
    ):
        """
        Subscribe to instrument updates
        
        Args:
            instrument_keys: List of instrument keys
            mode: 'ltpc', 'option_greeks', 'full', or 'full_d30'
        """
        if not self.is_connected:
            logger.warning("Not connected to WebSocket")
            return
        
        request = {
            "guid": self._generate_guid(),
            "method": "sub",
            "data": {
                "mode": mode,
                "instrumentKeys": instrument_keys
            }
        }
        
        try:
            # Convert to binary (Protobuf encoding)
            binary_request = self._encode_request(request)
            await self.ws_connection.send(binary_request)
            
            # Track subscriptions
            for key in instrument_keys:
                self.subscriptions[key] = mode
            
            logger.info(f"Subscribed to {len(instrument_keys)} instruments - Mode: {mode}")
            
        except Exception as e:
            logger.error(f"Subscription failed: {e}")
    
    async def unsubscribe(self, instrument_keys: List[str]):
        """Unsubscribe from instruments"""
        if not self.is_connected:
            return
        
        request = {
            "guid": self._generate_guid(),
            "method": "unsub",
            "data": {
                "instrumentKeys": instrument_keys
            }
        }
        
        try:
            binary_request = self._encode_request(request)
            await self.ws_connection.send(binary_request)
            
            for key in instrument_keys:
                self.subscriptions.pop(key, None)
            
            logger.info(f"Unsubscribed from {len(instrument_keys)} instruments")
            
        except Exception as e:
            logger.error(f"Unsubscription failed: {e}")
    
    async def change_mode(self, instrument_keys: List[str], new_mode: str):
        """Change subscription mode for instruments"""
        if not self.is_connected:
            return
        
        request = {
            "guid": self._generate_guid(),
            "method": "change_mode",
            "data": {
                "mode": new_mode,
                "instrumentKeys": instrument_keys
            }
        }
        
        try:
            binary_request = self._encode_request(request)
            await self.ws_connection.send(binary_request)
            
            for key in instrument_keys:
                self.subscriptions[key] = new_mode
            
            logger.info(f"Changed mode to {new_mode} for {len(instrument_keys)} instruments")
            
        except Exception as e:
            logger.error(f"Mode change failed: {e}")
    
    async def _listen(self):
        """Listen for incoming market data messages"""
        try:
            async for message in self.ws_connection:
                if isinstance(message, bytes):
                    # Decode Protobuf binary data
                    decoded = self.decoder.decode_message(message)
                    
                    if decoded:
                        # Broadcast to all registered callbacks
                        await self._broadcast(decoded)
                        
                        # Store in buffer for processing
                        self.message_buffer.append({
                            'data': decoded,
                            'timestamp': datetime.utcnow()
                        })
                        
                        # Keep buffer size reasonable
                        if len(self.message_buffer) > 1000:
                            self.message_buffer = self.message_buffer[-500:]
        
        except websockets.exceptions.ConnectionClosed:
            logger.warning("WebSocket connection closed")
            self.is_connected = False
        except Exception as e:
            logger.error(f"Listening error: {e}")
            self.is_connected = False
    
    async def _broadcast(self, decoded_message: Dict):
        """Broadcast message to all registered callbacks"""
        for callback in self.callbacks:
            try:
                if asyncio.iscoroutinefunction(callback):
                    await callback(decoded_message)
                else:
                    callback(decoded_message)
            except Exception as e:
                logger.error(f"Callback error: {e}")
    
    def register_callback(self, callback: Callable):
        """Register callback for market data updates"""
        self.callbacks.append(callback)
    
    async def _reconnect(self, websocket_url: str, access_token: str):
        """Reconnect with exponential backoff"""
        retry_count = 0
        max_retries = 5
        
        while not self.is_connected and retry_count < max_retries:
            retry_count += 1
            wait_time = min(2 ** retry_count, 60)  # Exponential backoff
            
            logger.info(f"Reconnecting in {wait_time} seconds (attempt {retry_count})...")
            await asyncio.sleep(wait_time)
            
            try:
                await self.connect(websocket_url, access_token)
            except Exception as e:
                logger.error(f"Reconnection attempt {retry_count} failed: {e}")
    
    async def cleanup(self):
        """Cleanup connection"""
        if self.ws_connection:
            await self.ws_connection.close()
        self.is_connected = False
        logger.info("Market Data Service cleaned up")
    
    @staticmethod
    def _encode_request(request: Dict) -> bytes:
        """Encode request as binary (Protobuf)"""
        # Convert to JSON bytes
        json_str = json.dumps(request)
        return json_str.encode('utf-8')
    
    @staticmethod
    def _generate_guid() -> str:
        """Generate unique request ID"""
        import uuid
        return str(uuid.uuid4())[:20]


# Global instance
market_data_service = MarketDataService()

async def initialize_market_data_service():
    """Initialize on app startup"""
    await market_data_service.initialize()

async def get_market_data_service():
    """Dependency injection"""
    return market_data_service
```

---

## 📡 BACKEND API ENDPOINTS FOR MARKET DATA

### **Add to market.py router**

```python
# backend/routers/market.py

@router.get("/market-feed/auth")
async def get_market_feed_auth(
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Get authenticated WebSocket URL for Market Data Feed V3"""
    try:
        access_token = decrypt_token(current_user['upstox_access_token'])
        auth_data = await upstox_service.get_market_feed_auth(access_token)
        
        return {
            "status": "success",
            "data": {
                "websocket_url": auth_data['data']['authorizedRedirectUri'],
                "client_id": auth_data['data']['clientId']
            }
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/subscribe")
async def subscribe_to_instruments(
    request: SubscribeRequest,
    current_user: dict = Depends(get_current_user),
    market_data: MarketDataService = Depends(get_market_data_service)
):
    """
    Subscribe to instrument market data
    
    Request:
    {
      "instrument_keys": ["NSE_EQ|Infy-EQ", "NSE_EQ|TCS-EQ"],
      "mode": "ltpc"
    }
    """
    try:
        await market_data.subscribe(
            instrument_keys=request.instrument_keys,
            mode=request.mode
        )
        
        return {
            "status": "success",
            "message": f"Subscribed to {len(request.instrument_keys)} instruments",
            "mode": request.mode
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/unsubscribe")
async def unsubscribe_from_instruments(
    request: UnsubscribeRequest,
    current_user: dict = Depends(get_current_user),
    market_data: MarketDataService = Depends(get_market_data_service)
):
    """Unsubscribe from instruments"""
    try:
        await market_data.unsubscribe(request.instrument_keys)
        
        return {
            "status": "success",
            "message": f"Unsubscribed from {len(request.instrument_keys)} instruments"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/instruments")
async def get_all_instruments(
    exchange: str = None,
    segment: str = None,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Get all available instruments
    
    Query Parameters:
    - exchange: NSE, BSE, MCX, NCDEX
    - segment: EQ, FO, COM
    """
    try:
        query = db.query(Instrument)
        
        if exchange:
            query = query.filter(Instrument.exchange == exchange)
        if segment:
            query = query.filter(Instrument.segment == segment)
        
        instruments = query.filter(Instrument.is_active == True).all()
        
        return {
            "status": "success",
            "data": [
                {
                    "instrument_key": i.instrument_key,
                    "trading_symbol": i.trading_symbol,
                    "name": i.name,
                    "exchange": i.exchange,
                    "segment": i.segment,
                    "lot_size": i.lot_size,
                    "tick_size": i.tick_size
                }
                for i in instruments
            ],
            "total": len(instruments)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/instruments/search")
async def search_instruments(
    q: str,
    limit: int = 10,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Search instruments by symbol or name"""
    try:
        instruments = db.query(Instrument).filter(
            (Instrument.trading_symbol.ilike(f"%{q}%")) |
            (Instrument.name.ilike(f"%{q}%"))
        ).limit(limit).all()
        
        return {
            "status": "success",
            "data": [
                {
                    "instrument_key": i.instrument_key,
                    "trading_symbol": i.trading_symbol,
                    "name": i.name
                }
                for i in instruments
            ]
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
```

---

## ✅ COMPLETE IMPLEMENTATION CHECKLIST

```
Backend Setup:
☐ Proto file in place: backend/middleware/helper/MarketDataFeed.proto
☐ Decoder implemented: backend/middleware/helper/market_data_decoder.py
☐ Service created: backend/services/market_data_service.py
☐ Database models: backend/models/instrument.py
☐ Router endpoints: backend/routers/market.py
☐ Dependencies: pip install protobuf websockets

Frontend Setup:
☐ WebSocket service: services/marketDataFeed.ts
☐ Market data store: Redux/Zustand integration
☐ UI components for subscriptions
☐ Real-time price display
☐ Error handling & reconnection

Configuration:
☐ Connection limits respected:
  - Max 2 connections per user
  - Max 5000 LTPC subscriptions
  - Max 2000 Full subscriptions
  - Max 3000 Option Greeks
☐ Binary message handling
☐ CORS configured
☐ Environment variables set

Testing:
☐ Test instrument download
☐ Test WebSocket connection
☐ Test subscription/unsubscription
☐ Test Protobuf decoding
☐ Test multi-mode subscriptions
☐ Test reconnection logic
```

---

**Document**: Upstox Market Data Feed V3 - Instrument Download & WebSocket Guide  
**Version**: 1.0  
**Date**: December 22, 2025, 12:10 PM IST  
**Proto File**: ✅ MarketDataFeed.proto (location: backend/middleware/helper/)

