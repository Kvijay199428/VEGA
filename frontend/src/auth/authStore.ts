import { create } from 'zustand'

/**
 * User role for RBAC.
 */
export type UserRole = 'USER' | 'ADMIN' | 'OPERATOR'

/**
 * Authenticated user information.
 */
export interface User {
    id: string
    role: UserRole
    displayName: string
    email?: string
}

/**
 * Authentication state.
 * 
 * Note: This store is in-memory only.
 * It resets on page refresh (by design - stateless frontend).
 * Session recovery happens via Bootstrap.tsx calling /api/auth/session.
 */
interface AuthState {
    // State
    authenticated: boolean
    user: User | null
    loading: boolean
    error: string | null

    // Actions
    setAuthenticated: (user: User) => void
    setLoading: (loading: boolean) => void
    setError: (error: string | null) => void
    logout: () => void
    reset: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
    // Initial state
    authenticated: false,
    user: null,
    loading: true, // Start as loading until bootstrap completes
    error: null,

    // Set authenticated with user info
    setAuthenticated: (user) =>
        set({
            authenticated: true,
            user,
            loading: false,
            error: null
        }),

    // Set loading state
    setLoading: (loading) => set({ loading }),

    // Set error state
    setError: (error) =>
        set({
            error,
            loading: false
        }),

    // Logout - clear all auth state
    logout: () =>
        set({
            authenticated: false,
            user: null,
            loading: false,
            error: null
        }),

    // Reset to initial state
    reset: () =>
        set({
            authenticated: false,
            user: null,
            loading: true,
            error: null
        })
}))
