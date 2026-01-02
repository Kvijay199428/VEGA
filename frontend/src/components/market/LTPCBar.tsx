/**
 * LTPCBar - Last Traded Price & Close Bar
 * 
 * Bloomberg-style top bar showing:
 * - LTP with flash animation (green up / red down)
 * - Change and change %
 * - Latency badge
 * - Close price comparison
 */

import { useLtpcStore } from '../../stores/ltpcStore';

interface LTPCBarProps {
    instrumentKey: string;
    symbol?: string;
}

export function LTPCBar({ instrumentKey, symbol }: LTPCBarProps) {
    const data = useLtpcStore(state => state.data.get(instrumentKey));

    if (!data) {
        return (
            <div className="flex items-center gap-4 px-4 py-2 bg-[#161b22] border-b border-[#30363d]">
                <span className="text-[#8b949e] font-mono text-sm">
                    {symbol || instrumentKey} - Waiting for data...
                </span>
            </div>
        );
    }

    const isPositive = data.change >= 0;
    const flashClass = data.flash === 'up'
        ? 'animate-flash-green'
        : data.flash === 'down'
            ? 'animate-flash-red'
            : '';

    return (
        <div className="flex items-center gap-6 px-4 py-2 bg-[#161b22] border-b border-[#30363d]">
            {/* Symbol */}
            <div className="font-bold text-[#c9d1d9] text-lg">
                {symbol || instrumentKey.split('|')[1]}
            </div>

            {/* LTP with flash */}
            <div className={`font-mono text-xl font-bold transition-colors ${flashClass} ${isPositive ? 'text-[#3fb950]' : 'text-[#f85149]'
                }`}>
                ₹{data.ltp.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
            </div>

            {/* Change */}
            <div className={`font-mono text-sm ${isPositive ? 'text-[#3fb950]' : 'text-[#f85149]'}`}>
                {isPositive ? '+' : ''}{data.change.toFixed(2)}
                ({isPositive ? '+' : ''}{data.changePercent.toFixed(2)}%)
            </div>

            {/* Close price */}
            <div className="text-[#8b949e] text-xs font-mono">
                PREV: ₹{data.cp.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
            </div>

            {/* Last trade quantity */}
            {data.ltq > 0 && (
                <div className="text-[#8b949e] text-xs font-mono">
                    LTQ: {data.ltq.toLocaleString('en-IN')}
                </div>
            )}

            {/* Latency indicator */}
            <div className="ml-auto flex items-center gap-2">
                <div className={`w-2 h-2 rounded-full ${Date.now() - data.lastUpdate < 1000 ? 'bg-[#3fb950]' :
                        Date.now() - data.lastUpdate < 5000 ? 'bg-[#f0c808]' : 'bg-[#f85149]'
                    }`} />
                <span className="text-[#8b949e] text-xs">
                    {Date.now() - data.lastUpdate < 1000 ? 'LIVE' : 'STALE'}
                </span>
            </div>
        </div>
    );
}

export default LTPCBar;
