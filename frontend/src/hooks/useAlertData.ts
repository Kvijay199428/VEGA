import { useState, useEffect } from 'react'

export interface AlertData {
    id: string
    type: 'INFO' | 'WARNING' | 'CRITICAL' | 'SUCCESS'
    message: string
    time: string
    source: 'SYSTEM' | 'RISK' | 'ORDER' | 'MARKET'
    read: boolean
}

const MOCK_ALERTS: AlertData[] = [
    { id: '1', type: 'WARNING', message: 'Margin utilization exceeded 75%', time: '10:15:22', source: 'RISK', read: false },
    { id: '2', type: 'SUCCESS', message: 'Order ORD001 RELIANCE BUY 100 Executed', time: '09:32:16', source: 'ORDER', read: true },
    { id: '3', type: 'CRITICAL', message: 'API Rate limit approaching (90%)', time: '14:20:00', source: 'SYSTEM', read: false },
    { id: '4', type: 'INFO', message: 'Market data connected primary stream', time: '09:00:01', source: 'SYSTEM', read: true },
]

export function useAlertData() {
    const [alerts, setAlerts] = useState<AlertData[]>(MOCK_ALERTS)
    const [unreadCount, setUnreadCount] = useState(0)

    useEffect(() => {
        setUnreadCount(alerts.filter(a => !a.read).length)
    }, [alerts])

    const markAsRead = (id: string) => {
        setAlerts(prev => prev.map(a => a.id === id ? { ...a, read: true } : a))
    }

    const clearAlerts = () => {
        setAlerts([])
    }

    // Simulate incoming random alerts
    useEffect(() => {
        const interval = setInterval(() => {
            if (Math.random() > 0.7) {
                const types: AlertData['type'][] = ['INFO', 'WARNING', 'SUCCESS']
                const sources: AlertData['source'][] = ['MARKET', 'ORDER', 'RISK']

                const newAlert: AlertData = {
                    id: Date.now().toString(),
                    type: types[Math.floor(Math.random() * types.length)],
                    message: `Simulated system event #${Math.floor(Math.random() * 1000)}`,
                    time: new Date().toLocaleTimeString('en-GB'),
                    source: sources[Math.floor(Math.random() * sources.length)],
                    read: false
                }
                setAlerts(prev => [newAlert, ...prev].slice(0, 50)) // Keep last 50
            }
        }, 8000)
        return () => clearInterval(interval)
    }, [])

    return { alerts, unreadCount, markAsRead, clearAlerts }
}
