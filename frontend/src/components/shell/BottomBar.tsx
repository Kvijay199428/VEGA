interface BottomBarProps {
    commandInput: string
    onCommandChange: (value: string) => void
    onCommandSubmit: (command: string) => void
}

/**
 * BottomBar - Command line and status bar.
 */
export default function BottomBar({ commandInput, onCommandChange, onCommandSubmit }: BottomBarProps) {
    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault()
        onCommandSubmit(commandInput)
    }

    return (
        <footer className="h-8 bg-[#161b22] border-t border-[#30363d] flex items-center px-4 text-xs">
            {/* Command Input */}
            <form onSubmit={handleSubmit} className="flex items-center gap-2 flex-1">
                <span className="text-[#00c176] font-bold">&gt;</span>
                <input
                    id="command-input"
                    type="text"
                    value={commandInput}
                    onChange={(e) => onCommandChange(e.target.value)}
                    placeholder="Type command... (/) or F1-F8"
                    className="flex-1 bg-transparent border-none outline-none text-[#c9d1d9] placeholder-[#6e7681]"
                    autoComplete="off"
                />
                <span className="text-[#6e7681]">ENTER to execute</span>
            </form>

            {/* Status */}
            <div className="flex items-center gap-4 text-[#8b949e]">
                <span>PORT 28020</span>
                <span>v1.0.0</span>
            </div>
        </footer>
    )
}
