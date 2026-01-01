import { LiveMarketSnapshot } from "../types/market";

interface GreeksPanelProps {
    // If we had full Greek support in snapshot, we'd use it here.
    // Currently LiveMarketSnapshot has basic fields.
    // Assuming snapshot might be extended or we use a separate DTO.
    // For now, mocking or using placeholder if data missing.
    instrumentKey: string;
}

export function GreeksPanel({ instrumentKey }: GreeksPanelProps) {
    return (
        <div className="bg-[#1e1e1e] p-2 rounded border border-[#333] text-xs font-mono">
            <h3 className="text-gray-400 mb-2">{instrumentKey.split('|')[1]} Greeks</h3>
            <div className="grid grid-cols-2 gap-2 text-gray-300">
                <div>Delta: <span className="text-blue-400">0.54</span></div>
                <div>Gamma: <span className="text-blue-400">0.05</span></div>
                <div>Theta: <span className="text-red-400">-12.4</span></div>
                <div>Vega : <span className="text-green-400">45.2</span></div>
            </div>
        </div>
    )
}
