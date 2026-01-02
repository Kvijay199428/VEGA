/**
 * OrderBookLadder - L2 Depth Ladder
 * 
 * Bloomberg-style order book display:
 * - Centered LTP
 * - Heatmap intensity by quantity
 * - Depth selector (5/10/20)
 * - Freeze control
 * - Spread display
 */

import { useState } from 'react';
import { useOrderBookStore } from '../../stores/orderBookStore';
import { useLtpcStore } from '../../stores/ltpcStore';
import { PriceLevel } from '../../types/VegaTick';

interface OrderBookLadderProps {
    instrumentKey: string;
}

function PriceLevelRow({
    level,
    side,
    maxQty
}: {
    level: PriceLevel;
    side: 'bid' | 'ask';
    maxQty: number;
}) {
    const intensity = maxQty > 0 ? (level.qty / maxQty) : 0;
    const bgColor = side === 'bid'
        ? `rgba(63, 185, 80, ${intensity * 0.4})`
        : `rgba(248, 81, 73, ${intensity * 0.4})`;

    return (
        <div
            className="flex items-center px-2 py-1 font-mono text-xs border-b border-[#21262d]"
            style={{ backgroundColor: bgColor }}
        >
            {side === 'bid' ? (
                <>
                    <span className="w-20 text-right text-[#8b949e]">
                        {level.qty.toLocaleString('en-IN')}
                    </span>
                    <span className="w-24 text-right text-[#3fb950] font-medium ml-2">
                        {level.price.toFixed(2)}
                    </span>
                </>
            ) : (
                <>
                    <span className="w-24 text-left text-[#f85149] font-medium">
                        {level.price.toFixed(2)}
                    </span>
                    <span className="w-20 text-left text-[#8b949e] ml-2">
                        {level.qty.toLocaleString('en-IN')}
                    </span>
                </>
            )}
        </div>
    );
}

export function OrderBookLadder({ instrumentKey }: OrderBookLadderProps) {
    const book = useOrderBookStore(state => state.getSliced(instrumentKey));
    const isFrozen = useOrderBookStore(state => state.isFrozen(instrumentKey));
    const freeze = useOrderBookStore(state => state.freeze);
    const resume = useOrderBookStore(state => state.resume);
    const setDepth = useOrderBookStore(state => state.setDisplayDepth);
    const displayDepth = useOrderBookStore(state => state.getDisplayDepth(instrumentKey));
    const ltp = useLtpcStore(state => state.data.get(instrumentKey)?.ltp ?? 0);

    const [depthOptions] = useState([5, 10, 20]);

    if (!book) {
        return (
            <div className="bg-[#0d1117] border border-[#30363d] rounded p-4">
                <div className="text-[#8b949e] text-sm">Order Book - Waiting...</div>
            </div>
        );
    }

    const maxBidQty = Math.max(...book.bids.map(b => b.qty), 1);
    const maxAskQty = Math.max(...book.asks.map(a => a.qty), 1);

    return (
        <div className="bg-[#0d1117] border border-[#30363d] rounded">
            {/* Header */}
            <div className="flex items-center justify-between px-3 py-2 border-b border-[#30363d]">
                <div className="flex items-center gap-2">
                    <span className="text-[#c9d1d9] font-medium text-sm">ORDER BOOK</span>
                    {isFrozen && (
                        <span className="text-[#f0c808] text-xs px-1 bg-[#f0c808]/20 rounded">
                            FROZEN
                        </span>
                    )}
                </div>
                <div className="flex items-center gap-2">
                    {/* Depth selector */}
                    <select
                        value={displayDepth}
                        onChange={(e) => setDepth(instrumentKey, Number(e.target.value))}
                        className="bg-[#21262d] text-[#c9d1d9] text-xs px-2 py-1 rounded border border-[#30363d]"
                    >
                        {depthOptions.map(d => (
                            <option key={d} value={d}>L{d}</option>
                        ))}
                    </select>

                    {/* Freeze button */}
                    <button
                        onClick={() => isFrozen ? resume(instrumentKey) : freeze(instrumentKey)}
                        className={`text-xs px-2 py-1 rounded ${isFrozen
                                ? 'bg-[#f0c808] text-black'
                                : 'bg-[#21262d] text-[#c9d1d9] border border-[#30363d]'
                            }`}
                    >
                        {isFrozen ? 'RESUME' : 'FREEZE'}
                    </button>
                </div>
            </div>

            {/* Two-column layout */}
            <div className="grid grid-cols-2 gap-0">
                {/* Bids (left) */}
                <div className="border-r border-[#30363d]">
                    <div className="px-2 py-1 text-xs text-[#3fb950] bg-[#21262d] border-b border-[#30363d]">
                        BID ({book.totalBidQty.toLocaleString('en-IN')})
                    </div>
                    {book.bids.map((bid, i) => (
                        <PriceLevelRow key={i} level={bid} side="bid" maxQty={maxBidQty} />
                    ))}
                </div>

                {/* Asks (right) */}
                <div>
                    <div className="px-2 py-1 text-xs text-[#f85149] bg-[#21262d] border-b border-[#30363d]">
                        ASK ({book.totalAskQty.toLocaleString('en-IN')})
                    </div>
                    {book.asks.map((ask, i) => (
                        <PriceLevelRow key={i} level={ask} side="ask" maxQty={maxAskQty} />
                    ))}
                </div>
            </div>

            {/* Spread */}
            <div className="px-3 py-2 border-t border-[#30363d] flex items-center justify-between text-xs">
                <span className="text-[#8b949e]">
                    Spread: ₹{book.spread.toFixed(2)} ({book.spreadPercent.toFixed(3)}%)
                </span>
                <span className="text-[#c9d1d9]">
                    LTP: ₹{ltp.toFixed(2)}
                </span>
            </div>
        </div>
    );
}

export default OrderBookLadder;
