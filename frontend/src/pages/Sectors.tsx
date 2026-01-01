import { useState } from 'react'
import { useSectorData } from '../hooks/useSectorData'
import { usePageTitle } from '../context/PageContext'

/**
 * Sectors - F4 Macro-to-micro analysis.
 */
export default function Sectors() {
    const { sectors, constituents, loading } = useSectorData()
    const [selectedSector, setSelectedSector] = useState<string | null>('BANK')

    // Set page title in TopBar
    usePageTitle('Sectors', 'F4', 'GRPS <GO>')

    if (loading) {
        return (
            <div className="space-y-4 animate-pulse">
                <div className="h-10 bg-[#161b22] rounded w-full"></div>
                <div className="grid grid-cols-4 gap-3">
                    {[1, 2, 3, 4, 5, 6, 7, 8].map(i => (
                        <div key={i} className="h-24 bg-[#161b22] rounded border border-[#30363d]"></div>
                    ))}
                </div>
            </div>
        )
    }

    const currentConstituents = selectedSector ? constituents[selectedSector] : []

    return (
        <div className="space-y-4">
            {/* Header */}
            <div>
                <h1 className="text-xl font-bold text-[#c9d1d9] flex items-center gap-2">
                    Sector Terminal
                    <span className="text-xs font-normal text-[#6e7681] border border-[#30363d] px-1 rounded">F4</span>
                </h1>
                <p className="text-sm text-[#6e7681]">SECT &lt;GO&gt;</p>
            </div>

            {/* Sector Tiles Grid */}
            <div className="grid grid-cols-4 gap-3">
                {sectors.map((sector) => (
                    <button
                        key={sector.code}
                        onClick={() => setSelectedSector(sector.code)}
                        className={`bg-[#161b22] border rounded p-3 text-left transition-all ${selectedSector === sector.code
                            ? 'border-[#00c176] bg-[#00c176]/10 shadow-[0_0_10px_rgba(0,193,118,0.2)]'
                            : 'border-[#30363d] hover:border-[#6e7681] hover:bg-[#21262d]'
                            }`}
                    >
                        <div className="flex justify-between items-start mb-2">
                            <span className="text-[#c9d1d9] font-bold text-sm tracking-wide">{sector.code}</span>
                            <span className={`text-xs font-bold font-mono ${sector.changePercent >= 0 ? 'text-[#00c176]' : 'text-[#ff4d4d]'}`}>
                                {sector.changePercent >= 0 ? '▲' : '▼'} {Math.abs(sector.changePercent).toFixed(2)}%
                            </span>
                        </div>
                        <div className="text-xs text-[#8b949e] truncate">{sector.name}</div>
                        <div className="text-[10px] text-[#6e7681] mt-2 flex justify-between items-center">
                            <span>Top: {sector.topStock}</span>
                            <span className={sector.topStockChange >= 0 ? 'text-[#00c176]' : 'text-[#ff4d4d]'}>
                                {sector.topStockChange > 0 ? '+' : ''}{sector.topStockChange}%
                            </span>
                        </div>
                    </button>
                ))}
            </div>

            {/* Constituents Table */}
            {selectedSector && (
                <div className="bg-[#161b22] border border-[#30363d] rounded overflow-hidden shadow-sm">
                    <div className="bg-[#21262d] px-4 py-2 border-b border-[#30363d] flex justify-between items-center">
                        <span className="text-sm font-bold text-[#c9d1d9]">
                            {sectors.find(s => s.code === selectedSector)?.name} Constituents
                        </span>
                        <span className="text-xs text-[#6e7681] font-mono">
                            {currentConstituents.length} Instruments
                        </span>
                    </div>
                    <table className="w-full text-sm">
                        <thead className="bg-[#21262d]">
                            <tr className="text-left text-[#8b949e] uppercase text-xs">
                                <th className="px-4 py-2 font-semibold">Symbol</th>
                                <th className="px-4 py-2 font-semibold">Name</th>
                                <th className="px-4 py-2 text-right font-semibold">Weight</th>
                                <th className="px-4 py-2 text-right font-semibold">LTP</th>
                                <th className="px-4 py-2 text-right font-semibold">Change %</th>
                                <th className="px-4 py-2 text-right font-semibold">Volume</th>
                            </tr>
                        </thead>
                        <tbody>
                            {currentConstituents.map((stock, i) => (
                                <tr
                                    key={stock.symbol}
                                    className={`border-t border-[#30363d] transition-colors ${i % 2 === 0 ? 'bg-[#161b22]' : 'bg-[#0d1117]'} hover:bg-[#21262d]`}
                                >
                                    <td className="px-4 py-2 font-medium text-[#c9d1d9]">{stock.symbol}</td>
                                    <td className="px-4 py-2 text-[#8b949e] text-xs">{stock.name}</td>
                                    <td className="px-4 py-2 text-right text-[#8b949e] font-mono">{stock.weight.toFixed(1)}%</td>
                                    <td className="px-4 py-2 text-right text-[#c9d1d9] font-mono font-bold">{stock.ltp.toFixed(2)}</td>
                                    <td className={`px-4 py-2 text-right font-mono ${stock.changePercent >= 0 ? 'text-[#00c176]' : 'text-[#ff4d4d]'}`}>
                                        {stock.changePercent >= 0 ? '+' : ''}{stock.changePercent.toFixed(2)}%
                                    </td>
                                    <td className="px-4 py-2 text-right text-[#8b949e] font-mono">
                                        {(stock.volume / 1000).toFixed(0)}K
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    )
}
