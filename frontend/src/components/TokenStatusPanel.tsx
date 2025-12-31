import { useSessionStore } from '../store/sessionStore'

export function TokenStatusPanel() {
    const { tokens } = useSessionStore()
    const tokenEntries = Object.entries(tokens)

    if (tokenEntries.length === 0) {
        return (
            <div className="p-4 text-terminal-muted text-sm">
                No token data available. Waiting for backend...
            </div>
        )
    }

    return (
        <div className="divide-y divide-terminal-border">
            {tokenEntries.map(([apiName, status]) => (
                <div
                    key={apiName}
                    className="px-4 py-3 flex items-center justify-between hover:bg-terminal-bg/50"
                >
                    <div>
                        <span className="font-medium text-terminal-text">{apiName}</span>
                        {status.expiresAt && (
                            <span className="text-terminal-muted text-xs ml-2">
                                Expires: {new Date(status.expiresAt).toLocaleString()}
                            </span>
                        )}
                    </div>
                    <span
                        className={`px-2 py-0.5 rounded text-xs font-semibold ${status.valid
                                ? 'bg-terminal-success/20 text-terminal-success'
                                : 'bg-terminal-error/20 text-terminal-error'
                            }`}
                    >
                        {status.valid ? 'ACTIVE' : 'EXPIRED'}
                    </span>
                </div>
            ))}
        </div>
    )
}
