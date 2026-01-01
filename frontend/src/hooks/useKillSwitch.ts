import { useEffect } from "react";

/**
 * Hook to enable Global Kill Switch via Keyboard.
 * Press CTRL + ALT + K to immediately HALT automation on backend.
 */
export function useKillSwitch() {
    useEffect(() => {
        const handler = (e: KeyboardEvent) => {
            // Check for CTRL + ALT + K (Kill Switch)
            if (e.ctrlKey && e.altKey && (e.key === "k" || e.key === "K")) {
                console.warn("[KILL-SWITCH] Triggered via keyboard!");

                fetch("/api/operator/kill-switch?enabled=false", {
                    method: "POST"
                })
                    .then(() => alert("🔴 AUTOMATION HALTED BY OPERATOR KILL SWITCH"))
                    .catch(err => alert("Failed to trigger kill switch: " + err));
            }
        };

        window.addEventListener("keydown", handler);
        return () => window.removeEventListener("keydown", handler);
    }, []);
}
