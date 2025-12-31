import { useState, useEffect } from 'react'

export interface IndexData {
    symbol: string
    name: string
    ltp: number
    change: number
    changePercent: number
    high: number
    low: number
}

const MOCK_INDICES: IndexData[] = [
    { symbol: 'NIFTY 50', name: 'NIFTY', ltp: 23644.80, change: 125.40, changePercent: 0.53, high: 23700.00, low: 23550.00 },
    { symbol: 'BANKNIFTY', name: 'BANK', ltp: 51232.15, change: -89.25, changePercent: -0.17, high: 51500.00, low: 51000.00 },
    { symbol: 'FINNIFTY', name: 'FIN', ltp: 23890.50, change: 45.30, changePercent: 0.19, high: 23950.00, low: 23800.00 },
    { symbol: 'INDIA VIX', name: 'VIX', ltp: 13.45, change: -0.50, changePercent: -3.58, high: 14.10, low: 13.20 },
]

export function useDashboardData() {
    const [indices, setIndices] = useState<IndexData[]>(MOCK_INDICES)
    const [loading, setLoading] = useState(true)

    // Simulate initial load
    useEffect(() => {
        const timer = setTimeout(() => {
            setLoading(false)
        }, 500)
        return () => clearTimeout(timer)
    }, [])

    // Simulate live updates
    useEffect(() => {
        const interval = setInterval(() => {
            setIndices(current => current.map(idx => {
                const move = (Math.random() - 0.5) * (idx.ltp * 0.0005) // 0.05% volatility
                const newLtp = idx.ltp + move
                const newChange = idx.change + move
                const newChangePercent = (newChange / (idx.ltp - idx.change)) * 100

                return {
                    ...idx,
                    ltp: newLtp,
                    change: newChange,
                    changePercent: newChangePercent,
                    high: Math.max(idx.high, newLtp),
                    low: Math.min(idx.low, newLtp)
                }
            }))
        }, 1000)

        return () => clearInterval(interval)
    }, [])

    return { indices, loading }
}
