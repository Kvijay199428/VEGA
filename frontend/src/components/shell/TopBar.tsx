import { useState, useEffect, useRef } from 'react'
import { httpClient } from '../../api/httpClient'
import { usePageContext } from '../../context/PageContext'
import { useTerminalStatus, TERMINAL_COLORS } from '../../hooks/useTerminalStatus'
import { executeCommand, parseCommand } from '../../hooks/commandRouter'
import { AuthPhase } from '../../auth/authPhase'

interface AuthStatus {
    requiredTokens: number
    generatedTokens: number
    authenticated: boolean
    inProgress: boolean
    dbLocked: boolean
    pendingInCache: number
}

/**
 * TopBar - Global Authority Plane (Bloomberg-Style)
 * 
 * Row 1: Header (Identity, Clock, System Status)
 * Row 2: Command/Status Strip (Leader, Auth Phase, Command Input)
 */
export default function TopBar() {
    const [time, setTime] = useState(new Date())
    const [authStatus, setAuthStatus] = useState<AuthStatus | null>(null)
    const [marketOpen, setMarketOpen] = useState(false)
    const { pageInfo } = usePageContext()

    // Terminal Status Logic
    const { authPhase, isLeader, fault } = useTerminalStatus()
    const [command, setCommand] = useState("")
    const commandInputRef = useRef<HTMLInputElement>(null)

    // Update clock every second
    useEffect(() => {
        const timer = setInterval(() => setTime(new Date()), 1000)
        return () => clearInterval(timer)
    }, [])

    // Poll auth status (legacy poll for header stats)
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

    // Check market hours
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

    // Global Key Listener for Command Bar Focus
    useEffect(() => {
        const handler = (e: KeyboardEvent) => {
            if (e.ctrlKey && e.key === "l") {
                e.preventDefault();
                commandInputRef.current?.focus();
            }
        };
        window.addEventListener("keydown", handler);
        return () => window.removeEventListener("keydown", handler);
    }, []);

    const formatTime = (d: Date) => d.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false })
    const formatDate = (d: Date) => d.toLocaleDateString('en-IN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })

    // Determining Auth Color
    let authColor = TERMINAL_COLORS.GREEN;
    if (authPhase === AuthPhase.UNAUTHENTICATED) authColor = TERMINAL_COLORS.RED;
    else if (authPhase === AuthPhase.INITIALIZING || authPhase === AuthPhase.DEGRADED) authColor = TERMINAL_COLORS.AMBER;
    else if (fault.level === "CRITICAL") authColor = TERMINAL_COLORS.RED;
    else if (fault.level === "WARN") authColor = TERMINAL_COLORS.AMBER;

    return (
        <div className="flex flex-col w-full border-b border-[#30363d] bg-[#0d1117]">
            {/* ROW 1: System Header */}
            <header className="h-9 flex items-center justify-between px-4 text-xs bg-[#161b22] border-b border-[#30363d]">
                {/* Left: Product & Market */}
                <div className="flex items-center gap-4">
                    <div className="flex items-center gap-2">
                        <div className={`w-2 h-2 rounded-full ${marketOpen ? 'bg-[#00c176]' : 'bg-[#6e7681]'}`}></div>
                        <span className="text-[#c9d1d9] font-bold tracking-tight">VEGA TERMINAL</span>
                    </div>

                    <div className="flex items-center gap-2 border-l border-[#30363d] pl-4">
                        <span className="text-[#8b949e]">NSE {marketOpen ? 'LIVE' : 'CLOSED'}</span>
                    </div>

                    <div className="flex items-center gap-2 border-l border-[#30363d] pl-4">
                        <span className="text-[#c9d1d9] font-semibold">{pageInfo.title}</span>
                    </div>
                </div>

                {/* Center: Clock */}
                <div className="text-[#8b949e] font-mono text-[10px]">
                    {formatDate(time)} <span className="text-[#c9d1d9] ml-2 text-xs">{formatTime(time)}</span>
                </div>

                {/* Right: Account & Stats */}
                <div className="flex items-center gap-4">
                    {authStatus && (
                        <span className="text-[#8b949e] font-mono">
                            TOKENS: {authStatus.generatedTokens}/{authStatus.requiredTokens}
                        </span>
                    )}
                    <span className="text-[#8b949e]">UPSTOX PRO</span>
                </div>
            </header>

            {/* ROW 2: Command & Status Plane */}
            <div className="h-8 flex items-center px-4 font-mono text-xs bg-[#0d1117] text-[#c9d1d9]">
                {/* 1. Leader Status */}
                <span className="text-[#8b949e] min-w-[80px]">
                    {isLeader ? "[LEADER]" : "[FOLLOWER]"}
                </span>

                {/* 2. Auth Phase */}
                <span className="ml-4 font-bold min-w-[100px]" style={{ color: authColor }}>
                    {authPhase}
                </span>

                {/* 3. Command Input */}
                <span className="ml-4 text-[#8b949e] select-none">{'>'}</span>
                <input
                    ref={commandInputRef}
                    className="ml-2 bg-transparent text-[#e6edf3] outline-none w-96 placeholder-[#30363d] caret-[#c9d1d9]"
                    placeholder="GO <SYMBOL> or DIAG"
                    value={command}
                    onChange={(e) => setCommand(e.target.value)}
                    onKeyDown={(e) => {
                        if (e.key === "Enter") {
                            executeCommand(parseCommand(command));
                            setCommand("");
                        }
                    }}
                />

                {/* 4. Fault Indicator (Right Aligned) */}
                <div className="ml-auto flex items-center gap-2">
                    <span
                        className={`font-bold px-2 py-0.5 rounded-sm ${fault.level === 'CRITICAL' ? 'bg-[#c62828] text-white' :
                            fault.level === 'WARN' ? 'bg-[#f0c808] text-black animate-pulse' :
                                'text-[#00c176]'
                            }`}
                    >
                        {fault.message}
                    </span>
                </div>
            </div>
        </div>
    )
}

