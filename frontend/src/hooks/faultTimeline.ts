
export interface FaultEvent {
    ts: number;
    level: "GREEN" | "AMBER" | "RED";
    source: "AUTH_WS" | "MARKET_WS" | "SYSTEM";
    message: string;
}

const MAX = 500;
const faults: FaultEvent[] = [];

export function recordFault(e: FaultEvent) {
    faults.push(e);
    if (faults.length > MAX) faults.shift();
    console.log(`[FAULT] ${e.level} | ${e.source} | ${e.message}`);
}

export function getFaultTimeline() {
    return [...faults];
}
