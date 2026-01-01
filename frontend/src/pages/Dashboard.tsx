import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDashboardData } from '../hooks/useDashboardData'
import { useAlertData } from '../hooks/useAlertData'
import { useAuthStatus } from '../hooks/useAuthStatus'
import { usePortfolioKpis } from '../hooks/usePortfolioKpis'
import { usePageTitle } from '../context/PageContext'

/**
 * Dashboard - F1 Home screen with indices, KPIs, and alerts.
 * Bloomberg-style command center.
 */
export default function Dashboard() {
    const navigate = useNavigate()
    const { indices, loading: indicesLoading } = useDashboardData()
    const { alerts } = useAlertData()
    const { status: authStatus } = useAuthStatus()
    const { kpis } = usePortfolioKpis()

    // Set page title in TopBar
    usePageTitle('Dashboard', 'F1', 'HOME <GO>')

    // Redirect to account if PRIMARY not ready
    useEffect(() => {
        if (authStatus && !authStatus.primaryReady) {
            navigate('/account')
        }
    }, [authStatus, navigate])

    if (indicesLoading) {

        return (
            <div className="space-y-6 animate-pulse">
                <div className="h-8 bg-[#161b22] rounded w-64"></div>
                <div className="grid grid-cols-4 gap-4">
                    {[1, 2, 3, 4].map(i => <div key={i} className="h-32 bg-[#161b22] rounded border border-[#30363d]"></div>)}
                </div>
            </div>
        )
    }

    return (
        <div className="space-y-6">
            {/* Indices Row */}
            <div className="grid grid-cols-4 gap-4">
                {indices.map((idx) => (
                    <div
                        key={idx.symbol}
                        className="bg-[#161b22] border border-[#30363d] p-4 rounded hover:border-[#6e7681] transition-colors"
                    >
                        <div className="flex justify-between items-start mb-2">
                            <span className="text-[#8b949e] text-xs font-bold tracking-wide">{idx.name}</span>
                            <span className={`text-xs px-2 py-0.5 rounded font-mono font-bold ${idx.change >= 0
                                ? 'bg-[#00c176]/10 text-[#00c176]'
                                : 'bg-[#ff4d4d]/10 text-[#ff4d4d]'
                                }`}>
                                {idx.change >= 0 ? '▲' : '▼'} {Math.abs(idx.changePercent).toFixed(2)}%
                            </span>
                        </div>
                        <div className="text-2xl font-bold text-[#c9d1d9] font-mono tracking-tight">
                            {idx.ltp.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                        </div>
                        <div className="flex justify-between items-end mt-1">
                            <span className={`text-sm font-mono ${idx.change >= 0 ? 'text-[#00c176]' : 'text-[#ff4d4d]'}`}>
                                {idx.change >= 0 ? '+' : ''}{idx.change.toFixed(2)}
                            </span>
                            <span className="text-[10px] text-[#6e7681]">H: {idx.high.toFixed(0)} L: {idx.low.toFixed(0)}</span>
                        </div>
                    </div>
                ))}
            </div>

            {/* KPI Grid */}
            {kpis && (
                <div className="grid grid-cols-4 gap-4">
                    {/* Net P&L */}
                    <div className="bg-[#161b22] border border-[#30363d] p-4 rounded">
                        <div className="text-xs text-[#8b949e] uppercase tracking-wider mb-1">Net P&L</div>
                        <div className={`text-2xl font-bold font-mono ${kpis.netPnl >= 0 ? 'text-[#00c176]' : 'text-[#ff4d4d]'}`}>
                            ₹{kpis.netPnl.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                        </div>
                        <div className={`text-xs font-mono ${kpis.netPnlPercent >= 0 ? 'text-[#00c176]' : 'text-[#ff4d4d]'}`}>
                            {kpis.netPnlPercent >= 0 ? '+' : ''}{kpis.netPnlPercent.toFixed(2)}%
                        </div>
                    </div>
                    {/* Exposure */}
                    <div className="bg-[#161b22] border border-[#30363d] p-4 rounded">
                        <div className="text-xs text-[#8b949e] uppercase tracking-wider mb-1">Exposure</div>
                        <div className="text-2xl font-bold font-mono text-[#c9d1d9]">
                            ₹{(kpis.netExposure / 1000).toFixed(0)}K
                        </div>
                        <div className="text-xs text-[#6e7681] font-mono">
                            Gross: ₹{(kpis.grossExposure / 1000).toFixed(0)}K
                        </div>
                    </div>
                    {/* Margin */}
                    <div className="bg-[#161b22] border border-[#30363d] p-4 rounded">
                        <div className="text-xs text-[#8b949e] uppercase tracking-wider mb-1">Margin Used</div>
                        <div className="text-2xl font-bold font-mono text-[#c9d1d9]">
                            {kpis.marginUtilization.toFixed(0)}%
                        </div>
                        <div className="text-xs text-[#6e7681] font-mono">
                            ₹{(kpis.marginUsed / 1000).toFixed(0)}K / ₹{((kpis.marginUsed + kpis.marginAvailable) / 1000).toFixed(0)}K
                        </div>
                    </div>
                    {/* Positions */}
                    <div className="bg-[#161b22] border border-[#30363d] p-4 rounded">
                        <div className="text-xs text-[#8b949e] uppercase tracking-wider mb-1">Positions</div>
                        <div className="text-2xl font-bold font-mono text-[#c9d1d9]">
                            {kpis.openPositions}
                        </div>
                        <div className="text-xs text-[#6e7681] font-mono">
                            Day Trades: {kpis.dayTrades}
                        </div>
                    </div>
                </div>
            )}

            {/* Global Alerts Panel */}
            <div className="bg-[#161b22] border border-[#30363d] rounded">
                <div className="p-4 border-b border-[#30363d] flex justify-between items-center">
                    <h2 className="text-sm font-bold text-[#c9d1d9] uppercase flex items-center gap-2">
                        <span>🔔</span> Global Alerts
                    </h2>
                    <span className="text-xs text-[#6e7681]">{alerts.length} Events</span>
                </div>

                <div className="p-2 space-y-2 max-h-[300px] overflow-y-auto custom-scrollbar">
                    {!authStatus?.authenticated && (
                        <div className="p-3 rounded bg-[#f0c808]/10 border-l-2 border-[#f0c808]">
                            <div className="flex justify-between items-start">
                                <span className="text-[#f0c808] font-bold text-xs uppercase">Auth Critical</span>
                                <span className="text-[10px] text-[#8b949e]">Now</span>
                            </div>
                            <div className="text-[#c9d1d9] text-xs mt-1">
                                Trading disabled. Login required to initialize tokens.
                            </div>
                        </div>
                    )}

                    {alerts.slice(0, 10).map(alert => (
                        <div key={alert.id} className="p-3 rounded bg-[#21262d] border-l-2 border-[#30363d] hover:bg-[#2c333e] transition-colors">
                            <div className="flex justify-between items-start">
                                <span className={`font-bold text-xs uppercase ${alert.type === 'CRITICAL' ? 'text-[#ff4d4d]' :
                                    alert.type === 'WARNING' ? 'text-[#f0c808]' :
                                        alert.type === 'SUCCESS' ? 'text-[#00c176]' : 'text-[#8b949e]'
                                    }`}>
                                    {alert.source} • {alert.type}
                                </span>
                                <span className="text-[10px] text-[#8b949e]">{alert.time}</span>
                            </div>
                            <div className="text-[#c9d1d9] text-xs mt-1">
                                {alert.message}
                            </div>
                        </div>
                    ))}

                    {alerts.length === 0 && authStatus?.authenticated && (
                        <div className="text-center text-[#6e7681] text-xs py-8">
                            No recent alerts
                        </div>
                    )}
                </div>
            </div>

            {/* Keyboard Shortcuts Footer */}
            <div className="fixed bottom-0 left-0 w-full bg-[#0d1117] border-t border-[#30363d] p-1 px-4 flex gap-4 text-[10px] text-[#6e7681] z-10 ml-20">
                <span className="hover:text-[#c9d1d9] cursor-pointer">F1 DASHBOARD</span>
                <span className="hover:text-[#c9d1d9] cursor-pointer">F2 MARKET</span>
                <span className="hover:text-[#c9d1d9] cursor-pointer">F3 OPTIONS</span>
                <span className="hover:text-[#c9d1d9] cursor-pointer">F4 SECTORS</span>
                <span className="hover:text-[#c9d1d9] cursor-pointer">F5 ORDERS</span>
                <span className="hover:text-[#c9d1d9] cursor-pointer">F6 RISK</span>
                <span className="hover:text-[#c9d1d9] cursor-pointer">F7 ACCOUNT</span>
                <span className="hover:text-[#c9d1d9] cursor-pointer">F8 SETTINGS</span>
                <span className="hover:text-[#c9d1d9] cursor-pointer">/ COMMAND</span>
            </div>
        </div>
    )
}
