import { useEffect } from "react";

/**
 * Hook for Bloomberg-style Command Palette.
 * Press CTRL + K to open command input.
 */
export function useCommandPalette() {
    useEffect(() => {
        const handler = (e: KeyboardEvent) => {
            // CTRL + K
            if (e.ctrlKey && (e.key === "k" || e.key === "K")) {
                e.preventDefault(); // Prevent browser 'Focus Search' default

                const cmd = prompt("TERMINAL COMMAND >");
                if (cmd && cmd.trim().length > 0) {
                    console.log("[TERMINAL] Sending: " + cmd);

                    fetch("/api/terminal/command", {
                        method: "POST",
                        headers: { "Content-Type": "text/plain" },
                        body: cmd
                    })
                        .then(res => {
                            if (res.ok) console.log("Command executed successfully");
                            else alert("Command execution failed");
                        })
                        .catch(err => alert("Terminal Error: " + err));
                }
            }
        };

        window.addEventListener("keydown", handler);
        return () => window.removeEventListener("keydown", handler);
    }, []);
}
