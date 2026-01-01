import { useState, useEffect } from 'react'
import { LiveMarketSnapshot, FeedMode } from '../types/market'
import { marketDataService } from '../services/marketDataService'
import { marketStream } from '../services/marketWebSocket'

// Mapping for compatibility with legacy StockData view
export interface StockData extends LiveMarketSnapshot {
    symbol: string
}

const WATCHLIST = [
    'NSE_EQ|RELIANCE', 'NSE_EQ|TCS', 'NSE_EQ|HDFCBANK', 'NSE_EQ|INFY',
    'NSE_EQ|ICICIBANK', 'NSE_EQ|SBIN', 'NSE_EQ|BHARTIARTL',
    'NSE_EQ|KOTAKBANK', 'NSE_EQ|ITC', 'NSE_EQ|BAJFINANCE'
]

export function useMarketData() {
    const [stocks, setStocks] = useState<Map<string, StockData>>(new Map())
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        const init = async () => {
            try {
                // 1. Connect WS
                marketStream.connect()

                // 2. Subscribe via API
                // Assuming client ID 'WEB_CLIENT' for now
                await marketDataService.subscribe({
                    instrumentKeys: WATCHLIST,
                    mode: FeedMode.FULL
                })

                // Initialize map with empty placeholders or wait for ticks
                const initialMap = new Map<string, StockData>()
                WATCHLIST.forEach(key => {
                    initialMap.set(key, {
                        instrumentKey: key,
                        symbol: key.split('|')[1],
                        ltp: 0,
                        change: 0,
                        changePercent: 0,
                        volume: 0,
                        oi: 0,
                        atp: 0,
                        tbq: 0,
                        tsq: 0,
                        open: 0,
                        high: 0,
                        low: 0,
                        close: 0,
                        exchangeTimestamp: 0,
                        receiveTimestamp: 0
                    })
                })
                setStocks(initialMap)
                setLoading(false)

            } catch (err: any) {
                console.error('Failed to init market data', err)
                setError(err.message)
                setLoading(false)
            }
        }

        init()

        // 3. Listen to Ticks
        const unsub = marketStream.onTick((tick) => {
            setStocks(prev => {
                const current = prev.get(tick.instrumentKey)
                if (!current) return prev // Ignore unknown symbols (robustness)

                // Merge and update
                const next = new Map(prev)
                next.set(tick.instrumentKey, {
                    ...current,
                    ...tick,
                    symbol: tick.instrumentKey.split('|')[1] // Ensure symbol is preserved
                })
                return next
            })
        })

        return () => {
            marketDataService.unsubscribe(WATCHLIST).catch(console.error)
            marketStream.disconnect()
            unsub()
        }
    }, [])

    return {
        stocks: Array.from(stocks.values()),
        loading,
        error
    }
}
