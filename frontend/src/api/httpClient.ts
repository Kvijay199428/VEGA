import axios, { AxiosError } from 'axios'

/**
 * Hardened HTTP Client for enterprise trading terminal.
 * 
 * Features:
 * - withCredentials for HttpOnly cookie support
 * - Session-expired event dispatch on 401
 * - Client type header for audit trails
 */
export const httpClient = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '',
    withCredentials: true, // REQUIRED for HttpOnly session cookies
    timeout: 10_000,
    headers: {
        'Content-Type': 'application/json',
        'X-Client-Type': 'WEB_TERMINAL',
        'X-Client-Version': '1.0.0'
    }
})

// Request interceptor for logging and audit context
httpClient.interceptors.request.use(
    (config) => {
        // Add timestamp for latency tracking
        config.headers['X-Request-Time'] = new Date().toISOString()
        console.log(`[HTTP] ${config.method?.toUpperCase()} ${config.url}`)
        return config
    },
    (error) => Promise.reject(error)
)

// Response interceptor for error handling and session management
httpClient.interceptors.response.use(
    (response) => response,
    (error: AxiosError) => {
        if (error.response?.status === 401) {
            // Dispatch session-expired event for global handling
            window.dispatchEvent(new CustomEvent('session-expired', {
                detail: { reason: 'unauthorized' }
            }))
        }

        if (error.response?.status === 403) {
            window.dispatchEvent(new CustomEvent('access-denied', {
                detail: { reason: 'forbidden' }
            }))
        }

        console.error('[HTTP Error]', error.response?.data || error.message)
        return Promise.reject(error)
    }
)

export default httpClient
