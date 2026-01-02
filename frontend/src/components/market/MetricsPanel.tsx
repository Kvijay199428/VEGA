/**
 * MetricsPanel - Trading Metrics Display
 * 
 * Bloomberg-style metrics:
 * - Volume traded
 * - Open Interest with change
 * - ATP (Average Traded Price)
 * - Buy/Sell pressure meter
 * - IV percentile
 */

import { useMetricsStore } from '../../stores/metricsStore';

interface MetricsPanelProps {
    instrumentKey: string;
}

function PressureMeter({
    bidQty,
    askQty
}: {
    bidQty: number;
    askQty: number;
}) {
    const total = bidQty + askQty;
    const buyPercent = total > 0 ? (bidQty / total) * 100 : 50;

    return (
        <div className="space-y-1">
            <div className="flex items-center justify-between text-xs">
                <span className="text-[#3fb950]">BUY</span>
                <span className="text-[#8b949e]">Pressure</span>
                <span className="text-[#f85149]">SELL</span>
            </div>
            <div className="h-3 bg-[#21262d] rounded-full overflow-hidden flex">
                <div
                    className="h-full bg-[#3fb950] transition-all"
                    style={{ width: `${buyPercent}%` }}
                />
                <div
                    className="h-full bg-[#f85149] transition-all"
                    style={{ width: `${100 - buyPercent}%` }}
                />
            </div>
            <div className="flex items-center justify-between text-xs font-mono">
                <span className="text-[#3fb950]">{bidQty.toLocaleString('en-IN')}</span>
                <span className="text-[#f85149]">{askQty.toLocaleString('en-IN')}</span>
            </div>
        </div>
    );
}

function MetricRow({
    label,
    value,
    change,
    prefix = '',
    suffix = ''
}: {
    label: string;
    value: string | number;
    change?: number;
    prefix?: string;
    suffix?: string;
}) {
    return (
        <div className="flex items-center justify-between py-2 border-b border-[#21262d]">
            <span className="text-[#8b949e] text-xs">{label}</span>
            <div className="flex items-center gap-2">
                <span className="text-[#c9d1d9] font-mono text-sm">
                    {prefix}{typeof value === 'number' ? value.toLocaleString('en-IN') : value}{suffix}
                </span>
                {change !== undefined && change !== 0 && (
                    <span className={`text-xs font-mono ${change > 0 ? 'text-[#3fb950]' : 'text-[#f85149]'}`}>
                        {change > 0 ? '+' : ''}{change.toLocaleString('en-IN')}
                    </span>
                )}
            </div>
        </div>
    );
}

export function MetricsPanel({ instrumentKey }: MetricsPanelProps) {
    const data = useMetricsStore(state => state.data.get(instrumentKey));

    if (!data) {
        return (
            <div className="bg-[#0d1117] border border-[#30363d] rounded p-4">
                <div className="text-[#8b949e] text-sm">Metrics - Waiting...</div>
            </div>
        );
    }

    return (
        <div className="bg-[#0d1117] border border-[#30363d] rounded">
            {/* Header */}
            <div className="px-3 py-2 border-b border-[#30363d]">
                <span className="text-[#c9d1d9] font-medium text-sm">METRICS</span>
            </div>

            {/* Content */}
            <div className="p-3 space-y-3">
                {/* Pressure meter */}
                <PressureMeter bidQty={data.totalBidQty} askQty={data.totalAskQty} />

                {/* Metrics */}
                <div className="mt-3">
                    <MetricRow
                        label="Volume"
                        value={data.volumeTraded}
                    />
                    <MetricRow
                        label="Open Interest"
                        value={data.openInterest}
                        change={data.oiChange}
                    />
                    <MetricRow
                        label="ATP"
                        value={data.atp.toFixed(2)}
                        prefix="₹"
                    />
                    {data.impliedVolatility !== undefined && (
                        <MetricRow
                            label="IV"
                            value={(data.impliedVolatility * 100).toFixed(2)}
                            suffix="%"
                        />
                    )}
                </div>

                {/* Imbalance indicator */}
                <div className="pt-2 border-t border-[#30363d]">
                    <div className="flex items-center justify-between">
                        <span className="text-[#8b949e] text-xs">Imbalance</span>
                        <span className={`font-mono text-sm font-medium ${data.imbalance > 0.1 ? 'text-[#3fb950]' :
                                data.imbalance < -0.1 ? 'text-[#f85149]' : 'text-[#8b949e]'
                            }`}>
                            {data.imbalance > 0 ? '+' : ''}{data.imbalancePercent.toFixed(1)}%
                            {data.imbalance > 0.1 ? ' (BUYERS)' :
                                data.imbalance < -0.1 ? ' (SELLERS)' : ' (NEUTRAL)'}
                        </span>
                    </div>
                </div>

                {/* Circuit limits */}
                {(data.upperCircuit || data.lowerCircuit) && (
                    <div className="pt-2 border-t border-[#30363d] grid grid-cols-2 gap-2 text-xs">
                        {data.upperCircuit && (
                            <div className="text-[#3fb950]">
                                UC: ₹{data.upperCircuit.toFixed(2)}
                            </div>
                        )}
                        {data.lowerCircuit && (
                            <div className="text-[#f85149]">
                                LC: ₹{data.lowerCircuit.toFixed(2)}
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}

export default MetricsPanel;
