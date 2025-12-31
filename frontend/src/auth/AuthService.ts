import httpClient from '../api/httpClient'
import { endpoints } from '../api/endpoints'
import { User } from './authStore'

/**
 * Login request payload.
 */
export interface LoginPayload {
    clientCode: string
    password: string
    totp?: string
    deviceId?: string
}

/**
 * Login response from backend.
 */
export interface LoginResponse {
    status: 'SUCCESS' | 'FAILED'
    user?: User
    message?: string
}

/**
 * Selenium login payload (for automated broker auth).
 */
export interface SeleniumLoginPayload {
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

/**
 * Auth Service - handles all authentication operations.
 * 
 * This service is stateless. It only makes HTTP calls.
 * State is managed by authStore.
 */
export const AuthService = {
    /**
     * Login with credentials.
     * Backend sets HttpOnly session cookie.
     */
    async login(payload: LoginPayload): Promise<LoginResponse> {
        const res = await httpClient.post(endpoints.auth.login, {
            ...payload,
            deviceId: payload.deviceId || 'web-terminal'
        })
        return res.data
    },

    /**
     * Bootstrap session - called on app start.
     * Checks if user has valid session cookie.
     */
    async bootstrap(): Promise<LoginResponse> {
        const res = await httpClient.get(endpoints.auth.session)
        return res.data
    },

    /**
     * Logout - invalidates session.
     */
    async logout(): Promise<void> {
        await httpClient.post(endpoints.auth.logout)
    },

    /**
     * Trigger Selenium login automation.
     * Used for broker OAuth flow.
     */
    async triggerSeleniumLogin(payload: SeleniumLoginPayload): Promise<{ status: string; message: string }> {
        const res = await httpClient.post(endpoints.auth.seleniumLogin, payload)
        return res.data
    },

    /**
     * Trigger multi-API Selenium login.
     */
    async triggerMultiLogin(payload: {
        apiConfigs: SeleniumLoginPayload[]
        username: string
        password: string
        totpSecret?: string
    }): Promise<{ status: string; successful: string[]; failed: string[] }> {
        const res = await httpClient.post(endpoints.auth.seleniumMultiLogin, payload)
        return res.data
    }
}

export default AuthService
