import { Navigate } from 'react-router-dom'
import { ReactNode } from 'react'
import { useAuthStore } from '../auth/authStore'

interface ProtectedRouteProps {
    children: ReactNode
}

/**
 * Protected Route - enforces authentication.
 * 
 * Behavior:
 * - While loading: show loading indicator
 * - If authenticated: render children
 * - If not authenticated: redirect to /login
 */
export function ProtectedRoute({ children }: ProtectedRouteProps) {
    const { authenticated, loading } = useAuthStore()

    // Show loading during bootstrap
    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-terminal-bg">
                <div className="text-center">
                    <div className="text-terminal-success text-lg font-bold mb-2">
                        VEGA TRADER
                    </div>
                    <div className="text-terminal-muted text-sm">
                        Verifying session...
                    </div>
                </div>
            </div>
        )
    }

    // Redirect if not authenticated
    if (!authenticated) {
        return <Navigate to="/login" replace />
    }

    return <>{children}</>
}

export default ProtectedRoute
