import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { LoginPage } from '../pages/LoginPage'
import { httpClient } from '../api/httpClient'

/**
 * Login Route with Session Guard
 * 
 * Checks for valid session before rendering login page.
 * If PRIMARY token exists and is valid, redirects to dashboard.
 * 
 * This provides Bloomberg-terminal behavior: login page only shown when necessary.
 */
export function GuardedLoginPage() {
    const navigate = useNavigate()
    const [loading, setLoading] = useState(true)
    const [shouldShowLogin, setShouldShowLogin] = useState(false)

    useEffect(() => {
        async function checkSession() {
            try {
                const response = await httpClient.get('/api/auth/session')
                const session = response.data

                // If PRIMARY token exists and is valid, bypass login
                if (session.status === 'SUCCESS' && session.primaryReady) {
                    console.log('[LoginGuard] Session valid - redirecting to dashboard')
                    console.log(`[LoginGuard] Tokens: ${session.generatedTokens}/${session.configuredApis}`)
                    navigate('/dashboard', { replace: true })
                    return
                }

                // Session invalid or PRIMARY missing - show login page
                console.log('[LoginGuard] No valid session - showing login page')
                setShouldShowLogin(true)
            } catch (error) {
                // Network error or backend down - show login page
                console.warn('[LoginGuard] Session check failed:', error)
                setShouldShowLogin(true)
            } finally {
                setLoading(false)
            }
        }

        checkSession()
    }, [navigate])

    if (loading) {
        // Minimal loading state - session check is very fast
        return (
            <div className="min-h-screen bg-terminal-bg flex items-center justify-center">
                <div className="text-terminal-muted text-sm">Checking session...</div>
            </div>
        )
    }

    return shouldShowLogin ? <LoginPage /> : null
}
