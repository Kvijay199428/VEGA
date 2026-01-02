import { useEffect } from "react";
import { activateKillSwitch } from "./killSwitch";

export function useKillSwitch() {
    useEffect(() => {
        const handler = (e: KeyboardEvent) => {
            if (e.ctrlKey && e.altKey && e.code === "KeyK") {
                e.preventDefault();
                activateKillSwitch("KEYBOARD");
            }
        };
        window.addEventListener("keydown", handler);
        return () => window.removeEventListener("keydown", handler);
    }, []);
}
