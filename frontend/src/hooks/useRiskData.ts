import { useState, useEffect } from 'react'

export interface RiskMetrics {
    netDelta: number
    netGamma: number
    netVega: number
    netTheta: number
    marginUsed: number
    marginTotal: number
    marginAvailable: number
    pnlDay: number
    pnlDayPercent: number
    var99: number // Value at Risk 99%
}

export interface SectorExposure {
    sector: string
    exposure: number // percentage
    value: number // in absolute terms
    limit: number // limit percentage
}

const MOCK_RISK_METRICS: RiskMetrics = {
    netDelta: 125.5,
    netGamma: 0.082,
    netVega: 1250.8,
    netTheta: -854.2,
    marginUsed: 45.2,
    marginTotal: 1000000,
    marginAvailable: 548000,
    pnlDay: 12500,
    pnlDayPercent: 1.25,
    var99: 45000
}

const MOCK_SECTOR_EXPOSURE: SectorExposure[] = [
    { sector: 'Banking', exposure: 35, value: 350000, limit: 40 },
    { sector: 'IT', exposure: 25, value: 250000, limit: 30 },
    { sector: 'Energy', exposure: 20, value: 200000, limit: 25 },
    { sector: 'Pharma', exposure: 12, value: 120000, limit: 20 },
    { sector: 'Other', exposure: 8, value: 80000, limit: 15 },
]

export function useRiskData() {
    const [metrics, setMetrics] = useState<RiskMetrics>(MOCK_RISK_METRICS)
    const [sectorExposure, setSectorExposure] = useState<SectorExposure[]>(MOCK_SECTOR_EXPOSURE)
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        const timer = setTimeout(() => {
            setLoading(false)
        }, 600)

        // Simulate live risk updates
        const interval = setInterval(() => {
            setMetrics(current => ({
                ...current,
                netDelta: current.netDelta + (Math.random() - 0.5) * 5,
                netVega: current.netVega + (Math.random() - 0.5) * 10,
                pnlDay: current.pnlDay + (Math.random() - 0.5) * 100
            }))
        }, 1000)

        return () => {
            clearTimeout(timer)
            clearInterval(interval)
        }
    }, [])

    return { metrics, sectorExposure, loading }
}
