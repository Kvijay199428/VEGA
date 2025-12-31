import { create } from 'zustand'

export interface BrokerTokenStatus {
    valid: boolean
    expiresAt: string
}

export type BrokerHealth = 'UP' | 'DEGRADED' | 'DOWN' | 'UNKNOWN'

interface SessionState {
    tokens: Record<string, BrokerTokenStatus>
    brokerHealth: BrokerHealth
    hardBlock: boolean
    lastUpdated: number | null

    setTokens: (tokens: Record<string, BrokerTokenStatus>) => void
    setHealth: (health: BrokerHealth) => void
    computeHardBlock: () => boolean
}

export const useSessionStore = create<SessionState>((set, get) => ({
    tokens: {},
    brokerHealth: 'UNKNOWN',
    hardBlock: true,
    lastUpdated: null,

    setTokens: (tokens) => {
        // Check if any token is invalid
        const hasInvalidToken = Object.values(tokens).some((t) => !t.valid)
        set({
            tokens,
            hardBlock: hasInvalidToken,
            lastUpdated: Date.now()
        })
    },

    setHealth: (health) => {
        set({
            brokerHealth: health,
            hardBlock: health === 'DOWN' || get().hardBlock
        })
    },

    computeHardBlock: () => {
        const state = get()
        const hasInvalidToken = Object.values(state.tokens).some((t) => !t.valid)
        return hasInvalidToken || state.brokerHealth === 'DOWN'
    }
}))
