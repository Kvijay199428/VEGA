import httpClient from '../api/httpClient'
import { endpoints } from '../api/endpoints'
import { useSessionStore, BrokerTokenStatus } from '../store/sessionStore'
import { useAuthStore } from '../auth/authStore'

/**
 * Fetch token status from backend.
 */
export const fetchTokenStatus = async () => {
    const res = await httpClient.get<Record<string, BrokerTokenStatus>>(endpoints.tokens.status)
    return res.data
}

/**
 * Fetch broker health status.
 */
export const fetchBrokerHealth = async () => {
    try {
        const res = await httpClient.get(endpoints.broker.health)
        return res.data?.status || 'UNKNOWN'
    } catch {
        return 'UNKNOWN'
    }
}

/**
 * Bootstrap session - fetches token status and broker health.
 * Called on app start and every 30 seconds.
 */
export const bootstrapSession = async (): Promise<void> => {
    const { setTokens, setHealth } = useSessionStore.getState()
    const { authenticated } = useAuthStore.getState()

    // Only fetch if authenticated
    if (!authenticated) {
        return
    }

    try {
        // Fetch token status
        const tokens = await fetchTokenStatus()
        setTokens(tokens)

        // Fetch broker health
        const health = await fetchBrokerHealth()
        setHealth(health)
    } catch (error) {
        console.error('[Session Bootstrap] Failed:', error)
    }
}

/**
 * Start session polling (every 30 seconds).
 */
export const startSessionPolling = () => {
    // Initial fetch
    bootstrapSession()

    // Set up interval
    const interval = setInterval(bootstrapSession, 30000)

    // Return cleanup function
    return () => clearInterval(interval)
}
