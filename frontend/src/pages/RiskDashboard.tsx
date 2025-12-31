import { useRiskData } from '../hooks/useRiskData'

/**
 * Risk Dashboard - F6 Capital protection and exposure control.
 */
export default function RiskDashboard() {
    const { metrics, sectorExposure, loading } = useRiskData()

    if (loading) {
        return (
            <div className="space-y-4 animate-pulse">
                <div className="h-10 bg-[#161b22] rounded w-full"></div>
                <div className="grid grid-cols-4 gap-4">
                    {[1, 2, 3, 4].map(i => (
                        <div key={i} className="h-24 bg-[#161b22] rounded border border-[#30363d]"></div>
                    ))}
                </div>
                <div className="grid grid-cols-2 gap-4">
                    <div className="h-48 bg-[#161b22] rounded border border-[#30363d]"></div>
                    <div className="h-48 bg-[#161b22] rounded border border-[#30363d]"></div>
                </div>
            </div>
        )
    }

    return (
        <div className="space-y-4">
            {/* Header */}
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-xl font-bold text-[#c9d1d9] flex items-center gap-2">
                        Risk Dashboard
                        <span className="text-xs font-normal text-[#6e7681] border border-[#30363d] px-1 rounded">F6</span>
                    </h1>
                    <p className="text-sm text-[#6e7681]">RMS &lt;GO&gt;</p>
                </div>
                <div className="text-right">
                    <div className="text-xs text-[#8b949e]">Value at Risk (99%)</div>
                    <div className="text-lg font-mono font-bold text-[#ff4d4d]">₹{metrics.var99.toLocaleString()}</div>
                </div>
            </div>

            {/* Portfolio Greeks */}
            <div className="grid grid-cols-4 gap-4">
                <div className="bg-[#161b22] border border-[#30363d] rounded p-4 relative overflow-hidden group">
                    <div className="absolute right-0 top-0 p-2 opacity-10 text-4xl font-bold">Δ</div>
                    <div className="text-[#8b949e] text-sm mb-1 font-semibold">Net Delta</div>
                    <div className={`text-2xl font-bold font-mono tracking-tight ${metrics.netDelta >= 0 ? 'text-[#00c176]' : 'text-[#ff4d4d]'}`}>
                        {metrics.netDelta >= 0 ? '+' : ''}{metrics.netDelta.toFixed(1)}
                    </div>
                    <div className="text-xs text-[#6e7681] mt-1">Directional Risk</div>
                </div>
                <div className="bg-[#161b22] border border-[#30363d] rounded p-4 relative overflow-hidden">
                    <div className="absolute right-0 top-0 p-2 opacity-10 text-4xl font-bold">Γ</div>
                    <div className="text-[#8b949e] text-sm mb-1 font-semibold">Net Gamma</div>
                    <div className="text-2xl font-bold text-[#c9d1d9] font-mono tracking-tight">
                        {metrics.netGamma.toFixed(3)}
                    </div>
                    <div className="text-xs text-[#6e7681] mt-1">Acceleration Risk</div>
                </div>
                <div className="bg-[#161b22] border border-[#30363d] rounded p-4 relative overflow-hidden">
                    <div className="absolute right-0 top-0 p-2 opacity-10 text-4xl font-bold">ν</div>
                    <div className="text-[#8b949e] text-sm mb-1 font-semibold">Net Vega</div>
                    <div className="text-2xl font-bold text-[#f0c808] font-mono tracking-tight">
                        {metrics.netVega.toFixed(1)}
                    </div>
                    <div className="text-xs text-[#6e7681] mt-1">Volatility Risk</div>
                </div>
                <div className="bg-[#161b22] border border-[#30363d] rounded p-4 relative overflow-hidden">
                    <div className="absolute right-0 top-0 p-2 opacity-10 text-4xl font-bold">Θ</div>
                    <div className="text-[#8b949e] text-sm mb-1 font-semibold">Net Theta</div>
                    <div className="text-2xl font-bold text-[#00c176] font-mono tracking-tight">
                        {metrics.netTheta >= 0 ? '+' : ''}{metrics.netTheta.toFixed(1)}
                    </div>
                    <div className="text-xs text-[#6e7681] mt-1">Time Decay (Daily)</div>
                </div>
            </div>

            {/* Margin & Sector Exposure */}
            <div className="grid grid-cols-2 gap-4">
                {/* Margin Utilization */}
                <div className="bg-[#161b22] border border-[#30363d] rounded p-5 shadow-sm">
                    <div className="flex justify-between items-center mb-4">
                        <div className="text-sm font-bold text-[#c9d1d9]">Margin Utilization</div>
                        <div className={`text-xs px-2 py-0.5 rounded font-bold ${metrics.marginUsed > 80 ? 'bg-red-500/20 text-red-500' : 'bg-green-500/20 text-green-500'}`}>
                            {metrics.marginUsed > 80 ? 'CRITICAL' : 'SAFE'}
                        </div>
                    </div>
                    <div className="mb-4">
                        <div className="flex justify-between text-sm text-[#8b949e] mb-2 font-mono">
                            <span>Used: {metrics.marginUsed.toFixed(1)}%</span>
                            <span>Free: {metrics.marginAvailable.toLocaleString()}</span>
                        </div>
                        <div className="h-4 bg-[#21262d] rounded-full overflow-hidden border border-[#30363d]">
                            <div
                                className={`h-full transition-all duration-1000 ${metrics.marginUsed > 80 ? 'bg-[#ff4d4d]' : metrics.marginUsed > 50 ? 'bg-[#f0c808]' : 'bg-[#00c176]'}`}
                                style={{ width: `${metrics.marginUsed}%` }}
                            />
                        </div>
                    </div>
                    <div className="flex justify-between text-xs text-[#6e7681] font-mono">
                        <span>Used: ₹{(metrics.marginTotal - metrics.marginAvailable).toLocaleString()}</span>
                        <span>Total: ₹{metrics.marginTotal.toLocaleString()}</span>
                    </div>
                </div>

                {/* Sector Exposure */}
                <div className="bg-[#161b22] border border-[#30363d] rounded p-5 shadow-sm">
                    <div className="flex justify-between items-center mb-4">
                        <div className="text-sm font-bold text-[#c9d1d9]">Sector Exposure</div>
                        <div className="text-xs text-[#6e7681]">Max Limit 40%</div>
                    </div>
                    <div className="space-y-3">
                        {sectorExposure.map((s) => (
                            <div key={s.sector} className="flex items-center gap-3">
                                <div className="w-20 text-xs text-[#8b949e] font-serif tracking-wide">{s.sector}</div>
                                <div className="flex-1 h-2 bg-[#21262d] rounded overflow-hidden">
                                    <div
                                        className={`h-full ${s.exposure > s.limit ? 'bg-[#ff4d4d]' : 'bg-[#00c176]'}`}
                                        style={{ width: `${s.exposure}%` }}
                                    />
                                </div>
                                <div className="w-12 text-xs text-[#8b949e] text-right font-mono">{s.exposure}%</div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            {/* Risk Alerts */}
            <div className="bg-[#161b22] border border-[#30363d] rounded p-4">
                <div className="text-sm font-bold text-[#c9d1d9] mb-4 flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full bg-[#f0c808] animate-pulse"></span>
                    Live Risk Alerts
                </div>
                <div className="space-y-2 text-sm font-mono">
                    {metrics.marginUsed > 80 && (
                        <div className="flex items-center gap-3 text-[#ff4d4d] bg-[#ff4d4d]/5 p-2 rounded border border-[#ff4d4d]/20">
                            <span className="text-lg">⚠</span>
                            <span>Margin utilization critical ({metrics.marginUsed.toFixed(1)}%)</span>
                        </div>
                    )}
                    {sectorExposure.some(s => s.exposure > s.limit) && (
                        <div className="flex items-center gap-3 text-[#f0c808] bg-[#f0c808]/5 p-2 rounded border border-[#f0c808]/20">
                            <span className="text-lg">⚠</span>
                            <span>Sector exposure limit breached for {sectorExposure.find(s => s.exposure > s.limit)?.sector}</span>
                        </div>
                    )}
                    <div className="flex items-center gap-3 text-[#00c176] bg-[#00c176]/5 p-2 rounded border border-[#00c176]/20">
                        <span className="text-lg">✓</span>
                        <span>Portfolio delta within acceptable limits</span>
                    </div>
                </div>
            </div>
        </div>
    )
}
