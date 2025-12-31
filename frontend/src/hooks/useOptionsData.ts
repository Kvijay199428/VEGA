import { useState, useEffect } from 'react'

export interface OptionData {
    strike: number
    ceIv: number
    ceDelta: number
    ceGamma: number
    ceTheta: number
    ceLtp: number
    peLtp: number
    peTheta: number
    peGamma: number
    peDelta: number
    peIv: number
}

const MOCK_OPTIONS: OptionData[] = [
    { strike: 23400, ceIv: 12.5, ceDelta: 0.85, ceGamma: 0.002, ceTheta: -8.5, ceLtp: 285.50, peLtp: 12.30, peTheta: -2.1, peGamma: 0.001, peDelta: -0.12, peIv: 14.2 },
    { strike: 23450, ceIv: 11.8, ceDelta: 0.78, ceGamma: 0.003, ceTheta: -9.2, ceLtp: 245.80, peLtp: 18.45, peTheta: -3.4, peGamma: 0.002, peDelta: -0.18, peIv: 13.8 },
    { strike: 23500, ceIv: 11.2, ceDelta: 0.68, ceGamma: 0.004, ceTheta: -10.5, ceLtp: 198.25, peLtp: 28.60, peTheta: -4.8, peGamma: 0.003, peDelta: -0.28, peIv: 13.2 },
    { strike: 23550, ceIv: 10.8, ceDelta: 0.55, ceGamma: 0.005, ceTheta: -11.2, ceLtp: 155.40, peLtp: 45.25, peTheta: -6.2, peGamma: 0.004, peDelta: -0.38, peIv: 12.8 },
    { strike: 23600, ceIv: 10.5, ceDelta: 0.45, ceGamma: 0.006, ceTheta: -12.0, ceLtp: 118.90, peLtp: 68.50, peTheta: -7.8, peGamma: 0.005, peDelta: -0.48, peIv: 12.4 },
    { strike: 23650, ceIv: 10.2, ceDelta: 0.35, ceGamma: 0.005, ceTheta: -11.5, ceLtp: 85.60, peLtp: 98.70, peTheta: -9.5, peGamma: 0.005, peDelta: -0.58, peIv: 12.0 },
    { strike: 23700, ceIv: 10.0, ceDelta: 0.25, ceGamma: 0.004, ceTheta: -10.2, ceLtp: 58.45, peLtp: 135.80, peTheta: -10.8, peGamma: 0.004, peDelta: -0.68, peIv: 11.8 },
    { strike: 23750, ceIv: 9.8, ceDelta: 0.18, ceGamma: 0.003, ceTheta: -8.5, ceLtp: 38.20, peLtp: 178.50, peTheta: -11.5, peGamma: 0.003, peDelta: -0.78, peIv: 11.5 },
]

export function useOptionsData(underlying: string, expiry: string) {
    const [options, setOptions] = useState<OptionData[]>([])
    const [spotPrice, setSpotPrice] = useState(23644.80)
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        setLoading(true)
        const timer = setTimeout(() => {
            setOptions(MOCK_OPTIONS)
            setLoading(false)
        }, 600)

        // Simulate random price movements
        const interval = setInterval(() => {
            setSpotPrice(p => p + (Math.random() - 0.5) * 5)
            setOptions(current =>
                current.map(o => ({
                    ...o,
                    ceLtp: o.ceLtp + (Math.random() - 0.5),
                    peLtp: o.peLtp + (Math.random() - 0.5)
                }))
            )
        }, 1000)

        return () => {
            clearTimeout(timer)
            clearInterval(interval)
        }
    }, [underlying, expiry])

    return { options, spotPrice, loading }
}
