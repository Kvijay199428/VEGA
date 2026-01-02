import { recordFault } from "./faultTimeline";
import { useOrderBookStore } from "../stores/orderBookStore";
import { useGreeksStore } from "../stores/greeksStore";
import { useFreezeControlStore } from "../stores/freezeControlStore";

type Command =
    | { type: "GO"; symbol: string }
    | { type: "BOOK"; instrumentKey: string }
    | { type: "FREEZE"; panel: "BOOK" | "GREEKS" | "ALL" }
    | { type: "RESUME"; panel: "BOOK" | "GREEKS" | "ALL" }
    | { type: "DEPTH"; level: number }
    | { type: "GREEKS"; action: "ON" | "OFF" }
    | { type: "SUBSCRIBE"; instrumentKey: string }
    | { type: "DIAG"; subtype: "AUTH_TRACE"; sessionId?: string }
    | { type: "UNKNOWN"; raw: string };

// Current context for commands
let currentInstrument: string | null = null;

export function setCurrentInstrument(instrumentKey: string) {
    currentInstrument = instrumentKey;
}

export function parseCommand(input: string): Command {
    const parts = input.trim().toUpperCase().split(" ");
    const cmd = parts[0];

    switch (cmd) {
        case "GO":
            if (parts[1]) {
                return { type: "GO", symbol: parts.slice(1).join(" ") };
            }
            break;

        case "BOOK":
            if (parts[1]) {
                return { type: "BOOK", instrumentKey: parts.slice(1).join("|") };
            }
            break;

        case "FREEZE":
            const freezePanel = parts[1] as "BOOK" | "GREEKS" | "ALL" | undefined;
            return { type: "FREEZE", panel: freezePanel || "ALL" };

        case "RESUME":
            const resumePanel = parts[1] as "BOOK" | "GREEKS" | "ALL" | undefined;
            return { type: "RESUME", panel: resumePanel || "ALL" };

        case "DEPTH":
            const level = parseInt(parts[1]);
            if (!isNaN(level) && [5, 10, 20].includes(level)) {
                return { type: "DEPTH", level };
            }
            break;

        case "GREEKS":
            if (parts[1] === "ON" || parts[1] === "OFF") {
                return { type: "GREEKS", action: parts[1] };
            }
            break;

        case "SUBSCRIBE":
        case "SUB":
            if (parts[1]) {
                return { type: "SUBSCRIBE", instrumentKey: parts.slice(1).join("|") };
            }
            break;
        case "DIAG":
            if (parts[1] === "AUTH" && parts[2] === "TRACE") {
                const sessionIdIdx = parts.indexOf("SESSION");
                const sessionId = sessionIdIdx !== -1 ? parts[sessionIdIdx + 1] : undefined;
                return { type: "DIAG", subtype: "AUTH_TRACE", sessionId };
            }
            break;
    }

    return { type: "UNKNOWN", raw: input };
}

export async function executeCommand(cmd: Command) {
    switch (cmd.type) {
        case "GO":
            console.log(`[Command] Navigating to symbol: ${cmd.symbol}`);
            window.location.hash = `/trade/${cmd.symbol}`;
            break;

        case "DIAG":
            if (cmd.subtype === "AUTH_TRACE") {
                console.log(`[DIAG] Fetching Auth Trace...`);
                // Use current session if not specified - hard to get from here without store access
                // For now, let's just log instructions if no session ID, or try to simplify
                // Actually, let's fetch from the DIAG endpoint
                // We need the session ID. 
                // We'll prompt user or use a default if we can access it.
                // But commandRouter doesn't have access to AuthState.
                // We will dispatch an event and let the UI handle it or fetch if session provided.

                if (cmd.sessionId) {
                    try {
                        const res = await fetch(`/api/auth/trace?sessionId=${cmd.sessionId}`);
                        if (res.ok) {
                            const trace = await res.json();
                            console.table(trace);
                        } else {
                            console.error("[DIAG] Failed to fetch trace", res.status);
                        }
                    } catch (e) {
                        console.error("[DIAG] Error fetching trace", e);
                    }
                } else {
                    console.warn("[DIAG] Session ID required. Usage: DIAG AUTH TRACE SESSION <id>");
                }
            }
            break;

        case "BOOK":
            console.log(`[Command] Opening order book: ${cmd.instrumentKey}`);
            setCurrentInstrument(cmd.instrumentKey);
            // Dispatch event for UI to handle
            window.dispatchEvent(new CustomEvent('vega:open-book', {
                detail: { instrumentKey: cmd.instrumentKey }
            }));
            break;

        case "FREEZE":
            console.log(`[Command] Freezing: ${cmd.panel}`);
            if (cmd.panel === "ALL") {
                useFreezeControlStore.getState().freezeAll();
            } else if (cmd.panel === "BOOK" && currentInstrument) {
                useOrderBookStore.getState().freeze(currentInstrument);
            } else if (cmd.panel === "GREEKS" && currentInstrument) {
                useGreeksStore.getState().freeze(currentInstrument);
            }
            break;

        case "RESUME":
            console.log(`[Command] Resuming: ${cmd.panel}`);
            if (cmd.panel === "ALL") {
                useFreezeControlStore.getState().resumeAll();
            } else if (cmd.panel === "BOOK" && currentInstrument) {
                useOrderBookStore.getState().resume(currentInstrument);
            } else if (cmd.panel === "GREEKS" && currentInstrument) {
                useGreeksStore.getState().resume(currentInstrument);
            }
            break;

        case "DEPTH":
            console.log(`[Command] Setting depth: ${cmd.level}`);
            if (currentInstrument) {
                useOrderBookStore.getState().setDisplayDepth(currentInstrument, cmd.level);
            }
            break;

        case "GREEKS":
            console.log(`[Command] Greeks: ${cmd.action}`);
            window.dispatchEvent(new CustomEvent('vega:greeks-toggle', {
                detail: { visible: cmd.action === "ON" }
            }));
            break;

        case "SUBSCRIBE":
            console.log(`[Command] Subscribe: ${cmd.instrumentKey}`);
            window.dispatchEvent(new CustomEvent('vega:subscribe', {
                detail: { instrumentKey: cmd.instrumentKey }
            }));
            break;

        default:
            console.warn(`[Command] Unknown command: ${cmd.raw}`);
            recordFault({
                ts: Date.now(),
                level: "AMBER",
                source: "SYSTEM",
                message: `Unknown command: ${cmd.raw}`
            });
    }
}

