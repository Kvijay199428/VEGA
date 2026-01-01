import { useState, useEffect } from 'react'
import { useOptionsData } from '../hooks/useOptionsData'
import { usePageTitle } from '../context/PageContext'

/**
 * Options Chain - F3 Professional options analysis.
 */
export default function OptionsChain() {
    const [underlying, setUnderlying] = useState('NIFTY')
    const [expiry, setExpiry] = useState('26-DEC-2024')
    const { options, spotPrice, loading } = useOptionsData(underlying, expiry)
    const [selectedStrikeIndex, setSelectedStrikeIndex] = useState<number | null>(null)

    // Set page title in TopBar
    usePageTitle('Options Chain', 'F3', 'OMON <GO>')

    // Keyboard Navigation
    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            if (loading || options.length === 0) return

            if (e.key === 'ArrowDown') {
                setSelectedStrikeIndex(prev =>
                    prev === null ? 0 : Math.min(prev + 1, options.length - 1)
                )
            } else if (e.key === 'ArrowUp') {
                setSelectedStrikeIndex(prev =>
                    prev === null ? options.length - 1 : Math.max(prev - 1, 0)
                )
            } else if (e.key === 'Enter' && selectedStrikeIndex !== null) {
                // Placeholder for order entry
                console.log('Selected strike:', options[selectedStrikeIndex].strike)
            }
        }
        window.addEventListener('keydown', handleKeyDown)
        return () => window.removeEventListener('keydown', handleKeyDown)
    }, [loading, options, selectedStrikeIndex])

    if (loading) {
        return (
            <div className="space-y-4 animate-pulse">
                <div className="h-10 bg-[#161b22] rounded w-full"></div>
                <div className="flex gap-4">
                    <div className="h-96 flex-1 bg-[#161b22] rounded border border-[#30363d]"></div>
                </div>
            </div>
        )
    }

    return (
        <div className="space-y-4">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-xl font-bold text-[#c9d1d9] flex items-center gap-2">
                        Options Chain
                        <span className="text-xs font-normal text-[#6e7681] border border-[#30363d] px-1 rounded">F3</span>
                    </h1>
                    <p className="text-sm text-[#6e7681]">OMON &lt;GO&gt;</p>
                </div>

                <div className="flex items-center gap-4">
                    {/* Underlying Selector */}
                    <select
                        value={underlying}
                        onChange={(e) => setUnderlying(e.target.value)}
                        className="bg-[#21262d] border border-[#30363d] rounded px-3 py-1 text-sm text-[#c9d1d9] outline-none hover:bg-[#30363d] transition-colors"
                    >
                        <option value="NIFTY">NIFTY</option>
                        <option value="BANKNIFTY">BANKNIFTY</option>
                        <option value="FINNIFTY">FINNIFTY</option>
                    </select>

                    {/* Expiry Selector */}
                    <select
                        value={expiry}
                        onChange={(e) => setExpiry(e.target.value)}
                        className="bg-[#21262d] border border-[#30363d] rounded px-3 py-1 text-sm text-[#c9d1d9] outline-none hover:bg-[#30363d] transition-colors"
                    >
                        <option value="26-DEC-2024">26-DEC-2024</option>
                        <option value="02-JAN-2025">02-JAN-2025</option>
                        <option value="30-JAN-2025">30-JAN-2025</option>
                    </select>

                    {/* Spot Price */}
                    <div className="text-sm bg-[#161b22] px-3 py-1 rounded border border-[#30363d]">
                        <span className="text-[#8b949e] mr-2">SPOT</span>
                        <span className="text-[#00c176] font-bold font-mono text-lg">{spotPrice.toFixed(2)}</span>
                    </div>
                </div>
            </div>

            {/* Options Chain Table */}
            <div className="bg-[#161b22] border border-[#30363d] rounded overflow-hidden shadow-sm relative">
                {/* Center Line for ATM */}
                <div className="absolute left-1/2 top-0 bottom-0 w-px bg-[#30363d] z-0"></div>

                <table className="w-full text-xs relative z-10">
                    <thead className="bg-[#21262d]">
                        <tr>
                            <th colSpan={5} className="px-2 py-2 text-[#00c176] border-r border-[#30363d] font-bold tracking-wider">CALLS</th>
                            <th className="px-2 py-2 text-[#c9d1d9] bg-[#30363d]">STRIKE</th>
                            <th colSpan={5} className="px-2 py-2 text-[#ff4d4d] border-l border-[#30363d] font-bold tracking-wider">PUTS</th>
                        </tr>
                        <tr className="text-[#8b949e] uppercase border-b border-[#30363d]">
                            <th className="px-2 py-1 text-right w-[8%]">Δ</th>
                            <th className="px-2 py-1 text-right w-[8%]">Γ</th>
                            <th className="px-2 py-1 text-right w-[8%]">Θ</th>
                            <th className="px-2 py-1 text-right w-[8%]">IV</th>
                            <th className="px-2 py-1 text-right border-r border-[#30363d] w-[10%]">LTP</th>
                            <th className="px-2 py-1 text-center w-[8%]"></th>
                            <th className="px-2 py-1 text-right border-l border-[#30363d] w-[10%]">LTP</th>
                            <th className="px-2 py-1 text-right w-[8%]">IV</th>
                            <th className="px-2 py-1 text-right w-[8%]">Θ</th>
                            <th className="px-2 py-1 text-right w-[8%]">Γ</th>
                            <th className="px-2 py-1 text-right w-[8%]">Δ</th>
                        </tr>
                    </thead>
                    <tbody>
                        {options.map((opt, i) => {
                            const isAtm = Math.abs(opt.strike - spotPrice) < 50
                            const isSelected = selectedStrikeIndex === i

                            return (
                                <tr
                                    key={opt.strike}
                                    className={`
                                        border-t border-[#30363d]/50 transition-colors cursor-pointer
                                        ${isSelected ? 'bg-[#30363d]' : isAtm ? 'bg-[#f0c808]/5' : i % 2 === 0 ? 'bg-[#161b22]' : 'bg-[#0d1117]'}
                                        hover:bg-[#21262d]
                                    `}
                                    onClick={() => setSelectedStrikeIndex(i)}
                                >
                                    <td className="px-2 py-1.5 text-right text-[#00c176] opacity-80 font-mono">{opt.ceDelta.toFixed(2)}</td>
                                    <td className="px-2 py-1.5 text-right text-[#8b949e] opacity-60 font-mono">{opt.ceGamma.toFixed(3)}</td>
                                    <td className="px-2 py-1.5 text-right text-[#ff4d4d] opacity-60 font-mono">{opt.ceTheta.toFixed(1)}</td>
                                    <td className="px-2 py-1.5 text-right text-[#8b949e] font-mono">{opt.ceIv.toFixed(1)}</td>
                                    <td className="px-2 py-1.5 text-right text-[#c9d1d9] font-bold border-r border-[#30363d] font-mono bg-opacity-10 bg-white/5">{opt.ceLtp.toFixed(2)}</td>

                                    <td className={`px-4 py-1.5 text-center font-bold font-mono text-sm border-x border-[#30363d] ${isAtm ? 'text-[#f0c808] bg-[#f0c808]/10' : 'text-[#c9d1d9] bg-[#21262d]'}`}>
                                        {opt.strike}
                                    </td>

                                    <td className="px-2 py-1.5 text-right text-[#c9d1d9] font-bold border-l border-[#30363d] font-mono bg-opacity-10 bg-white/5">{opt.peLtp.toFixed(2)}</td>
                                    <td className="px-2 py-1.5 text-right text-[#8b949e] font-mono">{opt.peIv.toFixed(1)}</td>
                                    <td className="px-2 py-1.5 text-right text-[#ff4d4d] opacity-60 font-mono">{opt.peTheta.toFixed(1)}</td>
                                    <td className="px-2 py-1.5 text-right text-[#8b949e] opacity-60 font-mono">{opt.peGamma.toFixed(3)}</td>
                                    <td className="px-2 py-1.5 text-right text-[#ff4d4d] opacity-80 font-mono">{opt.peDelta.toFixed(2)}</td>
                                </tr>
                            )
                        })}
                    </tbody>
                </table>
            </div>

            {/* Footer */}
            <div className="flex items-center justify-between text-xs text-[#6e7681]">
                <div className="flex gap-4">
                    <span>{underlying} · {expiry}</span>
                    <span>ATM: <span className="text-[#f0c808]">{Math.round(spotPrice / 50) * 50}</span></span>
                </div>
                <span>Use <span className="border border-[#30363d] px-1 rounded text-[#c9d1d9]">↑</span> <span className="border border-[#30363d] px-1 rounded text-[#c9d1d9]">↓</span> to navigate</span>
            </div>
        </div>
    )
}
