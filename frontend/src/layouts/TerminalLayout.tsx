import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import { useEffect, useState } from 'react'
import TopBar from '../components/shell/TopBar'
import Sidebar from '../components/shell/Sidebar'
import BottomBar from '../components/shell/BottomBar'
import VegaStatusBar from '../components/shell/VegaStatusBar'
import { useKillSwitch } from '../hooks/useKillSwitch'

/**
 * Terminal Layout - Bloomberg-style global shell.
 * Provides persistent navigation and status bars.
 */
export default function TerminalLayout() {
    const navigate = useNavigate()
    const location = useLocation()
    const [commandInput, setCommandInput] = useState('')

    // Terminal Control Plane
    // Global Kill Switch Key Binding (Ctrl+Alt+K)
    useKillSwitch()

    // Keyboard navigation (F1-F8)
    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            // Ignore if typing in input
            if ((e.target as HTMLElement).tagName === 'INPUT') return

            switch (e.key) {
                case 'F1':
                    e.preventDefault()
                    navigate('/dashboard')
                    break
                case 'F2':
                    e.preventDefault()
                    navigate('/market-watch')
                    break
                case 'F3':
                    e.preventDefault()
                    navigate('/options')
                    break
                case 'F4':
                    e.preventDefault()
                    navigate('/sectors')
                    break
                case 'F5':
                    e.preventDefault()
                    navigate('/orders')
                    break
                case 'F6':
                    e.preventDefault()
                    navigate('/risk')
                    break
                case 'F7':
                    e.preventDefault()
                    navigate('/account')
                    break
                case 'F8':
                    e.preventDefault()
                    navigate('/settings')
                    break
                case '/':
                    if (!e.ctrlKey && !e.metaKey) {
                        e.preventDefault()
                        document.getElementById('command-input')?.focus()
                    }
                    break
                case 'Escape':
                    setCommandInput('')
                        ; (document.activeElement as HTMLElement)?.blur()
                    break
            }
        }

        window.addEventListener('keydown', handleKeyDown)
        return () => window.removeEventListener('keydown', handleKeyDown)
    }, [navigate])

    // Handle command input
    const handleCommand = (cmd: string) => {
        const command = cmd.trim().toUpperCase()
        if (!command) return

        // Simple command parsing
        if (command.includes('DASH') || command === 'HOME') navigate('/dashboard')
        else if (command.includes('WATCH') || command === 'MON') navigate('/market-watch')
        else if (command.includes('OPT') || command === 'OMON') navigate('/options')
        else if (command.includes('SECT')) navigate('/sectors')
        else if (command.includes('ORDER') || command === 'EMSX') navigate('/orders')
        else if (command.includes('RISK') || command === 'RMS') navigate('/risk')
        else if (command.includes('PORT') || command === 'ACCOUNT') navigate('/account')
        else if (command.includes('SET')) navigate('/settings')

        setCommandInput('')
    }

    return (
        <div className="h-screen flex flex-col bg-[#0b0f14] text-[#c7ccd1] font-mono overflow-hidden">
            {/* Top Bar */}
            <TopBar />

            {/* TERMINAL STATUS BAR (Bloomberg Style) */}
            <VegaStatusBar />

            {/* Main Content Area */}
            <div className="flex-1 flex overflow-hidden">
                {/* Sidebar Navigation */}
                <Sidebar currentPath={location.pathname} />

                {/* Main Workspace */}
                <main className="flex-1 overflow-auto p-4 bg-[#0d1117]">
                    <Outlet />
                </main>
            </div>

            {/* Bottom Bar */}
            <BottomBar
                commandInput={commandInput}
                onCommandChange={setCommandInput}
                onCommandSubmit={handleCommand}
            />
        </div>
    )
}
