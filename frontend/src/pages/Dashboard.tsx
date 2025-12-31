import { useState, useEffect } from 'react'
import { httpClient } from '../api/httpClient'
import { useDashboardData } from '../hooks/useDashboardData'
import { useAlertData } from '../hooks/useAlertData'

interface AuthStatus {
    requiredTokens: number
    generatedTokens: number
    authenticated: boolean
    inProgress: boolean
    currentApi: string
    dbLocked: boolean
    pendingInCache: number
    recoveryInProgress: boolean
}

/**
 * Dashboard - F1 Home screen with indices, token health, alerts.
 */
export default function Dashboard() {
    const { indices, loading: indicesLoading } = useDashboardData()
    const { alerts } = useAlertData()
    const [authStatus, setAuthStatus] = useState<AuthStatus | null>(null)

    // Poll auth status
    useEffect(() => {
        const fetchStatus = async () => {
            try {
                const res = await httpClient.get<AuthStatus>('/api/auth/status')
                setAuthStatus(res.data)
            } catch {
                // Ignore errors
            }
        }
        fetchStatus()
        const interval = setInterval(fetchStatus, 3000)
        return () => clearInterval(interval)
    }, [])

    const tokenProgress = authStatus
        ? (authStatus.generatedTokens / authStatus.requiredTokens) * 100
        : 0

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
            {/* Page Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-xl font-bold text-[#c9d1d9] flex items-center gap-2">
                        Dashboard
                        <span className="text-xs font-normal text-[#6e7681] border border-[#30363d] px-1 rounded">F1</span>
                    </h1>
                    <p className="text-sm text-[#6e7681]">HOME &lt;GO&gt;</p>
                </div>
                <div className="text-sm text-[#6e7681] font-mono">
                    {new Date().toLocaleDateString('en-IN', {
                        weekday: 'long',
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric'
                    })}
                </div>
            </div>

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

            <div className="grid grid-cols-2 gap-6">
                {/* Left Column: System & Token Health */}
                <div className="space-y-6">
                    {/* Token Health Panel */}
                    <div className="bg-[#161b22] border border-[#30363d] p-4 rounded">
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-sm font-bold text-[#c9d1d9] uppercase flex items-center gap-2">
                                <span>🔐</span> Token Health
                            </h2>
                            <span className={`text-xs px-2 py-1 rounded font-bold ${authStatus?.authenticated
                                ? 'bg-[#00c176]/20 text-[#00c176]'
                                : 'bg-[#f0c808]/20 text-[#f0c808]'
                                }`}>
                                {authStatus?.authenticated ? 'READY' : 'PENDING'}
                            </span>
                        </div>

                        {/* Progress Bar */}
                        <div className="mb-4">
                            <div className="flex justify-between text-xs text-[#8b949e] mb-1 font-mono">
                                <span>Access Tokens</span>
                                <span>{authStatus?.generatedTokens ?? 0} / {authStatus?.requiredTokens ?? 6}</span>
                            </div>
                            <div className="h-2 bg-[#21262d] rounded-full overflow-hidden border border-[#30363d]">
                                <div
                                    className="h-full bg-[#00c176] transition-all duration-500 shadow-[0_0_10px_#00c176]"
                                    style={{ width: `${tokenProgress}%` }}
                                />
                            </div>
                        </div>

                        {/* Detailed Status */}
                        <div className="space-y-2 text-xs">
                            <div className="flex justify-between p-2 bg-[#21262d] rounded border border-[#30363d]">
                                <span className="text-[#8b949e]">Current Action</span>
                                <span className="text-[#c9d1d9]">{authStatus?.currentApi || 'Idle'}</span>
                            </div>
                            {authStatus?.inProgress && (
                                <div className="text-[#f0c808] animate-pulse flex items-center gap-2">
                                    <span className="w-2 h-2 rounded-full bg-[#f0c808]"></span>
                                    Authenticating...
                                </div>
                            )}
                            {authStatus?.dbLocked && (
                                <div className="text-[#f0c808] bg-[#f0c808]/10 p-2 rounded border border-[#f0c808]/20 flex items-center gap-2">
                                    <span>⚠</span> DB Locked ({authStatus.pendingInCache} queued)
                                </div>
                            )}
                        </div>
                    </div>

                    {/* System Status Table */}
                    <div className="bg-[#161b22] border border-[#30363d] rounded overflow-hidden">
                        <div className="px-4 py-2 border-b border-[#30363d] bg-[#21262d]">
                            <h2 className="text-sm font-bold text-[#c9d1d9] uppercase">System Status</h2>
                        </div>
                        <table className="w-full text-xs">
                            <tbody>
                                <tr className="border-b border-[#30363d]">
                                    <td className="px-4 py-2 text-[#8b949e]">Backend Server</td>
                                    <td className="px-4 py-2 text-right text-[#00c176] font-mono">PORT 28020 ●</td>
                                </tr>
                                <tr className="border-b border-[#30363d]">
                                    <td className="px-4 py-2 text-[#8b949e]">Database</td>
                                    <td className={`px-4 py-2 text-right font-mono ${authStatus?.dbLocked ? 'text-[#f0c808]' : 'text-[#00c176]'}`}>
                                        {authStatus?.dbLocked ? 'LOCKED' : 'READY'} ●
                                    </td>
                                </tr>
                                <tr className="border-b border-[#30363d]">
                                    <td className="px-4 py-2 text-[#8b949e]">WebSocket Stream</td>
                                    <td className="px-4 py-2 text-right text-[#6e7681] font-mono">STANDBY ○</td>
                                </tr>
                                <tr>
                                    <td className="px-4 py-2 text-[#8b949e]">Environment</td>
                                    <td className="px-4 py-2 text-right text-[#c9d1d9] font-mono">PROD-SIM</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* Right Column: Global Alerts */}
                <div className="bg-[#161b22] border border-[#30363d] rounded flex flex-col h-full">
                    <div className="p-4 border-b border-[#30363d] flex justify-between items-center">
                        <h2 className="text-sm font-bold text-[#c9d1d9] uppercase flex items-center gap-2">
                            <span>🔔</span> Global Alerts
                        </h2>
                        <span className="text-xs text-[#6e7681]">{alerts.length} Events</span>
                    </div>

                    <div className="flex-1 overflow-y-auto p-2 space-y-2 custom-scrollbar max-h-[400px]">
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
