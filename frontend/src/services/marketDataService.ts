import { httpClient } from '../api/httpClient'
import { SubscriptionRequest, SubscriptionResponse, LiveMarketSnapshot, OrderBookSnapshot } from '../types/market'

const BASE_PATH = '/api/market'

export const marketDataService = {
    /**
     * Subscribe to a list of instruments.
     */
    subscribe: async (request: SubscriptionRequest): Promise<SubscriptionResponse> => {
        const response = await httpClient.post<SubscriptionResponse>(`${BASE_PATH}/subscribe`, request)
        return response.data
    },

    /**
     * Unsubscribe from instruments.
     */
    unsubscribe: async (instrumentKeys: string[]): Promise<string[]> => {
        const response = await httpClient.post<string[]>(`${BASE_PATH}/unsubscribe`, { instrumentKeys })
        return response.data
    },

    /**
     * Get initial snapshot for instruments (HTTP fallback/init).
     * Note: You might need to add this endpoint to backend if not exists, 
     * or rely on WS 'onConnected' or just wait for ticks.
     * For now, we assume backend might return snapshot or we just wait.
     */
    // getSnapshot: async (instruments: string[]) => ...
}
