/**
 * API Endpoints Registry.
 * Single source of truth for all backend routes.
 */
export const endpoints = {
    // Auth endpoints
    auth: {
        login: '/api/auth/login',
        logout: '/api/auth/logout',
        session: '/api/auth/session',
        seleniumLogin: '/api/v1/auth/selenium/login',
        seleniumMultiLogin: '/api/v1/auth/selenium/multi-login'
    },

    // Token management
    tokens: {
        status: '/api/auth/upstox/tokens/status',
        generate: '/api/auth/upstox/tokens/generate',
        generateSingle: (apiName: string) => `/api/auth/upstox/tokens/generate/${apiName}`
    },

    // Broker
    broker: {
        health: '/api/broker/health'
    },

    // Orders
    orders: {
        place: '/api/orders/place',
        cancel: '/api/orders/cancel',
        modify: '/api/orders/modify',
        book: '/api/orders/book',
        trades: '/api/orders/trades'
    },

    // Market data
    market: {
        ltp: '/api/market-quote/ltp',
        ohlc: '/api/market-quote/ohlc',
        optionChain: '/api/option-chain'
    },

    // Admin
    admin: {
        actions: '/api/admin/actions',
        audit: '/api/admin/audit'
    }
} as const
