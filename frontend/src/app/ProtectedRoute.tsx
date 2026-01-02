import { Navigate } from 'react-router-dom'
import { ReactNode, useContext } from 'react'
import { AuthContext } from '../context/AuthContext'

interface ProtectedRouteProps {
    children: ReactNode
}

/**
 * Protected Route - enforces authentication via AuthContext.
 * 
 * Behavior:
 * - Loading: Show splash screen
 * - Authenticated: Render children (even if degraded/expiring)
 * - Unauthenticated/Error/Expired: Redirect to login
 */
export function ProtectedRoute({ children }: ProtectedRouteProps) {
    const { status } = useContext(AuthContext)

    // Show loading during WebSocket handshake or initial bootstrap
    if (status === 'loading') {
        return (
            <div className="min-h-screen flex items-center justify-center bg-terminal-bg">
                <div className="text-center">
                    <div className="text-terminal-success text-lg font-bold mb-2">
                        VEGA TRADER
                    </div>
                    <div className="text-terminal-muted text-sm">
                        Establishing secure uplink...
                    </div>
                </div>
            </div>
        )
    }

    // Redirect ONLY if strictly unauthenticated or explicitly expired
    // "Degraded" or low-timer states are considered valid for routing
    if (status === 'unauthenticated' || status === 'expired' || status === 'error') {
        return <Navigate to="/login" replace />
    }

    // AUTH_CONFIRMED, PRIMARY_VALIDATED, etc -> Access Granted
    return <>{children}</>
}

export default ProtectedRoute
