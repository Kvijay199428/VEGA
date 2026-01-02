/**
 * GreeksPanel - Option Greeks Display
 * 
 * Bloomberg-style Greeks visualization:
 * - Delta bar (-1 to +1)
 * - Gamma/Theta/Vega/Rho values
 * - IV display
 * - Freeze control
 */

import { useGreeksStore } from '../../stores/greeksStore';

interface GreeksPanelProps {
    instrumentKey: string;
}

function DeltaBar({ delta }: { delta: number }) {
    return (
        <div className="flex items-center gap-2">
            <span className="text-[#8b949e] text-xs w-8">Δ</span>
            <div className="flex-1 h-3 bg-[#21262d] rounded-full overflow-hidden relative">
                {/* Center line */}
                <div className="absolute left-1/2 top-0 bottom-0 w-px bg-[#484f58]" />

                {/* Delta bar */}
                <div
                    className={`h-full transition-all ${delta >= 0 ? 'bg-[#3fb950]' : 'bg-[#f85149]'}`}
                    style={{
                        width: `${Math.abs(delta) * 50}%`,
                        marginLeft: delta >= 0 ? '50%' : `${50 - Math.abs(delta) * 50}%`
                    }}
                />
            </div>
            <span className={`text-xs font-mono w-12 text-right ${delta >= 0 ? 'text-[#3fb950]' : 'text-[#f85149]'
                }`}>
                {delta.toFixed(3)}
            </span>
        </div>
    );
}

function GreekValue({
    label,
    value,
    prev,
    suffix = ''
}: {
    label: string;
    value: number;
    prev?: number;
    suffix?: string;
}) {
    const changed = prev !== undefined && value !== prev;
    const increased = prev !== undefined && value > prev;

    return (
        <div className="flex items-center justify-between py-1 border-b border-[#21262d]">
            <span className="text-[#8b949e] text-xs">{label}</span>
            <div className="flex items-center gap-1">
                <span className={`font-mono text-sm ${changed ? (increased ? 'text-[#3fb950]' : 'text-[#f85149]') : 'text-[#c9d1d9]'
                    }`}>
                    {value.toFixed(4)}{suffix}
                </span>
                {changed && (
                    <span className={`text-xs ${increased ? 'text-[#3fb950]' : 'text-[#f85149]'}`}>
                        {increased ? '▲' : '▼'}
                    </span>
                )}
            </div>
        </div>
    );
}

export function GreeksPanel({ instrumentKey }: GreeksPanelProps) {
    const data = useGreeksStore(state => state.data.get(instrumentKey));
    const isFrozen = useGreeksStore(state => state.isFrozen(instrumentKey));
    const freeze = useGreeksStore(state => state.freeze);
    const resume = useGreeksStore(state => state.resume);

    if (!data) {
        return (
            <div className="bg-[#0d1117] border border-[#30363d] rounded p-4">
                <div className="text-[#8b949e] text-sm">Greeks - No data</div>
                <div className="text-[#6e7681] text-xs mt-1">
                    (Only available for options)
                </div>
            </div>
        );
    }

    return (
        <div className="bg-[#0d1117] border border-[#30363d] rounded">
            {/* Header */}
            <div className="flex items-center justify-between px-3 py-2 border-b border-[#30363d]">
                <div className="flex items-center gap-2">
                    <span className="text-[#c9d1d9] font-medium text-sm">GREEKS</span>
                    {isFrozen && (
                        <span className="text-[#f0c808] text-xs px-1 bg-[#f0c808]/20 rounded">
                            FROZEN
                        </span>
                    )}
                </div>
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

            {/* Content */}
            <div className="p-3 space-y-2">
                {/* Delta bar */}
                <DeltaBar delta={data.delta} />

                {/* Other Greeks */}
                <div className="mt-3">
                    <GreekValue label="Gamma (Γ)" value={data.gamma} prev={data.prevGamma} />
                    <GreekValue label="Theta (Θ)" value={data.theta} prev={data.prevTheta} />
                    <GreekValue label="Vega (ν)" value={data.vega} />
                    <GreekValue label="Rho (ρ)" value={data.rho} />
                </div>

                {/* IV */}
                {data.iv > 0 && (
                    <div className="mt-3 pt-2 border-t border-[#30363d]">
                        <div className="flex items-center justify-between">
                            <span className="text-[#8b949e] text-xs">Implied Volatility</span>
                            <span className="text-[#f0c808] font-mono text-sm font-medium">
                                {(data.iv * 100).toFixed(2)}%
                            </span>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}

export default GreeksPanel;
