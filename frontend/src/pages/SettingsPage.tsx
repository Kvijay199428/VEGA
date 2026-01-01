import { useState, useEffect } from 'react'
import { httpClient } from '../api/httpClient'
import { usePageTitle } from '../context/PageContext'

interface HealthStatus {
    status: 'UP' | 'DOWN'
    details?: Record<string, unknown>
}

/**
 * Settings Page - F8 Operational control.
 */
export default function SettingsPage() {
    const [theme, setTheme] = useState<'dark' | 'light'>('dark')
    const [health, setHealth] = useState<HealthStatus | null>(null)

    // Set page title in TopBar
    usePageTitle('Settings', 'F8', 'SET <GO>')

    // Fetch health status
    useEffect(() => {
        const fetchHealth = async () => {
            try {
                const res = await httpClient.get<HealthStatus>('/actuator/health')
                setHealth(res.data)
            } catch {
                setHealth({ status: 'DOWN' })
            }
        }
        fetchHealth()
    }, [])

    const shortcuts = [
        { key: 'F1', action: 'Dashboard' },
        { key: 'F2', action: 'Market Watch' },
        { key: 'F3', action: 'Options Chain' },
        { key: 'F4', action: 'Sectors' },
        { key: 'F5', action: 'Orders & Trades' },
        { key: 'F6', action: 'Risk Dashboard' },
        { key: 'F7', action: 'Account' },
        { key: 'F8', action: 'Settings' },
        { key: '/', action: 'Command Line' },
        { key: 'ESC', action: 'Clear / Exit' },
    ]

    return (
        <div className="space-y-4">
            {/* Header */}
            <div>
                <h1 className="text-xl font-bold text-[#c9d1d9]">Settings</h1>
                <p className="text-sm text-[#6e7681]">SET &lt;GO&gt;</p>
            </div>

            <div className="grid grid-cols-2 gap-4">
                {/* Theme */}
                <div className="bg-[#161b22] border border-[#30363d] rounded p-4">
                    <div className="text-sm font-bold text-[#c9d1d9] mb-4">Theme</div>
                    <div className="flex gap-2">
                        <button
                            onClick={() => setTheme('dark')}
                            className={`px-4 py-2 text-sm rounded ${theme === 'dark' ? 'bg-[#00c176] text-black' : 'bg-[#21262d] text-[#8b949e]'}`}
                        >
                            Dark
                        </button>
                        <button
                            onClick={() => setTheme('light')}
                            className={`px-4 py-2 text-sm rounded ${theme === 'light' ? 'bg-[#00c176] text-black' : 'bg-[#21262d] text-[#8b949e]'}`}
                        >
                            Light
                        </button>
                    </div>
                    <p className="text-xs text-[#6e7681] mt-2">Bloomberg-style dark theme recommended</p>
                </div>

                {/* API Status */}
                <div className="bg-[#161b22] border border-[#30363d] rounded p-4">
                    <div className="text-sm font-bold text-[#c9d1d9] mb-4">API Status</div>
                    <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                            <span className="text-[#8b949e]">Backend</span>
                            <span className={health?.status === 'UP' ? 'text-[#00c176]' : 'text-[#ff4d4d]'}>
                                ● {health?.status ?? 'CHECKING...'}
                            </span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-[#8b949e]">WebSocket</span>
                            <span className="text-[#6e7681]">● STANDBY</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-[#8b949e]">Port</span>
                            <span className="text-[#c9d1d9]">28020</span>
                        </div>
                    </div>
                </div>
            </div>

            {/* Keyboard Shortcuts */}
            <div className="bg-[#161b22] border border-[#30363d] rounded p-4">
                <div className="text-sm font-bold text-[#c9d1d9] mb-4">Keyboard Shortcuts</div>
                <div className="grid grid-cols-2 gap-2 text-sm">
                    {shortcuts.map((s) => (
                        <div key={s.key} className="flex items-center gap-2">
                            <kbd className="bg-[#21262d] border border-[#30363d] px-2 py-1 rounded text-xs text-[#c9d1d9] font-mono">
                                {s.key}
                            </kbd>
                            <span className="text-[#8b949e]">{s.action}</span>
                        </div>
                    ))}
                </div>
            </div>

            {/* System Info */}
            <div className="bg-[#161b22] border border-[#30363d] rounded p-4">
                <div className="text-sm font-bold text-[#c9d1d9] mb-4">System Information</div>
                <div className="grid grid-cols-3 gap-4 text-sm">
                    <div>
                        <span className="text-[#8b949e]">Version: </span>
                        <span className="text-[#c9d1d9]">1.0.0</span>
                    </div>
                    <div>
                        <span className="text-[#8b949e]">Environment: </span>
                        <span className="text-[#c9d1d9]">Development</span>
                    </div>
                    <div>
                        <span className="text-[#8b949e]">Database: </span>
                        <span className="text-[#c9d1d9]">SQLite</span>
                    </div>
                </div>
            </div>

            {/* WebSocket Limits */}
            <div className="bg-[#161b22] border border-[#30363d] rounded p-4">
                <div className="text-sm font-bold text-[#c9d1d9] mb-4">WebSocket Limits</div>
                <div className="grid grid-cols-4 gap-4 text-sm">
                    <div className="text-center">
                        <div className="text-[#8b949e] mb-1">LTPC</div>
                        <div className="text-[#c9d1d9] font-bold">5,000</div>
                    </div>
                    <div className="text-center">
                        <div className="text-[#8b949e] mb-1">FULL</div>
                        <div className="text-[#c9d1d9] font-bold">1,500</div>
                    </div>
                    <div className="text-center">
                        <div className="text-[#8b949e] mb-1">OPTION_GREEKS</div>
                        <div className="text-[#c9d1d9] font-bold">2,000</div>
                    </div>
                    <div className="text-center">
                        <div className="text-[#8b949e] mb-1">FULL_D30</div>
                        <div className="text-[#c9d1d9] font-bold">3,000</div>
                    </div>
                </div>
            </div>
        </div>
    )
}
