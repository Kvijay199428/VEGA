import { useState, useEffect } from 'react'

export interface SectorData {
    name: string
    code: string
    change: number
    changePercent: number
    topStock: string
    topStockChange: number
    volume: number
    marketCap: number // in Cr
}

export interface ConstituentData {
    symbol: string
    name: string
    weight: number
    ltp: number
    change: number
    changePercent: number
    volume: number
}

const MOCK_SECTORS: SectorData[] = [
    { name: 'Nifty Bank', code: 'BANK', change: 450.25, changePercent: 1.32, topStock: 'HDFCBANK', topStockChange: 0.75, volume: 12500000, marketCap: 2500000 },
    { name: 'Nifty IT', code: 'IT', change: -150.40, changePercent: -0.45, topStock: 'TCS', topStockChange: -0.39, volume: 4500000, marketCap: 1800000 },
    { name: 'Nifty Pharma', code: 'PHARMA', change: 120.10, changePercent: 0.89, topStock: 'SUNPHARMA', topStockChange: 1.12, volume: 3200000, marketCap: 900000 },
    { name: 'Nifty FMCG', code: 'FMCG', change: 45.60, changePercent: 0.23, topStock: 'HINDUNILVR', topStockChange: 0.15, volume: 2800000, marketCap: 1100000 },
    { name: 'Nifty Auto', code: 'AUTO', change: -85.30, changePercent: -0.67, topStock: 'TATAMOTORS', topStockChange: -1.2, volume: 5600000, marketCap: 1300000 },
    { name: 'Nifty Energy', code: 'ENERGY', change: 230.50, changePercent: 1.85, topStock: 'RELIANCE', topStockChange: 0.96, volume: 7800000, marketCap: 2100000 },
    { name: 'Nifty Metal', code: 'METAL', change: 145.20, changePercent: 2.34, topStock: 'TATASTEEL', topStockChange: 2.1, volume: 6500000, marketCap: 800000 },
    { name: 'Nifty Realty', code: 'REALTY', change: -15.80, changePercent: -1.15, topStock: 'DLF', topStockChange: -0.98, volume: 1200000, marketCap: 400000 },
]

const MOCK_CONSTITUENTS_BANK: ConstituentData[] = [
    { symbol: 'HDFCBANK', name: 'HDFC Bank Ltd', weight: 29.5, ltp: 1678.80, change: 12.50, changePercent: 0.75, volume: 5678000 },
    { symbol: 'ICICIBANK', name: 'ICICI Bank Ltd', weight: 24.8, ltp: 1023.40, change: 5.60, changePercent: 0.55, volume: 3456000 },
    { symbol: 'SBIN', name: 'State Bank of India', weight: 11.2, ltp: 756.90, change: -3.20, changePercent: -0.42, volume: 6789000 },
    { symbol: 'KOTAKBANK', name: 'Kotak Mahindra Bank', weight: 9.5, ltp: 1789.50, change: -7.40, changePercent: -0.41, volume: 987000 },
    { symbol: 'AXISBANK', name: 'Axis Bank Ltd', weight: 8.8, ltp: 1123.45, change: 12.55, changePercent: 1.12, volume: 2345000 },
    { symbol: 'INDUSINDBK', name: 'IndusInd Bank Ltd', weight: 6.2, ltp: 1456.70, change: 15.60, changePercent: 1.08, volume: 1560000 },
]

export function useSectorData() {
    const [sectors, setSectors] = useState<SectorData[]>(MOCK_SECTORS)
    const [constituents, setConstituents] = useState<Record<string, ConstituentData[]>>({
        'BANK': MOCK_CONSTITUENTS_BANK,
        // Mock others with same data for now
        'IT': MOCK_CONSTITUENTS_BANK.map(s => ({ ...s, changePercent: -s.changePercent, change: -s.change })),
        'PHARMA': MOCK_CONSTITUENTS_BANK,
        'FMCG': MOCK_CONSTITUENTS_BANK,
        'AUTO': MOCK_CONSTITUENTS_BANK.map(s => ({ ...s, changePercent: -s.changePercent, change: -s.change })),
        'ENERGY': MOCK_CONSTITUENTS_BANK,
        'METAL': MOCK_CONSTITUENTS_BANK,
        'REALTY': MOCK_CONSTITUENTS_BANK.map(s => ({ ...s, changePercent: -s.changePercent, change: -s.change })),
    })
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        const timer = setTimeout(() => {
            setLoading(false)
        }, 800)

        const interval = setInterval(() => {
            setSectors(current =>
                current.map(s => ({
                    ...s,
                    changePercent: s.changePercent + (Math.random() - 0.5) * 0.05,
                    volume: s.volume + Math.floor(Math.random() * 5000)
                }))
            )
        }, 3000)

        return () => {
            clearTimeout(timer)
            clearInterval(interval)
        }
    }, [])

    return { sectors, constituents, loading }
}
