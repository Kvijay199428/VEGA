import { OrderBookSnapshot } from '../types/market';

interface DepthLadderProps {
    depth: OrderBookSnapshot | null;
}

/**
 * Standard 5-level DOM (Depth of Market) Ladder.
 */
export function DepthLadder({ depth }: DepthLadderProps) {
    if (!depth || !depth.bids || !depth.asks) {
        return <div className="text-gray-500 text-xs p-2">Waiting for Depth...</div>
    }

    // Sort asks ascending (low sell price at bottom near spread), bids descending (high buy price at top)
    // Standard vertical ladder logic:
    // Asks: High -> Low
    // --- SPREAD ---
    // Bids: High -> Low

    // We want top 5 asks (lowest prices) and top 5 bids (highest prices)
    const topAsks = [...depth.asks].sort((a, b) => a.price - b.price).slice(0, 5).reverse(); // Reverse to show lowest at bottom
    const topBids = [...depth.bids].sort((a, b) => b.price - a.price).slice(0, 5);

    return (
        <div className="bg-[#1e1e1e] rounded font-mono text-xs w-64 border border-[#333]">
            <div className="bg-[#2d2d2d] px-2 py-1 flex justify-between text-gray-400">
                <span>Bid Qty</span>
                <span>Price</span>
                <span>Ask Qty</span>
            </div>

            {/* Asks (Red) */}
            {topAsks.map((level, i) => (
                <div key={`ask-${i}`} className="flex justify-between px-2 py-0.5 relative">
                    {/* Overlay bar logic omitted for brevity */}
                    <span className="w-1/3 text-left"></span>
                    <span className="w-1/3 text-center text-red-400">{level.price.toFixed(2)}</span>
                    <span className="w-1/3 text-right text-gray-300">{level.quantity}</span>
                </div>
            ))}

            <div className="border-t border-b border-[#444] my-1"></div>

            {/* Bids (Green) */}
            {topBids.map((level, i) => (
                <div key={`bid-${i}`} className="flex justify-between px-2 py-0.5">
                    <span className="w-1/3 text-left text-gray-300">{level.quantity}</span>
                    <span className="w-1/3 text-center text-green-400">{level.price.toFixed(2)}</span>
                    <span className="w-1/3 text-right"></span>
                </div>
            ))}
        </div>
    )
}
