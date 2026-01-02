import { LiveMarketSnapshot, OrderBookSnapshot } from '../types/market'
import { VegaTick } from '../types/VegaTick'
import { normalize, RawMarketUpdate } from './normalizer'
import { useLtpcStore } from '../stores/ltpcStore'
import { useOrderBookStore } from '../stores/orderBookStore'
import { useGreeksStore } from '../stores/greeksStore'
import { useOhlcStore } from '../stores/ohlcStore'
import { useMetricsStore } from '../stores/metricsStore'

type TickCallback = (tick: LiveMarketSnapshot) => void
type DepthCallback = (depth: OrderBookSnapshot) => void
type VegaTickCallback = (tick: VegaTick) => void

export class MarketWebSocketClient {
    private ws: WebSocket | null = null
    private url: string
    private reconnectInterval: number = 5000
    private shouldReconnect: boolean = true

    // Legacy listeners (for backward compatibility)
    private tickListeners: Set<TickCallback> = new Set()
    private depthListeners: Set<DepthCallback> = new Set()

    // New normalized tick listeners
    private vegaTickListeners: Set<VegaTickCallback> = new Set()

    constructor(url: string = 'ws://localhost:28020/ws/market') {
        this.url = url
    }

    connect(authToken?: string) {
        if (this.ws && (this.ws.readyState === WebSocket.OPEN || this.ws.readyState === WebSocket.CONNECTING)) {
            console.log('[WS] Already connected/connecting, skipping.')
            return
        }
        this.shouldReconnect = true
        if (authToken) {
            this.url = `ws://localhost:28020/ws/market?token=${authToken}`
        }
        this.tryConnect()
    }

    private tryConnect() {
        console.log(`[WS] Connecting to ${this.url}`)
        this.ws = new WebSocket(this.url)

        this.ws.onopen = () => {
            console.log('[WS] Connected')
        }

        this.ws.onclose = () => {
            console.warn('[WS] Disconnected')
            if (this.shouldReconnect) {
                setTimeout(() => this.tryConnect(), this.reconnectInterval)
            }
        }

        this.ws.onerror = (error) => {
            console.error('[WS] Error', error)
        }

        this.ws.onmessage = (event) => {
            try {
                const message = JSON.parse(event.data)
                this.handleMessage(message)
            } catch (e) {
                console.error('[WS] Parse error', e)
            }
        }
    }

    private handleMessage(message: any) {
        // Skip non-market messages (CONNECTED, SUBSCRIBED, etc.)
        if (message.type && !message.instrumentKey) {
            console.log('[WS] Control message:', message.type)
            return
        }

        // Normalize at the boundary
        if (message.instrumentKey) {
            const tick = normalize(message as RawMarketUpdate)

            // Dispatch to all Zustand stores
            this.dispatchToStores(tick)

            // Notify VegaTick listeners
            this.vegaTickListeners.forEach(cb => cb(tick))

            // Legacy tick listeners (for backward compatibility)
            if (message.ltp !== undefined) {
                this.tickListeners.forEach(cb => cb(message as LiveMarketSnapshot))
            }

            // Legacy depth listeners
            if (message.bids && message.asks) {
                this.depthListeners.forEach(cb => cb(message as OrderBookSnapshot))
            }
        }
    }

    /**
     * Dispatch normalized tick to all domain stores.
     * This is the single point of store updates.
     */
    private dispatchToStores(tick: VegaTick) {
        // Get store instances (Zustand stores are singletons)
        useLtpcStore.getState().update(tick)
        useOrderBookStore.getState().update(tick)
        useGreeksStore.getState().update(tick)
        useOhlcStore.getState().update(tick)
        useMetricsStore.getState().update(tick)
    }

    disconnect(force = false) {
        if (!force && import.meta.env.DEV) {
            console.log('[WS] Soft disconnect ignored in DEV mode')
            return
        }

        this.shouldReconnect = false
        if (this.ws) {
            this.ws.close()
            this.ws = null
        }
    }

    /**
     * Subscribe to normalized VegaTick updates.
     * This is the preferred method for new components.
     */
    onVegaTick(callback: VegaTickCallback) {
        this.vegaTickListeners.add(callback)
        return () => this.vegaTickListeners.delete(callback)
    }

    // Legacy methods for backward compatibility
    onTick(callback: TickCallback) {
        this.tickListeners.add(callback)
        return () => this.tickListeners.delete(callback)
    }

    onDepth(callback: DepthCallback) {
        this.depthListeners.add(callback)
        return () => this.depthListeners.delete(callback)
    }

    /**
     * Send subscription message to backend.
     */
    subscribe(instrumentKeys: string[]) {
        if (this.ws?.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify({
                type: 'SUBSCRIBE',
                instruments: instrumentKeys
            }))
        }
    }

    /**
     * Send unsubscription message to backend.
     */
    unsubscribe(instrumentKeys: string[]) {
        if (this.ws?.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify({
                type: 'UNSUBSCRIBE',
                instruments: instrumentKeys
            }))
        }
    }

    /**
     * Check if connected.
     */
    isConnected(): boolean {
        return this.ws?.readyState === WebSocket.OPEN
    }
}

export const marketStream = new MarketWebSocketClient()

