import axios from 'axios'

// Create axios instance with base URL from environment
export const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '',
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// Request interceptor for logging
api.interceptors.request.use(
    (config) => {
        console.log(`[API] ${config.method?.toUpperCase()} ${config.url}`)
        return config
    },
    (error) => Promise.reject(error)
)

// Response interceptor for error handling
api.interceptors.response.use(
    (response) => response,
    (error) => {
        console.error('[API Error]', error.response?.data || error.message)
        return Promise.reject(error)
    }
)

// ============ Auth API ============

export interface LoginPayload {
    apiName: string
    clientId: string
    clientSecret?: string
    redirectUri?: string
    username: string
    password: string
    totpSecret?: string
    isPrimary?: boolean
    browser?: string
    headless?: boolean
}

export interface GeneratePayload {
    mode: 'ALL' | 'INVALID_ONLY' | 'PARTIAL'
    apiNames?: string[]
}

export interface TokenStatus {
    [key: string]: {
        valid: boolean
        expiresAt: string
    }
}

// Trigger Selenium login automation
export const triggerLogin = (payload: LoginPayload) =>
    api.post('/api/v1/auth/selenium/login', payload)

// Generate tokens (batch)
export const generateTokens = (payload: GeneratePayload = { mode: 'ALL' }) =>
    api.post('/api/auth/upstox/tokens/generate', payload)

// Fetch token status
export const fetchTokenStatus = () =>
    api.get<TokenStatus>('/api/auth/upstox/tokens/status')

// Fetch broker health (placeholder - implement endpoint if not exists)
export const fetchBrokerHealth = () =>
    api.get('/api/broker/health').catch(() => ({ data: { status: 'UNKNOWN' } }))
