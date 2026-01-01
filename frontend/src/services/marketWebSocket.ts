import { LiveMarketSnapshot, OrderBookSnapshot } from '../types/market'

type TickCallback = (tick: LiveMarketSnapshot) => void
type DepthCallback = (depth: OrderBookSnapshot) => void

export class MarketWebSocketClient {
    private ws: WebSocket | null = null
    private url: string
    private reconnectInterval: number = 5000
    private shouldReconnect: boolean = true

    private tickListeners: Set<TickCallback> = new Set()
    private depthListeners: Set<DepthCallback> = new Set()

    constructor(url: string = 'ws://localhost:28020/ws/market') {
        this.url = url
    }

    connect() {
        this.shouldReconnect = true
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
        if (message.instrumentKey && message.ltp !== undefined) {
            // It's a tick
            this.tickListeners.forEach(cb => cb(message as LiveMarketSnapshot))
        } else if (message.instrumentKey && message.bids && message.asks) {
            // It's depth
            this.depthListeners.forEach(cb => cb(message as OrderBookSnapshot))
        }
    }

    disconnect() {
        this.shouldReconnect = false
        if (this.ws) {
            this.ws.close()
        }
    }

    onTick(callback: TickCallback) {
        this.tickListeners.add(callback)
        return () => this.tickListeners.delete(callback)
    }

    onDepth(callback: DepthCallback) {
        this.depthListeners.add(callback)
        return () => this.depthListeners.delete(callback)
    }
}

export const marketStream = new MarketWebSocketClient()
