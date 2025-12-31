import { useState, useEffect } from 'react'
import { httpClient } from '../../api/httpClient'

interface AuthStatus {
    requiredTokens: number
    generatedTokens: number
    authenticated: boolean
    inProgress: boolean
    dbLocked: boolean
    pendingInCache: number
}

/**
 * TopBar - Global status bar with clock, market status, connection, account info.
 */
export default function TopBar() {
    const [time, setTime] = useState(new Date())
    const [authStatus, setAuthStatus] = useState<AuthStatus | null>(null)
    const [marketOpen, setMarketOpen] = useState(false)

    // Update clock every second
    useEffect(() => {
        const timer = setInterval(() => setTime(new Date()), 1000)
        return () => clearInterval(timer)
    }, [])

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
        const interval = setInterval(fetchStatus, 5000)
        return () => clearInterval(interval)
    }, [])

    // Check market hours (9:15 AM - 3:30 PM IST, Mon-Fri)
    useEffect(() => {
        const now = new Date()
        const hours = now.getHours()
        const mins = now.getMinutes()
        const day = now.getDay()
        const isWeekday = day >= 1 && day <= 5
        const isMarketHours = (hours > 9 || (hours === 9 && mins >= 15)) &&
            (hours < 15 || (hours === 15 && mins <= 30))
        setMarketOpen(isWeekday && isMarketHours)
    }, [time])

    const formatTime = (d: Date) => {
        return d.toLocaleTimeString('en-IN', {
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
        })
    }

    return (
        <header className="h-10 bg-[#161b22] border-b border-[#30363d] flex items-center justify-between px-4 text-xs">
            {/* Left: Logo and Market Status */}
            <div className="flex items-center gap-4">
                <span className="text-[#00c176] font-bold tracking-wider">VEGA</span>
                <div className="flex items-center gap-2">
                    <span className={`w-2 h-2 rounded-full ${marketOpen ? 'bg-[#00c176]' : 'bg-[#6e7681]'}`}></span>
                    <span className="text-[#8b949e]">
                        NSE {marketOpen ? 'LIVE' : 'CLOSED'}
                    </span>
                </div>
            </div>

            {/* Center: Time */}
            <div className="text-[#c9d1d9] font-mono">
                {formatTime(time)} IST
            </div>

            {/* Right: Connection, Auth Status, Account */}
            <div className="flex items-center gap-4">
                {/* Connection Status */}
                <div className="flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full bg-[#00c176]"></span>
                    <span className="text-[#8b949e]">CONN</span>
                </div>

                {/* Token Status */}
                {authStatus && (
                    <div className="flex items-center gap-2">
                        <span className={`${authStatus.authenticated ? 'text-[#00c176]' : 'text-[#f0c808]'}`}>
                            {authStatus.generatedTokens}/{authStatus.requiredTokens} API
                        </span>
                    </div>
                )}

                {/* DB Lock Warning */}
                {authStatus?.dbLocked && (
                    <span className="text-[#f0c808]">⚠ DB</span>
                )}

                {/* Account */}
                <div className="text-[#8b949e]">
                    UPSTOX
                </div>
            </div>
        </header>
    )
}
