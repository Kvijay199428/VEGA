import { useContext } from 'react'
import { Navigate } from 'react-router-dom'
import { LoginPage } from '../pages/LoginPage'
import { AuthContext } from '../context/AuthContext'

/**
 * Login Route with Session Guard (Single Source of Truth)
 * 
 * Uses AuthContext (same as ProtectedRoute) to prevent ping-pong redirects.
 * If authenticated, redirects to dashboard. Otherwise shows login page.
 * 
 * IMPORTANT: This guard and ProtectedRoute MUST use the same source of truth
 * (AuthContext via WebSocket) to prevent infinite redirect loops.
 */
export function GuardedLoginPage() {
    const { status, primaryReady } = useContext(AuthContext)

    console.log('[LoginGuard] AuthContext status:', status, 'primaryReady:', primaryReady)

    // Loading state - same splash as ProtectedRoute
    if (status === 'loading') {
        return (
            <div className="min-h-screen bg-terminal-bg flex items-center justify-center">
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

    // If authenticated (same check as ProtectedRoute inverse), redirect to dashboard
    if (status === 'authenticated') {
        console.log('[LoginGuard] Authenticated - redirecting to /dashboard')
        return <Navigate to="/dashboard" replace />
    }

    // Unauthenticated / expired / error - show login page
    console.log('[LoginGuard] Not authenticated - showing LoginPage')
    return <LoginPage />
}
