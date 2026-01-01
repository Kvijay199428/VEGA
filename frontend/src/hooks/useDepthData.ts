import { useState, useEffect } from 'react'
import { OrderBookSnapshot } from '../types/market'
import { marketStream } from '../services/marketWebSocket'

/**
 * Hook to subscribe to Depth (L2) data for a specific instrument.
 * NOTE: In a real app, this might trigger a specific "subscribe to depth" API call 
 * if depth is a separate stream or mode.
 */
export function useDepthData(instrumentKey: string | null) {
    const [depth, setDepth] = useState<OrderBookSnapshot | null>(null)

    useEffect(() => {
        if (!instrumentKey) {
            setDepth(null)
            return
        }

        // Listen to depth updates
        const unsub = marketStream.onDepth((snapshot) => {
            if (snapshot.instrumentKey === instrumentKey) {
                setDepth(snapshot)
            }
        })

        return () => {
            unsub()
            // Optional: Unsubscribe from depth mode if handled separately by backend
        }
    }, [instrumentKey])

    return { depth }
}
