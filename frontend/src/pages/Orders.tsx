import { useState } from 'react'
import { useOrderData } from '../hooks/useOrderData'
import { useAlertData } from '../hooks/useAlertData'
import { usePageTitle } from '../context/PageContext'

/**
 * Orders - F5 Execution monitoring & Alert management.
 */
export default function Orders() {
    const { orders, loading: ordersLoading, placeOrder, cancelOrder } = useOrderData()
    const { alerts, clearAlerts } = useAlertData()
    const [activeTab, setActiveTab] = useState<'orders' | 'trades'>('orders')

    // Set page title in TopBar
    usePageTitle('Orders', 'F5', 'EMSX <GO>')

    // Order Entry State
    const [symbol, setSymbol] = useState('RELIANCE')
    const [qty, setQty] = useState(1)
    const [price, setPrice] = useState(2450.00)
    const [orderType, setOrderType] = useState<'LIMIT' | 'MARKET'>('LIMIT')

    const handleSubmit = (side: 'BUY' | 'SELL') => {
        placeOrder(symbol, side, qty, orderType, price)
    }

    if (ordersLoading) {
        return (
            <div className="space-y-4 animate-pulse">
                <div className="h-32 bg-[#161b22] rounded w-full border border-[#30363d]"></div>
                <div className="h-64 bg-[#161b22] rounded w-full border border-[#30363d]"></div>
            </div>
        )
    }

    return (
        <div className="space-y-4">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-xl font-bold text-[#c9d1d9] flex items-center gap-2">
                        Execution & Alerts
                        <span className="text-xs font-normal text-[#6e7681] border border-[#30363d] px-1 rounded">F5</span>
                    </h1>
                    <p className="text-sm text-[#6e7681]">EMSX &lt;GO&gt;</p>
                </div>
            </div>

            <div className="grid grid-cols-3 gap-4">
                {/* Left Column: Order Entry & Alerts */}
                <div className="col-span-1 space-y-4">
                    {/* Order Entry Form */}
                    <div className="bg-[#161b22] border border-[#30363d] rounded p-4 shadow-lg">
                        <div className="text-sm font-bold text-[#c9d1d9] mb-3 border-b border-[#30363d] pb-2">Order Ticket</div>

                        <div className="space-y-3">
                            <div>
                                <label className="text-xs text-[#8b949e] block mb-1">Symbol</label>
                                <input
                                    type="text"
                                    value={symbol}
                                    onChange={e => setSymbol(e.target.value.toUpperCase())}
                                    className="w-full bg-[#f0c808]/10 border border-[#f0c808] text-[#f0c808] font-bold px-2 py-1 rounded outline-none focus:ring-1 focus:ring-[#f0c808]"
                                />
                            </div>

                            <div className="flex gap-2">
                                <div className="flex-1">
                                    <label className="text-xs text-[#8b949e] block mb-1">Qty</label>
                                    <input
                                        type="number"
                                        value={qty}
                                        onChange={e => setQty(Number(e.target.value))}
                                        className="w-full bg-[#21262d] border border-[#30363d] text-[#c9d1d9] px-2 py-1 rounded focus:border-[#00c176] outline-none"
                                    />
                                </div>
                                <div className="flex-1">
                                    <label className="text-xs text-[#8b949e] block mb-1">Type</label>
                                    <div className="flex bg-[#21262d] rounded border border-[#30363d]">
                                        <button
                                            onClick={() => setOrderType('LIMIT')}
                                            className={`flex-1 text-xs py-1 ${orderType === 'LIMIT' ? 'bg-[#30363d] text-[#c9d1d9]' : 'text-[#8b949e]'}`}
                                        >LMT</button>
                                        <button
                                            onClick={() => setOrderType('MARKET')}
                                            className={`flex-1 text-xs py-1 ${orderType === 'MARKET' ? 'bg-[#30363d] text-[#c9d1d9]' : 'text-[#8b949e]'}`}
                                        >MKT</button>
                                    </div>
                                </div>
                            </div>

                            <div>
                                <label className="text-xs text-[#8b949e] block mb-1">Price</label>
                                <input
                                    type="number"
                                    value={price}
                                    onChange={e => setPrice(Number(e.target.value))}
                                    disabled={orderType === 'MARKET'}
                                    className={`w-full border px-2 py-1 rounded outline-none font-mono ${orderType === 'MARKET' ? 'bg-[#21262d] border-[#30363d] text-[#6e7681]' : 'bg-[#f0c808]/10 border-[#f0c808] text-[#f0c808]'}`}
                                />
                            </div>

                            <div className="flex gap-2 pt-2">
                                <button
                                    onClick={() => handleSubmit('BUY')}
                                    className="flex-1 bg-[#00c176] hover:bg-[#00a062] text-black font-bold py-2 rounded transition-colors"
                                >
                                    BUY
                                </button>
                                <button
                                    onClick={() => handleSubmit('SELL')}
                                    className="flex-1 bg-[#ff4d4d] hover:bg-[#cc0000] text-black font-bold py-2 rounded transition-colors"
                                >
                                    SELL
                                </button>
                            </div>
                        </div>
                    </div>

                    {/* Alerts Panel */}
                    <div className="bg-[#161b22] border border-[#30363d] rounded p-4 flex flex-col h-[300px]">
                        <div className="flex justify-between items-center mb-3">
                            <span className="text-sm font-bold text-[#c9d1d9]">System Alerts</span>
                            <button onClick={clearAlerts} className="text-xs text-[#6e7681] hover:text-[#c9d1d9]">Clear</button>
                        </div>
                        <div className="flex-1 overflow-y-auto space-y-2 pr-1 custom-scrollbar">
                            {alerts.length === 0 ? (
                                <div className="text-center text-[#6e7681] text-xs py-4">No active alerts</div>
                            ) : (
                                alerts.map(alert => (
                                    <div key={alert.id} className="text-xs p-2 rounded bg-[#21262d] border-l-2 border-[#30363d] relative group">
                                        <div className="flex justify-between text-[#8b949e] mb-1">
                                            <span>{alert.time}</span>
                                            <span className={`font-bold ${alert.type === 'CRITICAL' ? 'text-[#ff4d4d]' : alert.type === 'WARNING' ? 'text-[#f0c808]' : alert.type === 'SUCCESS' ? 'text-[#00c176]' : 'text-[#8b949e]'}`}>
                                                {alert.type}
                                            </span>
                                        </div>
                                        <div className="text-[#c9d1d9]">{alert.message}</div>
                                    </div>
                                ))
                            )}
                        </div>
                    </div>
                </div>

                {/* Right Column: Order Book */}
                <div className="col-span-2 space-y-4">
                    {/* Tabs */}
                    <div className="flex gap-2 border-b border-[#30363d] pb-2">
                        <button
                            onClick={() => setActiveTab('orders')}
                            className={`px-4 py-1 text-sm rounded transition-colors ${activeTab === 'orders' ? 'bg-[#00c176] text-black font-bold' : 'text-[#8b949e] hover:text-[#c9d1d9]'}`}
                        >
                            Working Orders
                        </button>
                        <button
                            onClick={() => setActiveTab('trades')}
                            className={`px-4 py-1 text-sm rounded transition-colors ${activeTab === 'trades' ? 'bg-[#00c176] text-black font-bold' : 'text-[#8b949e] hover:text-[#c9d1d9]'}`}
                        >
                            Fills / Trades
                        </button>
                    </div>

                    <div className="bg-[#161b22] border border-[#30363d] rounded overflow-hidden shadow-sm h-[600px] overflow-y-auto">
                        <table className="w-full text-sm">
                            <thead className="bg-[#21262d] sticky top-0">
                                <tr className="text-left text-[#8b949e] uppercase text-xs">
                                    <th className="px-4 py-3 font-semibold">Time</th>
                                    <th className="px-4 py-3 font-semibold">ID</th>
                                    <th className="px-4 py-3 font-semibold">Symbol</th>
                                    <th className="px-4 py-3 font-semibold">Side</th>
                                    <th className="px-4 py-3 text-right font-semibold">Qty</th>
                                    <th className="px-4 py-3 text-right font-semibold">Price</th>
                                    <th className="px-4 py-3 font-semibold">Status</th>
                                    <th className="px-4 py-3 font-semibold">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                {orders.length === 0 ? (
                                    <tr>
                                        <td colSpan={8} className="px-4 py-8 text-center text-[#6e7681]">No orders found</td>
                                    </tr>
                                ) : (
                                    orders.map((order, i) => (
                                        <tr key={order.orderId} className={`border-t border-[#30363d] hover:bg-[#21262d] transition-colors ${i % 2 === 0 ? 'bg-[#161b22]' : 'bg-[#0d1117]'}`}>
                                            <td className="px-4 py-2 text-[#8b949e] font-mono">{order.time}</td>
                                            <td className="px-4 py-2 text-[#6e7681] text-xs font-mono">{order.orderId}</td>
                                            <td className="px-4 py-2 font-bold text-[#c9d1d9]">{order.symbol}</td>
                                            <td className={`px-4 py-2 font-bold ${order.side === 'BUY' ? 'text-[#00c176]' : 'text-[#ff4d4d]'}`}>
                                                {order.side}
                                            </td>
                                            <td className="px-4 py-2 text-right text-[#c9d1d9] font-mono">
                                                {order.filledQty}/{order.qty}
                                            </td>
                                            <td className="px-4 py-2 text-right text-[#c9d1d9] font-mono">
                                                {order.type === 'MARKET' ? 'MKT' : order.limitPrice?.toFixed(2)}
                                            </td>
                                            <td className="px-4 py-2">
                                                <span className={`text-xs font-bold px-2 py-0.5 rounded ${order.status === 'COMPLETE' ? 'bg-[#00c176]/20 text-[#00c176]' :
                                                    order.status === 'REJECTED' ? 'bg-[#ff4d4d]/20 text-[#ff4d4d]' :
                                                        order.status === 'CANCELLED' ? 'bg-[#6e7681]/20 text-[#8b949e]' :
                                                            'bg-[#f0c808]/20 text-[#f0c808]'
                                                    }`}>
                                                    {order.status}
                                                </span>
                                            </td>
                                            <td className="px-4 py-2">
                                                {['PENDING', 'OPEN'].includes(order.status) && (
                                                    <button
                                                        onClick={() => cancelOrder(order.orderId)}
                                                        className="text-xs text-[#ff4d4d] hover:text-[#ff0000] border border-[#ff4d4d]/30 px-2 py-0.5 rounded hover:bg-[#ff4d4d]/10 transition-colors"
                                                    >
                                                        Cancel
                                                    </button>
                                                )}
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    )
}
