import { useSessionStore } from '../store/sessionStore'
import { TokenStatusPanel } from '../components/TokenStatusPanel'
import httpClient from '../api/httpClient'
import { endpoints } from '../api/endpoints'
import { useState, useEffect } from 'react'
import { startSessionPolling } from '../services/sessionBootstrap'

/**
 * System Readiness Page - Post-login landing.
 * 
 * Shows:
 * - Token validity status
 * - Broker health
 * - Hard-block warning if system not ready
 */
export function SystemReadinessPage() {
    const { brokerHealth, lastUpdated, hardBlock } = useSessionStore()
    const [generating, setGenerating] = useState(false)
    const [genMessage, setGenMessage] = useState<string | null>(null)

    // Start polling when component mounts
    useEffect(() => {
        const cleanup = startSessionPolling()
        return cleanup
    }, [])

    const handleRegenerate = async () => {
        try {
            setGenerating(true)
            setGenMessage(null)
            const res = await httpClient.post(endpoints.tokens.generate, { mode: 'INVALID_ONLY' })
            setGenMessage(`Generated ${res.data?.successCount || 0} tokens`)
        } catch {
            setGenMessage('Token generation failed')
        } finally {
            setGenerating(false)
        }
    }

    const formatTime = (ts: number | null) => {
        if (!ts) return 'Never'
        return new Date(ts).toLocaleTimeString()
    }

    return (
        <div className="p-6 space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-xl font-bold text-terminal-text uppercase tracking-wide">
                        System Readiness
                    </h1>
                    <p className="text-terminal-muted text-sm">
                        Last updated: {formatTime(lastUpdated)}
                    </p>
                </div>
                <div className="flex items-center gap-4">
                    <span
                        className={`px-3 py-1 text-xs font-semibold uppercase ${brokerHealth === 'UP'
                                ? 'bg-terminal-success/20 text-terminal-success'
                                : brokerHealth === 'DOWN'
                                    ? 'bg-terminal-error/20 text-terminal-error'
                                    : 'bg-terminal-warning/20 text-terminal-warning'
                            }`}
                    >
                        Broker: {brokerHealth}
                    </span>
                </div>
            </div>

            {/* Hard Block Warning */}
            {hardBlock && (
                <div className="bg-terminal-error/10 border border-terminal-error p-4">
                    <p className="text-terminal-error font-semibold uppercase">
                        ⚠ System Not Ready – Token or Broker Issue
                    </p>
                    <p className="text-terminal-muted text-sm mt-1">
                        Trading is blocked until all tokens are valid and broker is available.
                    </p>
                </div>
            )}

            {/* Token Status Panel */}
            <div className="border border-terminal-border">
                <div className="px-4 py-3 border-b border-terminal-border flex items-center justify-between bg-terminal-surface">
                    <h2 className="font-semibold text-terminal-text uppercase text-sm">
                        Broker Tokens
                    </h2>
                    <button
                        onClick={handleRegenerate}
                        disabled={generating}
                        className="bg-terminal-accent text-white px-3 py-1 text-xs uppercase hover:bg-terminal-accent/90 disabled:opacity-50"
                    >
                        {generating ? 'Generating...' : 'Regenerate Invalid'}
                    </button>
                </div>
                <TokenStatusPanel />
                {genMessage && (
                    <div className="px-4 py-2 text-sm text-terminal-muted border-t border-terminal-border">
                        {genMessage}
                    </div>
                )}
            </div>

            {/* System Ready */}
            {!hardBlock && (
                <div className="bg-terminal-success/10 border border-terminal-success p-4">
                    <p className="text-terminal-success font-semibold uppercase">
                        ✓ System Ready – All Tokens Valid
                    </p>
                    <p className="text-terminal-muted text-sm mt-1">
                        You may proceed to trading dashboards.
                    </p>
                </div>
            )}
        </div>
    )
}

export default SystemReadinessPage
