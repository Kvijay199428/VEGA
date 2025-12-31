import { ReactNode } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { TokenStatusPanel } from '../components/TokenStatusPanel'
import { useSessionStore } from '../store/sessionStore'

interface TerminalShellProps {
    children: ReactNode
}

export function TerminalShell({ children }: TerminalShellProps) {
    const location = useLocation()
    const { brokerHealth, hardBlock } = useSessionStore()

    const navItems = [
        { path: '/', label: 'SYSTEM', icon: '◉' },
        { path: '/orders', label: 'ORDERS', icon: '◈' },
        { path: '/options', label: 'OPTIONS', icon: '◇' },
        { path: '/admin', label: 'ADMIN', icon: '◆' }
    ]

    return (
        <div className="h-screen flex bg-terminal-bg text-terminal-text overflow-hidden">
            {/* Left Sidebar */}
            <aside className="w-60 border-r border-terminal-border flex flex-col">
                {/* Logo */}
                <div className="p-4 border-b border-terminal-border">
                    <h1 className="text-lg font-bold text-terminal-success">VEGA TRADER</h1>
                    <p className="text-terminal-muted text-xs">Terminal v1.0</p>
                </div>

                {/* Navigation */}
                <nav className="flex-1 py-2">
                    {navItems.map((item) => (
                        <Link
                            key={item.path}
                            to={item.path}
                            className={`flex items-center gap-2 px-4 py-2 text-sm hover:bg-terminal-surface ${location.pathname === item.path
                                    ? 'bg-terminal-surface text-terminal-success border-l-2 border-terminal-success'
                                    : 'text-terminal-muted'
                                }`}
                        >
                            <span>{item.icon}</span>
                            <span>{item.label}</span>
                        </Link>
                    ))}
                </nav>

                {/* Token Status Summary */}
                <div className="border-t border-terminal-border">
                    <div className="p-3 text-xs">
                        <div className="flex items-center justify-between mb-2">
                            <span className="text-terminal-muted">BROKER</span>
                            <span
                                className={
                                    brokerHealth === 'UP'
                                        ? 'text-terminal-success'
                                        : brokerHealth === 'DOWN'
                                            ? 'text-terminal-error'
                                            : 'text-terminal-warning'
                                }
                            >
                                {brokerHealth}
                            </span>
                        </div>
                        <div className="flex items-center justify-between">
                            <span className="text-terminal-muted">SYSTEM</span>
                            <span
                                className={
                                    hardBlock ? 'text-terminal-error' : 'text-terminal-success'
                                }
                            >
                                {hardBlock ? 'BLOCKED' : 'READY'}
                            </span>
                        </div>
                    </div>
                    <TokenStatusPanel />
                </div>
            </aside>

            {/* Main Content */}
            <main className="flex-1 overflow-auto">{children}</main>
        </div>
    )
}
