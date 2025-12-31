import { useState, useEffect, useRef, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { httpClient } from '../api/httpClient'

interface AuthStatus {
    // Renamed from requiredTokens → configuredApis
    configuredApis: number
    generatedTokens: number
    authenticated: boolean
    inProgress: boolean
    currentApi: string
    // Database lock fields
    dbLocked: boolean
    pendingInCache: number
    recoveryInProgress: boolean
    // Valid tokens and missing APIs
    validTokens: string[]
    missingApis: string[]
    // Rate limit cooldown
    rateLimited: boolean
    cooldownEndsAt: number | null
    cooldownMessage: string | null
    // New fields for proper auth gating
    primaryReady: boolean        // PRIMARY token is valid
    fullyReady: boolean          // ALL tokens valid
    canProceed: boolean          // Can access dashboard
    canGenerateRemaining: boolean // Has missing tokens
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
let _pollGuard = false // Module-level guard (used by startedRef)

export function LoginPage() {
    const navigate = useNavigate()
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const [status, setStatus] = useState<AuthStatus | null>(null)
    const [cooldownRemaining, setCooldownRemaining] = useState<string | null>(null)
    const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null)
    const isMountedRef = useRef(true)
    const startedRef = useRef(false)

    // Poll frequencies
    const POLL_FAST = 2000   // When not ready
    const POLL_SLOW = 10000  // When stable (primaryReady && !inProgress)

    // Stop polling safely
    const stopPolling = useCallback(() => {
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current)
            timeoutRef.current = null
        }
    }, [])

    // Calculate remaining cooldown time
    const updateCooldownTimer = useCallback((endsAt: number) => {
        const remaining = endsAt - Date.now()
        if (remaining <= 0) {
            setCooldownRemaining(null)
            return
        }
        const mins = Math.floor(remaining / 60000)
        const secs = Math.floor((remaining % 60000) / 1000)
        setCooldownRemaining(`${mins}m ${secs.toString().padStart(2, '0')}s`)
    }, [])

    // Poll auth status - NO AUTO-REDIRECT (user action only)
    const pollStatus = useCallback(async () => {
        if (!isMountedRef.current) return

        try {
            const response = await httpClient.get<AuthStatus>('/api/auth/status')
            if (!isMountedRef.current) return

            setStatus(response.data)

            // Update cooldown timer
            if (response.data.rateLimited && response.data.cooldownEndsAt) {
                updateCooldownTimer(response.data.cooldownEndsAt)
            } else {
                setCooldownRemaining(null)
            }

            // REMOVED: Auto-redirect - navigation only happens on explicit user action
            // if (response.data.authenticated && !response.data.canGenerateRemaining) { navigate(...) }

            // Dynamic poll frequency: slow when stable, fast when not ready
            const pollInterval = (response.data.primaryReady && !response.data.inProgress)
                ? POLL_SLOW
                : POLL_FAST

            // Schedule next poll
            if (isMountedRef.current) {
                timeoutRef.current = setTimeout(pollStatus, pollInterval)
            }
        } catch (err) {
            console.error('Error polling status:', err)
            // Retry after delay on error
            if (isMountedRef.current) {
                timeoutRef.current = setTimeout(pollStatus, 5000)
            }
        }
    }, [updateCooldownTimer])

    useEffect(() => {
        // StrictMode guard: only start polling once
        if (startedRef.current) return
        startedRef.current = true
        isMountedRef.current = true

        // Initial poll with small delay
        const initialDelay = setTimeout(pollStatus, 100)

        return () => {
            isMountedRef.current = false
            clearTimeout(initialDelay)
            stopPolling()
        }
    }, [pollStatus, stopPolling])

    const handleBatchLogin = async () => {
        try {
            setLoading(true)
            setError(null)

            // Trigger batch login for all 6 APIs
            await httpClient.post('/api/v1/auth/selenium/batch-login', {
                headless: false // Visible Chrome for debugging
            }, {
                timeout: 300000 // 5 minute timeout for 6 APIs
            })

        } catch (err: any) {
            setError(err.response?.data?.message || 'Batch login failed')
        } finally {
            setLoading(false)
            // Final status check
            pollStatus()
        }
    }

    // Explicit Proceed with Primary (Navigates immediately)
    const handleProceedPrimary = useCallback(async () => {
        try {
            stopPolling();
            await httpClient.post('/api/v1/auth/selenium/proceed-primary');
            navigate('/dashboard', { replace: true });
        } catch (err) {
            console.error('Failed to proceed with primary:', err);
            // Resume polling if failed?
        }
    }, [stopPolling, navigate]);

    // Generate Remaining Tokens (Background, stays on page)
    const handleGenerateRemaining = useCallback(async () => {
        try {
            setLoading(true);
            await httpClient.post('/api/v1/auth/selenium/batch-login?headless=true');
            // Polling will update progress
        } catch (err) {
            console.error('Failed to generate remaining:', err);
            setError('Generation failed. Check logs.');
            setLoading(false);
        }
    }, []);

    // Handle explicit user action to proceed to dashboard
    const handleProceedToDashboard = () => {
        stopPolling()
        navigate('/dashboard', { replace: true })
    }

    const progress = status ? status.generatedTokens : 0
    const total = status ? status.configuredApis : 6
    const progressPercent = total > 0 ? (progress / total) * 100 : 0

    return (
        <div className="min-h-screen flex items-center justify-center bg-terminal-bg relative overflow-hidden">
            {/* Background Decor */}
            <div className="absolute inset-0 grid grid-cols-12 gap-4 pointer-events-none opacity-5">
                {Array.from({ length: 12 }).map((_, i) => (
                    <div key={i} className="h-full border-r border-terminal-success/30" />
                ))}
            </div>

            <div className="w-full max-w-md z-10 p-1">
                <div className="bg-terminal-surface border border-terminal-border p-8 shadow-2xl relative">
                    {/* Corner accents */}
                    <div className="absolute top-0 left-0 w-2 h-2 border-t-2 border-l-2 border-terminal-accent" />
                    <div className="absolute top-0 right-0 w-2 h-2 border-t-2 border-r-2 border-terminal-accent" />
                    <div className="absolute bottom-0 left-0 w-2 h-2 border-b-2 border-l-2 border-terminal-accent" />
                    <div className="absolute bottom-0 right-0 w-2 h-2 border-b-2 border-r-2 border-terminal-accent" />

                    <div className="text-center mb-8">
                        <h1 className="text-3xl font-bold text-terminal-success mb-2 tracking-wider">
                            VEGA TRADER
                        </h1>
                        <p className="text-terminal-muted text-sm">MULTI-API AUTHENTICATION</p>
                    </div>

                    {/* Rate Limit Cooldown Banner */}
                    {status?.rateLimited && (
                        <div className="mb-6 bg-amber-500/10 border border-amber-500 p-4 text-amber-400">
                            <div className="flex items-center gap-2 mb-2">
                                <span className="text-lg">⏳</span>
                                <span className="font-bold uppercase">Rate Limit Cooldown Active</span>
                            </div>
                            <p className="text-sm">
                                API generation halted to prevent ban.
                                {cooldownRemaining && (
                                    <span className="block text-xl font-mono mt-2 animate-pulse">
                                        Resume in: {cooldownRemaining}
                                    </span>
                                )}
                            </p>
                        </div>
                    )}

                    {/* Database Lock Warning Banner */}
                    {status?.dbLocked && (
                        <div className="mb-6 bg-amber-500/10 border border-amber-500 p-4 text-amber-400 text-sm">
                            <div className="flex items-center gap-2 mb-2">
                                <span className="text-lg">⚠</span>
                                <span className="font-bold uppercase">Database Temporarily Locked</span>
                            </div>
                            <p className="text-xs opacity-80">
                                {status.pendingInCache} token(s) safely held in memory.
                                {status.recoveryInProgress && (
                                    <span className="ml-1 animate-pulse">Auto-recovery in progress...</span>
                                )}
                            </p>
                        </div>
                    )}

                    {/* Valid Tokens Display */}
                    {status?.validTokens && status.validTokens.length > 0 && !status.fullyReady && (
                        <div className="mb-6 bg-terminal-success/10 border border-terminal-success p-4">
                            <div className="flex items-center gap-2 mb-2">
                                <span className="text-terminal-success">✓</span>
                                <span className="text-terminal-success font-bold text-sm uppercase">
                                    {status.validTokens.length} Valid Token{status.validTokens.length > 1 ? 's' : ''} Available
                                </span>
                            </div>
                            <div className="flex flex-wrap gap-2 text-xs">
                                {status.validTokens.map(token => (
                                    <span key={token} className="bg-terminal-success/20 text-terminal-success px-2 py-1 rounded">
                                        {token}
                                    </span>
                                ))}
                            </div>
                            {status.missingApis && status.missingApis.length > 0 && (
                                <div className="mt-2 text-xs text-terminal-muted">
                                    Missing: {status.missingApis.join(', ')}
                                </div>
                            )}
                        </div>
                    )}

                    {/* Progress Display */}
                    <div className="mb-6 p-4 bg-terminal-bg border border-terminal-border">
                        <div className="flex justify-between items-center mb-2">
                            <span className="text-terminal-muted text-sm uppercase">Access Tokens</span>
                            <span className="text-terminal-success font-bold text-lg">
                                {progress} / {total}
                            </span>
                        </div>
                        <div className="h-2 bg-terminal-border rounded-full overflow-hidden">
                            <div
                                className="h-full bg-terminal-success transition-all duration-500"
                                style={{ width: `${progressPercent}%` }}
                            />
                        </div>
                        {status?.inProgress && status.currentApi && (
                            <div className="mt-2 text-terminal-accent text-xs animate-pulse">
                                ▶ Authenticating: {status.currentApi}
                            </div>
                        )}
                    </div>

                    {error && (
                        <div className="mb-6 bg-terminal-error/10 border border-terminal-error p-3 text-terminal-error text-sm text-center uppercase">
                            ⚠ {error}
                        </div>
                    )}

                    <div className="space-y-3">
                        {/* Button: PROCEED TO DASHBOARD (Fully Ready) */}
                        {status?.fullyReady ? (
                            <button
                                onClick={handleProceedToDashboard}
                                className="w-full bg-terminal-success/20 border border-terminal-success text-terminal-success py-4 font-bold uppercase tracking-widest hover:bg-terminal-success hover:text-white transition-all duration-300 shadow-[0_0_15px_rgba(34,197,94,0.3)]"
                            >
                                PROCEED TO DASHBOARD
                            </button>
                        ) : status?.primaryReady ? (
                            /* Primary Ready - Split Actions */
                            <div className="space-y-3">
                                <button
                                    onClick={handleProceedPrimary}
                                    className="w-full bg-terminal-success/10 border border-terminal-success text-terminal-success py-4 font-bold uppercase tracking-widest hover:bg-terminal-success hover:text-white transition-all duration-300"
                                >
                                    PROCEED WITH PRIMARY
                                </button>
                                {status.canGenerateRemaining && !status.rateLimited && (
                                    <button
                                        onClick={handleGenerateRemaining}
                                        disabled={loading || status?.inProgress}
                                        className="w-full bg-terminal-accent/10 border border-terminal-accent text-terminal-accent py-3 font-bold uppercase tracking-wider hover:bg-terminal-accent hover:text-white transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed text-sm"
                                    >
                                        {loading || status?.inProgress
                                            ? `GENERATING...`
                                            : `GENERATE REMAINING ${status.missingApis?.length || 0} TOKENS`}
                                    </button>
                                )}
                            </div>
                        ) : (
                            /* Initial Login */
                            <button
                                onClick={handleBatchLogin}
                                disabled={loading || status?.inProgress || status?.rateLimited}
                                className="w-full bg-terminal-accent/10 border border-terminal-accent text-terminal-accent py-4 font-bold uppercase tracking-widest hover:bg-terminal-accent hover:text-white transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                {loading || status?.inProgress
                                    ? `AUTHENTICATING...`
                                    : `INITIATE SECURE LOGIN (${total} APIs)`}
                            </button>
                        )}
                    </div>
                </div>

                <div className="mt-4 text-center text-xs text-terminal-muted opacity-50 font-mono">
                    UPSTOX V2 API · PORT 28020 · SECURE
                </div>
            </div>
        </div>
    )
}

export default LoginPage
