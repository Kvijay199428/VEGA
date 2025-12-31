import { useNavigate } from 'react-router-dom'

interface SidebarProps {
    currentPath: string
}

const navItems = [
    { key: 'F1', label: 'Dashboard', path: '/dashboard', icon: '📊' },
    { key: 'F2', label: 'Market', path: '/market-watch', icon: '📈' },
    { key: 'F3', label: 'Options', path: '/options', icon: '⚡' },
    { key: 'F4', label: 'Sectors', path: '/sectors', icon: '🏢' },
    { key: 'F5', label: 'Orders', path: '/orders', icon: '📋' },
    { key: 'F6', label: 'Risk', path: '/risk', icon: '⚠️' },
    { key: 'F7', label: 'Account', path: '/account', icon: '👤' },
    { key: 'F8', label: 'Settings', path: '/settings', icon: '⚙️' },
]

/**
 * Sidebar - F1-F8 keyboard navigation panel.
 */
export default function Sidebar({ currentPath }: SidebarProps) {
    const navigate = useNavigate()

    return (
        <aside className="w-20 bg-[#161b22] border-r border-[#30363d] flex flex-col py-2">
            {navItems.map((item) => {
                const isActive = currentPath === item.path ||
                    (item.path === '/dashboard' && currentPath === '/')

                return (
                    <button
                        key={item.key}
                        onClick={() => navigate(item.path)}
                        className={`flex flex-col items-center justify-center py-3 px-2 transition-colors ${isActive
                                ? 'bg-[#21262d] text-[#00c176] border-l-2 border-[#00c176]'
                                : 'text-[#8b949e] hover:bg-[#21262d] hover:text-[#c9d1d9]'
                            }`}
                        title={`${item.key}: ${item.label}`}
                    >
                        <span className="text-lg mb-1">{item.icon}</span>
                        <span className="text-[10px] font-medium">{item.key}</span>
                    </button>
                )
            })}

            {/* Spacer */}
            <div className="flex-1" />

            {/* Keyboard hint */}
            <div className="text-center text-[8px] text-[#6e7681] pb-2">
                F1-F8<br />NAV
            </div>
        </aside>
    )
}
