package com.vegatrader.upstox.auth.state;

import org.springframework.stereotype.Component;

/**
 * Global state for Operator Controls (Kill Switch).
 */
@Component
public class OperatorControlState {
    private volatile boolean automationEnabled = true;

    public boolean isAutomationEnabled() {
        return automationEnabled;
    }

    public void setAutomationEnabled(boolean automationEnabled) {
        this.automationEnabled = automationEnabled;
    }
}
