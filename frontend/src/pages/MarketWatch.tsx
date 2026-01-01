import { useState, useEffect } from 'react'
import { useMarketData } from '../hooks/useMarketData'
import { useDepthData } from '../hooks/useDepthData'
import { usePageTitle } from '../context/PageContext'
import { DepthLadder } from '../components/DepthLadder'
import { GreeksPanel } from '../components/GreeksPanel'

/**
 * Market Watch - F2 Live price monitoring.
 */
export default function MarketWatch() {
    const { stocks, loading } = useMarketData()
    const [searchTerm, setSearchTerm] = useState('')
    const [mode, setMode] = useState<'LTPC' | 'FULL'>('LTPC')
    const [selectedInstrument, setSelectedInstrument] = useState<string | null>(null)

    // Fetch depth for selected instrument
    const { depth } = useDepthData(selectedInstrument)

    // Set page title in TopBar
    usePageTitle('Market Watch', 'F2', 'MON <GO>')

    // Keyboard Shortcuts
    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            // Ignore if typing in input
            if (document.activeElement?.tagName === 'INPUT') return

            switch (e.key.toUpperCase()) {
                case 'L': setMode('LTPC'); break;
                case 'F': setMode('FULL'); break;
                case 'ESCAPE':
                    setSearchTerm('');
                    setSelectedInstrument(null);
                    break;
            }
        }
        window.addEventListener('keydown', handleKeyDown)
        return () => window.removeEventListener('keydown', handleKeyDown)
    }, [])

    const filteredStocks = stocks.filter(s =>
        s.symbol.toLowerCase().includes(searchTerm.toLowerCase())
    )

    if (loading) {
        return (
            <div className="space-y-4 animate-pulse">
                <div className="h-10 bg-[#161b22] rounded w-full"></div>
                <div className="h-64 bg-[#161b22] rounded w-full border border-[#30363d]"></div>
            </div>
        )
    }

    return (
        <div className="flex gap-4 h-[calc(100vh-120px)]">
            {/* Main Table Area */}
            <div className="flex-1 space-y-4 overflow-hidden flex flex-col">
                {/* Header */}
                <div className="flex items-center justify-between">
                    <div>
                        <h1 className="text-xl font-bold text-[#c9d1d9] flex items-center gap-2">
                            Market Watch
                            <span className="text-xs font-normal text-[#6e7681] border border-[#30363d] px-1 rounded">F2</span>
                        </h1>
                        <p className="text-sm text-[#6e7681]">MON &lt;GO&gt;</p>
                    </div>
                    <div className="flex items-center gap-4">
                        {/* Mode Selector */}
                        <div className="flex items-center gap-2 text-sm">
                            <span className="text-[#8b949e]">Mode:</span>
                            <button
                                onClick={() => setMode('LTPC')}
                                className={`px-2 py-1 rounded transition-colors ${mode === 'LTPC' ? 'bg-[#00c176] text-black font-bold' : 'bg-[#21262d] text-[#8b949e] hover:bg-[#30363d]'}`}
                            >
                                LTPC <span className="text-[10px] opacity-60 ml-1">(L)</span>
                            </button>
                            <button
                                onClick={() => setMode('FULL')}
                                className={`px-2 py-1 rounded transition-colors ${mode === 'FULL' ? 'bg-[#00c176] text-black font-bold' : 'bg-[#21262d] text-[#8b949e] hover:bg-[#30363d]'}`}
                            >
                                FULL <span className="text-[10px] opacity-60 ml-1">(F)</span>
                            </button>
                        </div>

                        {/* Search */}
                        <input
                            type="text"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            placeholder="Search symbol... (ESC to clear)"
                            className="bg-[#21262d] border border-[#30363d] rounded px-3 py-1 text-sm text-[#c9d1d9] placeholder-[#6e7681] outline-none focus:border-[#00c176] w-64 transition-all"
                        />
                    </div>
                </div>

                {/* Stock Table */}
                <div className="bg-[#161b22] border border-[#30363d] rounded overflow-auto shadow-sm flex-1">
                    <table className="w-full text-sm border-collapse">
                        <thead className="bg-[#21262d] sticky top-0 z-10">
                            <tr className="text-left text-[#8b949e] uppercase text-xs">
                                <th className="px-4 py-3 font-semibold">Symbol</th>
                                <th className="px-4 py-3 text-right font-semibold">LTP</th>
                                <th className="px-4 py-3 text-right font-semibold">Change</th>
                                <th className="px-4 py-3 text-right font-semibold">Chg%</th>
                                <th className="px-4 py-3 text-right font-semibold">Volume</th>
                                {mode === 'FULL' && (
                                    <>
                                        <th className="px-4 py-3 text-right font-semibold">High</th>
                                        <th className="px-4 py-3 text-right font-semibold">Low</th>
                                        <th className="px-4 py-3 text-right font-semibold">OI</th>
                                    </>
                                )}
                            </tr>
                        </thead>
                        <tbody>
                            {filteredStocks.map((stock, i) => (
                                <tr
                                    key={stock.instrumentKey}
                                    onClick={() => setSelectedInstrument(stock.instrumentKey)}
                                    className={`border-t border-[#30363d] hover:bg-[#21262d] cursor-pointer transition-colors 
                                        ${i % 2 === 0 ? 'bg-[#161b22]' : 'bg-[#0d1117]'}
                                        ${selectedInstrument === stock.instrumentKey ? 'bg-[#2a3038] border-l-2 border-l-[#00c176]' : ''}
                                    `}
                                >
                                    <td className="px-4 py-2 font-medium text-[#c9d1d9] border-r border-[#30363d]/30">{stock.symbol}</td>
                                    <td className={`px-4 py-2 text-right font-mono font-bold ${stock.change >= 0 ? 'text-[#00c176]' : 'text-[#ff4d4d]'}`}>
                                        {stock.ltp.toFixed(2)}
                                    </td>
                                    <td className={`px-4 py-2 text-right font-mono ${stock.change >= 0 ? 'text-[#00c176]' : 'text-[#ff4d4d]'}`}>
                                        {stock.change >= 0 ? '+' : ''}{stock.change.toFixed(2)}
                                    </td>
                                    <td className={`px-4 py-2 text-right font-mono ${stock.changePercent >= 0 ? 'text-[#00c176]' : 'text-[#ff4d4d]'}`}>
                                        {stock.changePercent >= 0 ? '+' : ''}{stock.changePercent.toFixed(2)}%
                                    </td>
                                    <td className="px-4 py-2 text-right text-[#8b949e] font-mono">
                                        {(stock.volume / 1000).toFixed(0)}K
                                    </td>
                                    {mode === 'FULL' && (
                                        <>
                                            <td className="px-4 py-2 text-right text-[#8b949e] font-mono">{stock.high.toFixed(2)}</td>
                                            <td className="px-4 py-2 text-right text-[#8b949e] font-mono">{stock.low.toFixed(2)}</td>
                                            <td className="px-4 py-2 text-right text-[#8b949e] font-mono">{stock.oi}</td>
                                        </>
                                    )}
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                {/* Footer */}
                <div className="flex items-center justify-between text-xs text-[#6e7681] px-1">
                    <div className="flex gap-4">
                        <span>{filteredStocks.length} symbols</span>
                        <span>Mode: <span className="text-[#c9d1d9] font-mono">{mode}</span></span>
                    </div>
                    <div className="flex gap-2">
                        <span className="bg-[#21262d] px-1 rounded border border-[#30363d]">L</span>
                        <span className="bg-[#21262d] px-1 rounded border border-[#30363d]">F</span>
                        <span className="hidden md:inline">Shortcuts Active</span>
                    </div>
                </div>
            </div>

            {/* Detail Panel */}
            {selectedInstrument && (
                <div className="w-80 bg-[#0d1117] border border-[#30363d] rounded p-4 shadow-lg flex flex-col gap-4 animate-slide-in-right">
                    <div className="flex justify-between items-center border-b border-[#30363d] pb-2">
                        <h2 className="text-[#c9d1d9] font-bold">{selectedInstrument.split('|')[1]}</h2>
                        <button onClick={() => setSelectedInstrument(null)} className="text-[#8b949e] hover:text-white">✕</button>
                    </div>

                    {/* Depth */}
                    <div>
                        <h3 className="text-xs uppercase text-[#8b949e] font-semibold mb-2">Market Depth</h3>
                        <DepthLadder depth={depth} />
                    </div>

                    {/* Greeks */}
                    <div>
                        <h3 className="text-xs uppercase text-[#8b949e] font-semibold mb-2">Option Greeks</h3>
                        <GreeksPanel instrumentKey={selectedInstrument} />
                    </div>

                    {/* Actions */}
                    <div className="mt-auto grid grid-cols-2 gap-2">
                        <button className="bg-[#00c176] text-black font-bold py-2 rounded hover:bg-[#00a062]">BUY</button>
                        <button className="bg-[#ff4d4d] text-white font-bold py-2 rounded hover:bg-[#cc0000]">SELL</button>
                    </div>
                </div>
            )}
        </div>
    )
}

