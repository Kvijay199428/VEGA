import { redirect } from 'react-router-dom'
import { httpClient } from '../api/httpClient'

/**
 * Login Route Loader
 * 
 * Guards the login page with session awareness.
 * If PRIMARY token exists and is valid, bypasses login and redirects to dashboard.
 * 
 * This prevents the login UI from showing when users manually navigate to /login
 * while already authenticated - matches Bloomberg/OMS terminal behavior.
 * 
 * @returns Redirect to dashboard if session is valid, null otherwise
 */
export async function loginLoader() {
    try {
        const response = await httpClient.get('/api/auth/session')
        const session = response.data

        // If PRIMARY token exists and is valid, bypass login page
        if (session.status === 'SUCCESS' && session.primaryReady) {
            console.log('[LoginGuard] Session valid - redirecting to dashboard')
            console.log(`[LoginGuard] Tokens: ${session.generatedTokens}/${session.configuredApis}`)
            return redirect('/dashboard')
        }

        // Session invalid or PRIMARY missing - allow login page to render
        console.log('[LoginGuard] No valid session - showing login page')
        return null
    } catch (error) {
        // Network error or backend down - allow login page
        // User may need to see the login UI to understand the issue
        console.warn('[LoginGuard] Session check failed:', error)
        return null
    }
}
