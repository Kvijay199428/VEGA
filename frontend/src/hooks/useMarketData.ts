import { useState, useEffect } from 'react'

export interface StockData {
    symbol: string
    ltp: number
    change: number
    changePercent: number
    volume: number
    oi: number
    high: number
    low: number
}

const MOCK_STOCKS: StockData[] = [
    { symbol: 'RELIANCE', ltp: 2456.50, change: 23.45, changePercent: 0.96, volume: 4523000, oi: 0, high: 2478.00, low: 2430.25 },
    { symbol: 'TCS', ltp: 3890.15, change: -15.30, changePercent: -0.39, volume: 1234000, oi: 0, high: 3920.00, low: 3875.00 },
    { symbol: 'HDFCBANK', ltp: 1678.80, change: 12.50, changePercent: 0.75, volume: 5678000, oi: 0, high: 1692.00, low: 1665.00 },
    { symbol: 'INFY', ltp: 1534.25, change: -8.75, changePercent: -0.57, volume: 2345000, oi: 0, high: 1550.00, low: 1525.00 },
    { symbol: 'ICICIBANK', ltp: 1023.40, change: 5.60, changePercent: 0.55, volume: 3456000, oi: 0, high: 1035.00, low: 1015.00 },
    { symbol: 'SBIN', ltp: 756.90, change: -3.20, changePercent: -0.42, volume: 6789000, oi: 0, high: 765.00, low: 750.00 },
    { symbol: 'BHARTIARTL', ltp: 1567.80, change: 18.90, changePercent: 1.22, volume: 1567000, oi: 0, high: 1580.00, low: 1545.00 },
    { symbol: 'KOTAKBANK', ltp: 1789.50, change: -7.40, changePercent: -0.41, volume: 987000, oi: 0, high: 1805.00, low: 1775.00 },
    { symbol: 'ITC', ltp: 456.20, change: 2.10, changePercent: 0.46, volume: 8900000, oi: 0, high: 460.00, low: 452.00 },
    { symbol: 'BAJFINANCE', ltp: 6789.00, change: -45.00, changePercent: -0.66, volume: 450000, oi: 0, high: 6850.00, low: 6750.00 },
]

export function useMarketData() {
    const [stocks, setStocks] = useState<StockData[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        // Simulate API call
        const timer = setTimeout(() => {
            setStocks(MOCK_STOCKS)
            setLoading(false)
        }, 800)

        // Simulate live updates
        const interval = setInterval(() => {
            setStocks(current =>
                current.map(s => ({
                    ...s,
                    ltp: s.ltp + (Math.random() - 0.5) * 0.5,
                    volume: s.volume + Math.floor(Math.random() * 100)
                }))
            )
        }, 2000)

        return () => {
            clearTimeout(timer)
            clearInterval(interval)
        }
    }, [])

    return { stocks, loading, error }
}
