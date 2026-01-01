import { useNavigate } from 'react-router-dom'
import { useAuthStatus } from '../hooks/useAuthStatus'
import { useAccountData } from '../hooks/useAccountData'
import { useSystemHealth } from '../hooks/useSystemHealth'
import { usePageTitle } from '../context/PageContext'
import { TokenTimeline } from '../components/TokenTimeline'
import { useState, useEffect, useRef, useCallback } from 'react'
import { httpClient } from '../api/httpClient'

interface AuthProgressEvent {
    api: string
    status: 'STARTED' | 'SUCCESS' | 'FAILED'
    completed: number
    total: number
    timestamp: string
    message?: string
}

/**
 * Account Page - F7 Account transparency & login lifecycle.
 * Acts as the 'Control Room' for authentication progress.
 */
export default function AccountPage() {
    const navigate = useNavigate()
    const { status, loading } = useAuthStatus()
    const { health } = useSystemHealth()
    const { positions } = useAccountData()

    // Set page title in TopBar
    usePageTitle('System Status & Account', 'F7')

    const [cooldownSeconds, setCooldownSeconds] = useState(0)
    const [isGenerating, setIsGenerating] = useState(false)
    const [currentApi, setCurrentApi] = useState<string | null>(null)
    const [error, setError] = useState<string | null>(null)
    const eventSourceRef = useRef<EventSource | null>(null)

    // Cooldown Timer Effect
    useEffect(() => {
        if (status?.cooldownActive && status?.remainingSeconds) {
            setCooldownSeconds(status.remainingSeconds)
        }
    }, [status?.cooldownActive, status?.remainingSeconds])

    useEffect(() => {
        if (cooldownSeconds <= 0) return
        const timer = setInterval(() => {
            setCooldownSeconds(prev => Math.max(0, prev - 1))
        }, 1000)
        return () => clearInterval(timer)
    }, [cooldownSeconds])

    // SSE subscription for real-time progress
    useEffect(() => {
        if (!isGenerating) return

        console.log('[AccountPage SSE] Connecting to auth progress stream...')
        const eventSource = new EventSource('/api/v1/auth/selenium/progress')
        eventSourceRef.current = eventSource

        eventSource.addEventListener('auth-progress', (e) => {
            try {
                const event: AuthProgressEvent = JSON.parse(e.data)
                console.log(`[SSE] ${event.api} -> ${event.status}`, event)
                setCurrentApi(event.status === 'STARTED' ? event.api : null)

                if (event.status === 'FAILED') {
                    setError(`Failed to generate ${event.api} token`)
                }

                // Check if generation is complete
                if (event.completed === event.total && event.status !== 'STARTED') {
                    console.log('[SSE] Token generation complete!')
                    setIsGenerating(false)
                    setCurrentApi(null)
                }
            } catch (err) {
                console.error('[SSE] Parse error', err)
            }
        })

        eventSource.onerror = () => {
            console.log('[SSE] Connection error or closed')
            eventSource.close()
            setIsGenerating(false)
        }

        return () => {
            console.log('[SSE] Closing connection')
            eventSource.close()
        }
    }, [isGenerating])

    // Handle Generate Tokens button click
    const handleGenerateTokens = useCallback(async () => {
        try {
            setIsGenerating(true)
            setError(null)
            setCurrentApi('Initializing...')

            // Trigger async batch login (PRIMARY first, then remaining in background)
            await httpClient.post('/api/v1/auth/selenium/batch-login-async', null, {
                params: { headless: true },
                timeout: 300000 // 5 min timeout
            })

        } catch (err: any) {
            console.error('[GenerateTokens] Error:', err)
            setError(err.response?.data?.message || 'Token generation failed')
            setIsGenerating(false)
        }
    }, [])

    if (loading || !status) {
        return (
            <div className="flex flex-col items-center justify-center h-[calc(100vh-100px)] space-y-4">
                <div className="w-8 h-8 border-2 border-[#00c176] border-t-transparent rounded-full animate-spin"></div>
                <div className="text-[#6e7681] text-sm font-mono animate-pulse">Establishing secure link...</div>
            </div>
        )
    }

    const { state, generatedTokens, requiredTokens, missingApis, primaryReady, fullyReady } = status

    const getStatusColor = (s: string) => {
        switch (s) {
            case 'AUTH_CONFIRMED': return 'text-[#00c176]'
            case 'PRIMARY_VALIDATED': return 'text-[#f0c808]'
            case 'GENERATING_TOKENS': return 'text-[#7ee787]'
            case 'PARTIAL_AUTH': return 'text-[#f0c808]'
            case 'COOLDOWN': return 'text-[#ff4d4d]'
            default: return 'text-[#6e7681]'
        }
    }

    const getHealthIndicator = (statusVal: string) => {
        switch (statusVal) {
            case 'OK':
            case 'SAFE':
            case 'CONNECTED':
                return { color: 'bg-[#00c176]', text: 'text-[#00c176]' }
            case 'LIMITED':
            case 'DEGRADED':
                return { color: 'bg-[#f0c808]', text: 'text-[#f0c808]' }
            case 'DISCONNECTED':
            case 'ERROR':
                return { color: 'bg-[#ff4d4d]', text: 'text-[#ff4d4d]' }
            default:
                return { color: 'bg-[#6e7681]', text: 'text-[#6e7681]' }
        }
    }

    const progressPercent = Math.min(100, (generatedTokens / (requiredTokens || 1)) * 100)
    const needsTokens = generatedTokens < requiredTokens && !fullyReady

    const formatCooldown = (seconds: number) => {
        const mins = Math.floor(seconds / 60)
        const secs = seconds % 60
        return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
    }

    return (
        <div className="space-y-6 max-w-4xl mx-auto py-4">
            {/* Status Bar */}
            <div className="flex justify-between items-center border-b border-[#30363d] pb-4">
                <div className="text-sm text-[#6e7681] font-mono">
                    SESSION: {Math.random().toString(36).substring(7).toUpperCase()}
                </div>
                <div className={`text-sm font-bold font-mono ${fullyReady ? 'text-[#00c176]' : 'text-[#f0c808]'}`}>
                    {fullyReady ? '● OPERATIONAL' : '◐ INITIALIZING'}
                </div>
            </div>

            {/* Token Timeline */}
            <TokenTimeline />

            {/* Error Banner */}
            {error && (
                <div className="bg-[#ff4d4d]/10 border border-[#ff4d4d] rounded-lg p-4 flex items-center gap-3">
                    <div className="w-3 h-3 bg-[#ff4d4d] rounded-full"></div>
                    <div className="text-[#ff4d4d]">{error}</div>
                    <button onClick={() => setError(null)} className="ml-auto text-[#ff4d4d] hover:text-white">✕</button>
                </div>
            )}

            {/* Cooldown Timer Banner */}
            {state === 'COOLDOWN' && cooldownSeconds > 0 && (
                <div className="bg-[#ff4d4d]/10 border border-[#ff4d4d] rounded-lg p-4 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <div className="w-3 h-3 bg-[#ff4d4d] rounded-full animate-pulse"></div>
                        <div>
                            <div className="font-bold text-[#ff4d4d]">API COOLDOWN ACTIVE</div>
                            <div className="text-sm text-[#8b949e]">Rate limit reached. Authentication paused for safety.</div>
                        </div>
                    </div>
                    <div className="text-3xl font-mono font-bold text-[#ff4d4d]">
                        {formatCooldown(cooldownSeconds)}
                    </div>
                </div>
            )}

            {/* Token Generation Banner (when generating) */}
            {isGenerating && currentApi && (
                <div className="bg-[#00c176]/10 border border-[#00c176] rounded-lg p-4 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <div className="w-3 h-3 bg-[#00c176] rounded-full animate-pulse"></div>
                        <div>
                            <div className="font-bold text-[#00c176]">GENERATING TOKENS</div>
                            <div className="text-sm text-[#8b949e]">Authenticating with Upstox API...</div>
                        </div>
                    </div>
                    <div className="text-lg font-mono font-bold text-[#00c176]">
                        {currentApi}
                    </div>
                </div>
            )}

            {/* Main Status Panel */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Auth State & Progress */}
                <div className="bg-[#161b22] border border-[#30363d] rounded-lg p-6 space-y-6">
                    <div>
                        <div className="text-xs text-[#8b949e] uppercase tracking-wider mb-2">Authentication State</div>
                        <div className={`text-xl font-bold font-mono ${getStatusColor(state)}`}>
                            {state.replace('_', ' ')}
                        </div>
                        <div className="text-sm text-[#6e7681] mt-1">
                            {state === 'GENERATING_TOKENS' && "Requesting tokens from Upstox API..."}
                            {state === 'COOLDOWN' && "Rate limit hit. Paused for safety."}
                            {state === 'PRIMARY_VALIDATED' && "Core trading functions enabled."}
                            {state === 'AUTH_CONFIRMED' && "Full system access granted."}
                        </div>
                    </div>

                    <div>
                        <div className="flex justify-between text-sm mb-2">
                            <span className="text-[#c9d1d9]">Token Generation Progress</span>
                            <span className="text-[#8b949e] font-mono">{generatedTokens}/{requiredTokens}</span>
                        </div>
                        <div className="h-2 bg-[#21262d] rounded-full overflow-hidden">
                            <div
                                className="h-full bg-[#00c176] transition-all duration-500 ease-out relative"
                                style={{ width: `${progressPercent}%` }}
                            >
                                <div className="absolute inset-0 bg-white/20 animate-[shimmer_2s_infinite]"></div>
                            </div>
                        </div>
                    </div>

                    {missingApis.length > 0 && (
                        <div className="bg-[#0d1117] rounded border border-[#30363d] p-3">
                            <div className="text-xs text-[#8b949e] mb-2 uppercase">Pending Tokens</div>
                            <div className="flex flex-wrap gap-2">
                                {missingApis.map(api => (
                                    <span
                                        key={api}
                                        className={`text-xs border px-2 py-1 rounded font-mono transition-all ${currentApi === api
                                            ? 'border-[#00c176] bg-[#00c176]/20 text-[#00c176] animate-pulse'
                                            : 'border-[#30363d] bg-[#161b22] text-[#f0c808]'
                                            }`}
                                    >
                                        {currentApi === api ? '⚡ ' : ''}{api}
                                    </span>
                                ))}
                            </div>
                        </div>
                    )}
                </div>

                {/* Account & Controls */}
                <div className="bg-[#161b22] border border-[#30363d] rounded-lg p-6 flex flex-col justify-between">
                    <div className="space-y-4">
                        <div className="text-xs text-[#8b949e] uppercase tracking-wider border-b border-[#30363d] pb-2">User Identity</div>
                        <div className="grid grid-cols-2 gap-4 text-sm">
                            <div>
                                <div className="text-[#8b949e]">User</div>
                                <div className="text-[#c9d1d9] font-bold">Vega Admin</div>
                            </div>
                            <div>
                                <div className="text-[#8b949e]">Role</div>
                                <div className="text-[#c9d1d9]">SYSTEM_ADMIN</div>
                            </div>
                            <div>
                                <div className="text-[#8b949e]">Broker</div>
                                <div className="text-[#c9d1d9]">Upstox Pro</div>
                            </div>
                            <div>
                                <div className="text-[#8b949e]">Total P&L</div>
                                <div className={`font-mono font-bold ${positions.reduce((a, b) => a + b.pnl, 0) >= 0 ? 'text-[#00c176]' : 'text-[#ff4d4d]'}`}>
                                    ₹{positions.reduce((a, b) => a + b.pnl, 0).toFixed(2)}
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="space-y-3 mt-6">
                        {/* Generate Tokens Button */}
                        {needsTokens && !isGenerating && (
                            <button
                                onClick={handleGenerateTokens}
                                className="w-full py-3 rounded text-sm font-bold bg-gradient-to-r from-[#7ee787] to-[#00c176] text-black hover:opacity-90 transition-opacity flex items-center justify-center gap-2"
                            >
                                <span>⚡</span>
                                GENERATE ACCESS TOKENS
                            </button>
                        )}

                        {/* Generating in progress */}
                        {isGenerating && (
                            <div className="w-full py-3 rounded text-sm font-bold bg-[#21262d] text-[#8b949e] border border-[#30363d] flex items-center justify-center gap-2">
                                <div className="w-4 h-4 border-2 border-[#00c176] border-t-transparent rounded-full animate-spin"></div>
                                GENERATING... {currentApi || ''}
                            </div>
                        )}

                        {/* Dashboard Button */}
                        <button
                            disabled={!primaryReady}
                            onClick={() => navigate('/dashboard')}
                            className={`w-full py-2 rounded text-sm font-bold transition-all
                                ${primaryReady
                                    ? 'bg-[#00c176] text-black hover:bg-[#00a866]'
                                    : 'bg-[#21262d] text-[#484f58] cursor-not-allowed border border-[#30363d]'
                                }`}
                        >
                            {primaryReady ? "OPEN TRADING DESK" : "WAITING FOR PRIMARY TOKEN..."}
                        </button>

                        <button
                            onClick={() => {
                                if (confirm("Abort session and logout?")) {
                                    if (eventSourceRef.current) {
                                        eventSourceRef.current.close()
                                    }
                                    alert("Session terminated");
                                    navigate('/login');
                                }
                            }}
                            className="w-full py-2 rounded text-sm text-[#ff4d4d] border border-[#ff4d4d]/30 hover:bg-[#ff4d4d]/10 transition-colors"
                        >
                            EMERGENCY LOGOUT
                        </button>
                    </div>
                </div>
            </div>

            {/* System Health Panel */}
            {health && (
                <div className="bg-[#161b22] border border-[#30363d] rounded-lg p-6">
                    <div className="text-xs text-[#8b949e] uppercase tracking-wider mb-4 border-b border-[#30363d] pb-2">System Health</div>
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        {/* Database */}
                        <div className="flex items-center gap-3">
                            <div className={`w-3 h-3 rounded-full ${getHealthIndicator(health.database.status).color}`}></div>
                            <div>
                                <div className="text-sm font-bold text-[#c9d1d9]">Database</div>
                                <div className={`text-xs ${getHealthIndicator(health.database.status).text}`}>{health.database.status}</div>
                            </div>
                        </div>
                        {/* Cache */}
                        <div className="flex items-center gap-3">
                            <div className={`w-3 h-3 rounded-full ${getHealthIndicator(health.cache.status).color}`}></div>
                            <div>
                                <div className="text-sm font-bold text-[#c9d1d9]">Cache</div>
                                <div className={`text-xs ${getHealthIndicator(health.cache.status).text}`}>{health.cache.status}</div>
                            </div>
                        </div>
                        {/* API */}
                        <div className="flex items-center gap-3">
                            <div className={`w-3 h-3 rounded-full ${getHealthIndicator(health.api.status).color}`}></div>
                            <div>
                                <div className="text-sm font-bold text-[#c9d1d9]">API Rate</div>
                                <div className={`text-xs ${getHealthIndicator(health.api.status).text}`}>{health.api.status}</div>
                            </div>
                        </div>
                        {/* WebSocket */}
                        <div className="flex items-center gap-3">
                            <div className={`w-3 h-3 rounded-full ${getHealthIndicator(health.websocket.status).color}`}></div>
                            <div>
                                <div className="text-sm font-bold text-[#c9d1d9]">WebSocket</div>
                                <div className={`text-xs ${getHealthIndicator(health.websocket.status).text}`}>
                                    {health.websocket.status} ({health.websocket.connections})
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}
