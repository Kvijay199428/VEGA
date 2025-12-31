import { useState, useEffect } from 'react'
import { httpClient } from '../api/httpClient'
import { useAccountData } from '../hooks/useAccountData'

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

interface TokenInfo {
    apiName: string
    status: 'valid' | 'expired' | 'pending'
    expiresAt: string
}

const mockTokens: TokenInfo[] = [
    { apiName: 'PRIMARY_API', status: 'valid', expiresAt: '2025-01-01 03:30:00' },
    { apiName: 'MARKET_DATA_WEBSOCKET', status: 'valid', expiresAt: '2025-01-01 03:30:00' },
    { apiName: 'ORDER_UPDATE_WEBSOCKET', status: 'valid', expiresAt: '2025-01-01 03:30:00' },
    { apiName: 'OPTION_CHAIN_FEED', status: 'valid', expiresAt: '2025-01-01 03:30:00' },
    { apiName: 'BACKUP_DATA_STREAM', status: 'pending', expiresAt: '-' },
    { apiName: 'HISTORICAL_DATA_API', status: 'pending', expiresAt: '-' },
]

/**
 * Account Page - F7 Account transparency & login lifecycle.
 */
export default function AccountPage() {
    const { positions, holdings, auditLogs, loading: dataLoading } = useAccountData()
    const [authStatus, setAuthStatus] = useState<AuthStatus | null>(null)
    const [activeTab, setActiveTab] = useState<'positions' | 'holdings'>('positions')
    const [tokens] = useState<TokenInfo[]>(mockTokens)

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

    const getStatusColor = (status: TokenInfo['status']) => {
        switch (status) {
            case 'valid': return 'bg-[#00c176]'
            case 'expired': return 'bg-[#ff4d4d]'
            case 'pending': return 'bg-[#6e7681]'
        }
    }

    if (dataLoading) {
        return (
            <div className="space-y-4 animate-pulse">
                <div className="h-20 bg-[#161b22] rounded w-full border border-[#30363d]"></div>
                <div className="grid grid-cols-2 gap-4">
                    <div className="h-64 bg-[#161b22] rounded border border-[#30363d]"></div>
                    <div className="h-64 bg-[#161b22] rounded border border-[#30363d]"></div>
                </div>
            </div>
        )
    }

    return (
        <div className="space-y-4">
            {/* Header */}
            <div>
                <h1 className="text-xl font-bold text-[#c9d1d9] flex items-center gap-2">
                    Portfolio & Account
                    <span className="text-xs font-normal text-[#6e7681] border border-[#30363d] px-1 rounded">F7</span>
                </h1>
                <p className="text-sm text-[#6e7681]">PORT &lt;GO&gt;</p>
            </div>

            {/* Account Summary */}
            <div className="bg-[#161b22] border border-[#30363d] rounded p-4">
                <div className="grid grid-cols-4 gap-4 text-sm">
                    <div>
                        <span className="text-[#8b949e]">Account: </span>
                        <span className="text-[#c9d1d9] font-bold">UPSTOX_PRO</span>
                    </div>
                    <div>
                        <span className="text-[#8b949e]">Status: </span>
                        <span className={`font-bold ${authStatus?.authenticated ? 'text-[#00c176]' : 'text-[#f0c808]'}`}>
                            {authStatus?.authenticated ? 'AUTHENTICATED' : 'PENDING'}
                        </span>
                    </div>
                    <div>
                        <span className="text-[#8b949e]">APIs Live: </span>
                        <span className="text-[#c9d1d9] font-mono">
                            {authStatus?.generatedTokens ?? 0}/{authStatus?.requiredTokens ?? 6}
                        </span>
                    </div>
                    <div>
                        <span className="text-[#8b949e]">Total P&L: </span>
                        <span className={`font-bold font-mono ${positions.reduce((a, b) => a + b.pnl, 0) >= 0 ? 'text-[#00c176]' : 'text-[#ff4d4d]'}`}>
                            ₹{positions.reduce((a, b) => a + b.pnl, 0).toFixed(2)}
                        </span>
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
                {/* Left Column: Positions & Holdings */}
                <div className="space-y-4">
                    {/* Tabs */}
                    <div className="flex gap-2">
                        <button
                            onClick={() => setActiveTab('positions')}
                            className={`px-4 py-1 text-sm rounded ${activeTab === 'positions' ? 'bg-[#00c176] text-black font-bold' : 'bg-[#21262d] text-[#8b949e]'}`}
                        >
                            Positions
                        </button>
                        <button
                            onClick={() => setActiveTab('holdings')}
                            className={`px-4 py-1 text-sm rounded ${activeTab === 'holdings' ? 'bg-[#00c176] text-black font-bold' : 'bg-[#21262d] text-[#8b949e]'}`}
                        >
                            Holdings
                        </button>
                    </div>

                    <div className="bg-[#161b22] border border-[#30363d] rounded overflow-hidden shadow-sm min-h-[300px]">
                        <table className="w-full text-sm">
                            <thead className="bg-[#21262d]">
                                <tr className="text-left text-[#8b949e] uppercase text-xs">
                                    <th className="px-4 py-2 font-semibold">Symbol</th>
                                    <th className="px-4 py-2 text-right font-semibold">Qty</th>
                                    <th className="px-4 py-2 text-right font-semibold">Avg</th>
                                    <th className="px-4 py-2 text-right font-semibold">LTP</th>
                                    <th className="px-4 py-2 text-right font-semibold">P&L</th>
                                </tr>
                            </thead>
                            <tbody>
                                {activeTab === 'positions' ? (
                                    positions.map((pos, i) => (
                                        <tr key={pos.symbol} className={`border-t border-[#30363d] ${i % 2 === 0 ? 'bg-[#161b22]' : 'bg-[#0d1117]'}`}>
                                            <td className="px-4 py-2">
                                                <div className="font-bold text-[#c9d1d9]">{pos.symbol}</div>
                                                <div className="text-[10px] text-[#6e7681]">{pos.product}</div>
                                            </td>
                                            <td className="px-4 py-2 text-right text-[#c9d1d9] font-mono">{pos.qty}</td>
                                            <td className="px-4 py-2 text-right text-[#c9d1d9] font-mono">{pos.avgPrice.toFixed(2)}</td>
                                            <td className="px-4 py-2 text-right text-[#c9d1d9] font-mono">{pos.ltp.toFixed(2)}</td>
                                            <td className={`px-4 py-2 text-right font-mono font-bold ${pos.pnl >= 0 ? 'text-[#00c176]' : 'text-[#ff4d4d]'}`}>
                                                {pos.pnl.toFixed(2)}
                                            </td>
                                        </tr>
                                    ))
                                ) : (
                                    holdings.map((hold, i) => (
                                        <tr key={hold.symbol} className={`border-t border-[#30363d] ${i % 2 === 0 ? 'bg-[#161b22]' : 'bg-[#0d1117]'}`}>
                                            <td className="px-4 py-2 font-bold text-[#c9d1d9]">{hold.symbol}</td>
                                            <td className="px-4 py-2 text-right text-[#c9d1d9] font-mono">{hold.qty}</td>
                                            <td className="px-4 py-2 text-right text-[#c9d1d9] font-mono">{hold.avgPrice.toFixed(2)}</td>
                                            <td className="px-4 py-2 text-right text-[#c9d1d9] font-mono">{hold.ltp.toFixed(2)}</td>
                                            <td className={`px-4 py-2 text-right font-mono font-bold ${hold.pnl >= 0 ? 'text-[#00c176]' : 'text-[#ff4d4d]'}`}>
                                                {hold.pnl.toFixed(2)}
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* Right Column: Timelines & Audits */}
                <div className="space-y-4">
                    {/* Token Status */}
                    <div className="bg-[#161b22] border border-[#30363d] rounded p-4">
                        <div className="text-sm font-bold text-[#c9d1d9] mb-3 flex justify-between">
                            <span>API Token Status</span>
                            <span className="text-xs font-normal text-[#6e7681]">Auto-Renewal Enabled</span>
                        </div>
                        <div className="space-y-2">
                            {tokens.map((token) => (
                                <div key={token.apiName} className="flex items-center gap-3">
                                    <div className={`w-2 h-2 rounded-full ${token.status === 'valid' ? 'bg-[#00c176]' : 'bg-[#6e7681]'}`}></div>
                                    <span className="flex-1 text-xs text-[#8b949e] font-mono">{token.apiName}</span>
                                    <div className="w-1/3 h-1.5 bg-[#21262d] rounded overflow-hidden">
                                        <div
                                            className={`h-full ${getStatusColor(token.status)}`}
                                            style={{ width: token.status === 'valid' ? '100%' : '5%' }}
                                        />
                                    </div>
                                    <span className="text-[10px] text-[#6e7681] w-8 text-right">
                                        {token.status === 'valid' ? 'OK' : '-'}
                                    </span>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Audit Log */}
                    <div className="bg-[#161b22] border border-[#30363d] rounded p-4 flex flex-col h-[280px]">
                        <div className="text-sm font-bold text-[#c9d1d9] mb-3 border-b border-[#30363d] pb-2">Audit Log</div>
                        <div className="flex-1 overflow-y-auto space-y-2 custom-scrollbar pr-2">
                            {auditLogs.map((log) => (
                                <div key={log.id} className="text-xs font-mono border-l-2 border-[#30363d] pl-2 hover:border-[#6e7681] transition-colors">
                                    <div className="flex justify-between text-[#6e7681]">
                                        <span>{log.time}</span>
                                        <span className={log.status === 'SUCCESS' ? 'text-[#00c176]' : 'text-[#ff4d4d]'}>{log.status}</span>
                                    </div>
                                    <div className="text-[#c9d1d9] mt-0.5">
                                        <span className="text-[#f0c808]">{log.action}: </span>
                                        {log.details}
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>

            {/* DB Lock Warning */}
            {authStatus?.dbLocked && (
                <div className="bg-[#f0c808]/10 border border-[#f0c808] rounded p-4 flex items-start gap-3">
                    <span className="text-2xl">⚠</span>
                    <div>
                        <div className="font-bold text-[#f0c808]">Database Locked</div>
                        <p className="text-sm text-[#8b949e]">
                            {authStatus.pendingInCache} tokens pending write. System is in read-only mode for safety.
                            Do not close the terminal.
                        </p>
                    </div>
                </div>
            )}
        </div>
    )
}
