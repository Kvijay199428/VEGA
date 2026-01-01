import { useEffect, useState } from 'react'
import { httpClient } from '../api/httpClient'

interface TokenTimelineItem {
    apiName: string
    validityAt: string
    status: 'VALID' | 'WARNING' | 'CRITICAL' | 'EXPIRED' | 'ERROR'
    remainingMinutes: number
    durationString: string
}

export function TokenTimeline() {
    const [items, setItems] = useState<TokenTimelineItem[]>([])
    const [loading, setLoading] = useState(true)

    const fetchTimeline = async () => {
        try {
            const response = await httpClient.get<TokenTimelineItem[]>('/api/auth/tokens/timeline')
            setItems(response.data)
        } catch (e) {
            console.error('Failed to fetch timeline', e)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchTimeline()
        const interval = setInterval(fetchTimeline, 60000) // update every minute
        return () => clearInterval(interval)
    }, [])

    if (loading) return <div className="text-xs text-terminal-muted animate-pulse">Scanning tokens...</div>

    return (
        <div className="bg-terminal-card border border-terminal-border p-4 rounded-sm shadow-sm opacity-90 hover:opacity-100 transition-opacity">
            <h3 className="text-terminal-accent text-xs font-bold uppercase mb-4 tracking-wider flex items-center">
                <span className="w-1.5 h-1.5 bg-terminal-accent rounded-full mr-2"></span>
                Token Risk Timeline
            </h3>
            <div className="space-y-3">
                {items.map(item => (
                    <div key={item.apiName} className="flex items-center justify-between text-xs group">
                        <div className="flex items-center space-x-3">
                            <StatusIndicator status={item.status} />
                            <span className="text-gray-300 font-mono w-28 group-hover:text-white transition-colors">
                                {item.apiName}
                            </span>
                        </div>

                        {/* Progress Bar Background */}
                        <div className="flex-1 mx-4 h-1 bg-gray-800 rounded-full overflow-hidden relative">
                            {/* Calculated width based on 24h scale or just status logic? */}
                            {/* Simple visual based on status */}
                            <div
                                className={`h-full ${getStatusBg(item.status)} transition-all duration-1000`}
                                style={{ width: getWidth(item.status, item.remainingMinutes) }}
                            />
                        </div>

                        <div className="flex items-center space-x-3 min-w-[120px] justify-end">
                            <span className="text-gray-500 text-[10px]">{item.validityAt.split(' ')[1]}</span>
                            <span className={`font-bold w-14 text-right ${getStatusColor(item.status)}`}>
                                {item.durationString}
                            </span>
                        </div>
                    </div>
                ))}
            </div>

            <div className="mt-4 pt-3 border-t border-gray-800 flex justify-between text-[10px] text-gray-500">
                <span>Updated: {new Date().toLocaleTimeString()}</span>
                <span>Next refresh: 60s</span>
            </div>
        </div>
    )
}

function StatusIndicator({ status }: { status: string }) {
    const color = getStatusBg(status)
    const pulse = status === 'CRITICAL' || status === 'WARNING' ? 'animate-pulse' : ''
    return <div className={`w-2 h-2 rounded-full ${color} ${pulse} shadow-[0_0_8px_rgba(0,0,0,0.5)]`} />
}

function getStatusColor(status: string) {
    switch (status) {
        case 'VALID': return 'text-terminal-green'
        case 'WARNING': return 'text-terminal-yellow decoration-yellow-500 underline decoration-dashed'
        case 'CRITICAL': return 'text-terminal-red font-black'
        case 'EXPIRED': return 'text-gray-400 line-through'
        default: return 'text-gray-500'
    }
}

function getStatusBg(status: string) {
    switch (status) {
        case 'VALID': return 'bg-terminal-green'
        case 'WARNING': return 'bg-terminal-yellow'
        case 'CRITICAL': return 'bg-terminal-red'
        default: return 'bg-gray-600'
    }
}

function getWidth(status: string, minutes: number): string {
    if (status === 'EXPIRED') return '0%'
    // Max 24 hours (1440 mins)
    // Scale mostly for visual
    if (minutes > 300) return '100%'
    const pct = Math.min(100, Math.max(5, (minutes / 180) * 100)) // Scale based on 3 hours for relevant movement
    return `${pct}%`
}
