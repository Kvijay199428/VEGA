import { useEffect, useState, ReactNode } from 'react'
import { AuthService } from '../auth/AuthService'
import { useAuthStore } from '../auth/authStore'

interface BootstrapProps {
    children: ReactNode
}

/**
 * Bootstrap component - wraps the entire app.
 * 
 * Responsibilities:
 * 1. Attempt session recovery on app start
 * 2. Listen for session-expired events
 * 3. Show loading state during bootstrap
 * 
 * This is critical for SEBI compliance:
 * - Session state is always verified with backend
 * - Frontend never assumes authentication
 */
export function Bootstrap({ children }: BootstrapProps) {
    const { setAuthenticated, setLoading, logout } = useAuthStore()
    const [bootstrapped, setBootstrapped] = useState(false)

    useEffect(() => {
        // Attempt session recovery
        const initSession = async () => {
            try {
                const res = await AuthService.bootstrap()
                if (res.status === 'SUCCESS' && res.user) {
                    setAuthenticated(res.user)
                } else {
                    setLoading(false)
                }
            } catch {
                // No valid session - this is expected for new visitors
                setLoading(false)
            } finally {
                setBootstrapped(true)
            }
        }

        initSession()

        // Listen for session-expired events (from httpClient)
        const handleSessionExpired = () => {
            logout()
            // Optionally redirect to login
            window.location.href = '/login'
        }

        window.addEventListener('session-expired', handleSessionExpired)

        return () => {
            window.removeEventListener('session-expired', handleSessionExpired)
        }
    }, [setAuthenticated, setLoading, logout])

    // Show loading during bootstrap
    if (!bootstrapped) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-terminal-bg">
                <div className="text-center">
                    <div className="text-terminal-success text-xl font-bold mb-2">
                        VEGA TRADER
                    </div>
                    <div className="text-terminal-muted text-sm">
                        Initializing session...
                    </div>
                </div>
            </div>
        )
    }

    return <>{children}</>
}

export default Bootstrap
