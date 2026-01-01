import { useState, useEffect, useCallback } from 'react'

export interface OrderData {
    orderId: string
    symbol: string
    side: 'BUY' | 'SELL'
    qty: number
    price: number
    status: 'PENDING' | 'COMPLETE' | 'REJECTED' | 'CANCELLED' | 'OPEN'
    limitPrice?: number
    type: 'LIMIT' | 'MARKET'
    time: string
    filledQty: number
    avgPrice: number
}

export interface TradeData {
    tradeId: string
    orderId: string
    symbol: string
    side: 'BUY' | 'SELL'
    qty: number
    price: number
    time: string
}

const ORDER_ID_PREFIX = 'ORD'
let orderCounter = 100

const MOCK_ORDERS: OrderData[] = [
    { orderId: 'ORD001', symbol: 'RELIANCE', side: 'BUY', qty: 100, price: 0, limitPrice: 2450.00, type: 'LIMIT', status: 'COMPLETE', time: '09:32:15', filledQty: 100, avgPrice: 2448.50 },
    { orderId: 'ORD002', symbol: 'HDFCBANK', side: 'SELL', qty: 50, price: 0, limitPrice: 1680.50, type: 'LIMIT', status: 'COMPLETE', time: '10:15:42', filledQty: 50, avgPrice: 1681.00 },
    { orderId: 'ORD003', symbol: 'TCS', side: 'BUY', qty: 25, price: 0, limitPrice: 3890.00, type: 'LIMIT', status: 'OPEN', time: '11:02:33', filledQty: 0, avgPrice: 0 },
    { orderId: 'ORD004', symbol: 'INFY', side: 'BUY', qty: 75, price: 0, limitPrice: 1540.00, type: 'LIMIT', status: 'REJECTED', time: '11:45:18', filledQty: 0, avgPrice: 0 },
    { orderId: 'ORD005', symbol: 'SBIN', side: 'SELL', qty: 200, price: 0, limitPrice: 755.00, type: 'LIMIT', status: 'CANCELLED', time: '12:30:55', filledQty: 0, avgPrice: 0 },
]

export function useOrderData() {
    const [orders, setOrders] = useState<OrderData[]>(MOCK_ORDERS)
    const [trades, _setTrades] = useState<TradeData[]>([])
    const [loading, setLoading] = useState(true)

    // Simulate initial load
    useEffect(() => {
        const timer = setTimeout(() => {
            setLoading(false)
        }, 500)
        return () => clearTimeout(timer)
    }, [])

    const placeOrder = useCallback((
        symbol: string,
        side: 'BUY' | 'SELL',
        qty: number,
        type: 'LIMIT' | 'MARKET',
        limitPrice?: number
    ) => {
        const newOrder: OrderData = {
            orderId: `${ORDER_ID_PREFIX}${orderCounter++}`,
            symbol: symbol.toUpperCase(),
            side,
            qty,
            type,
            limitPrice: type === 'LIMIT' ? limitPrice : 0,
            price: 0,
            status: 'PENDING',
            time: new Date().toLocaleTimeString('en-GB', { hour12: false }),
            filledQty: 0,
            avgPrice: 0
        }

        setOrders(prev => [newOrder, ...prev])

        // Simulate network delay and execution
        setTimeout(() => {
            const isRejected = Math.random() > 0.9
            const finalStatus = isRejected ? 'REJECTED' : (type === 'MARKET' || Math.random() > 0.5) ? 'COMPLETE' : 'OPEN'

            setOrders(prev => prev.map(o => {
                if (o.orderId === newOrder.orderId) {
                    return {
                        ...o,
                        status: finalStatus,
                        filledQty: finalStatus === 'COMPLETE' ? o.qty : 0,
                        avgPrice: finalStatus === 'COMPLETE' ? (o.limitPrice || 100) : 0
                    }
                }
                return o
            }))
        }, 1500)
    }, [])

    const cancelOrder = useCallback((orderId: string) => {
        setOrders(prev => prev.map(o => {
            if (o.orderId === orderId && o.status === 'OPEN') {
                return { ...o, status: 'CANCELLED' }
            }
            return o
        }))
    }, [])

    return { orders, trades, loading, placeOrder, cancelOrder }
}
