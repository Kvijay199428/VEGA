import { useState, useEffect } from 'react'

export interface Position {
    symbol: string
    product: 'MIS' | 'CNC' | 'NRML'
    qty: number
    avgPrice: number
    ltp: number
    pnl: number
    pnlPercent: number
    dayPnl: number
}

export interface Holding {
    symbol: string
    qty: number
    avgPrice: number
    ltp: number
    currentValue: number
    pnl: number
    pnlPercent: number
    t1Qty: number
}

export interface AuditLog {
    id: string
    time: string
    action: string
    details: string
    status: 'SUCCESS' | 'FAILURE' | 'PENDING'
    user: string
    ip: string
}

const MOCK_POSITIONS: Position[] = [
    { symbol: 'NIFTY 24000 CE', product: 'MIS', qty: 50, avgPrice: 145.50, ltp: 162.00, pnl: 825.00, pnlPercent: 11.34, dayPnl: 825.00 },
    { symbol: 'BANKNIFTY 51000 PE', product: 'NRML', qty: 15, avgPrice: 320.00, ltp: 290.00, pnl: -450.00, pnlPercent: -9.37, dayPnl: -120.00 },
    { symbol: 'RELIANCE', product: 'MIS', qty: 100, avgPrice: 2445.00, ltp: 2452.00, pnl: 700.00, pnlPercent: 0.29, dayPnl: 700.00 },
]

const MOCK_HOLDINGS: Holding[] = [
    { symbol: 'HDFCBANK', qty: 50, avgPrice: 1550.00, ltp: 1680.00, currentValue: 84000.00, pnl: 6500.00, pnlPercent: 8.38, t1Qty: 0 },
    { symbol: 'INFY', qty: 100, avgPrice: 1420.00, ltp: 1540.00, currentValue: 154000.00, pnl: 12000.00, pnlPercent: 8.45, t1Qty: 0 },
    { symbol: 'TATASTEEL', qty: 500, avgPrice: 110.00, ltp: 145.00, currentValue: 72500.00, pnl: 17500.00, pnlPercent: 31.81, t1Qty: 0 },
]

const MOCK_LOGS: AuditLog[] = [
    { id: '1', time: '10:00:01', action: 'LOGIN', details: 'Session started via secure auth', status: 'SUCCESS', user: 'SYSTEM', ip: '192.168.1.1' },
    { id: '2', time: '10:00:05', action: 'TOKEN_GEN', details: 'Generated 6/6 API tokens', status: 'SUCCESS', user: 'SYSTEM', ip: '192.168.1.1' },
    { id: '3', time: '10:15:22', action: 'ORDER_PLACE', details: 'BUY 50 NIFTY 24000 CE @ MKT', status: 'SUCCESS', user: 'TRADER', ip: '192.168.1.4' },
    { id: '4', time: '11:30:00', action: 'RISK_CHECK', details: 'Margin utilization > 80%', status: 'FAILURE', user: 'RISK_BOT', ip: 'internal' },
]

export function useAccountData() {
    const [positions, setPositions] = useState<Position[]>(MOCK_POSITIONS)
    const [holdings, setHoldings] = useState<Holding[]>(MOCK_HOLDINGS)
    const [auditLogs, setAuditLogs] = useState<AuditLog[]>(MOCK_LOGS)
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        // Simulate load
        setTimeout(() => setLoading(false), 800)

        // Simulate live PnL updates
        const interval = setInterval(() => {
            setPositions(prev => prev.map(p => ({
                ...p,
                ltp: p.ltp + (Math.random() - 0.5) * 2,
                pnl: (p.ltp - p.avgPrice) * p.qty
            })))
        }, 1000)

        return () => clearInterval(interval)
    }, [])

    return { positions, holdings, auditLogs, loading }
}
