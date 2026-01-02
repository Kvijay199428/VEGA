
let killed = false;
let listeners: ((k: boolean) => void)[] = [];

export function activateKillSwitch(reason = "MANUAL_KILL") {
    killed = true;
    listeners.forEach(l => l(true));

    console.warn("[KILL SWITCH] ACTIVATED:", reason);
    // Here we could also forcibly close sockets
}

export function resetKillSwitch() {
    killed = false;
    listeners.forEach(l => l(false));
    console.log("[KILL SWITCH] RESET - System returning to normal.");
}

export function isKilled() {
    return killed;
}

export function onKillSwitch(cb: (k: boolean) => void) {
    listeners.push(cb);
    return () => {
        listeners = listeners.filter(l => l !== cb);
    };
}
