package com.vegatrader.upstox.auth.controller;

import com.vegatrader.upstox.auth.state.OperatorControlState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operator")
public class OperatorController {

    private static final Logger log = LoggerFactory.getLogger(OperatorController.class);

    private final OperatorControlState operatorControlState;

    public OperatorController(OperatorControlState operatorControlState) {
        this.operatorControlState = operatorControlState;
    }

    @PostMapping("/kill-switch")
    public void kill(@RequestParam boolean enabled) {
        operatorControlState.setAutomationEnabled(enabled);
        if (!enabled) {
            log.warn("[OPERATOR] 🔴 KILL SWITCH ACTIVATED - Automation HALTED");
        } else {
            log.info("[OPERATOR] 🟢 Automation Resumed");
        }
    }
}
