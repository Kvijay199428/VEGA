package com.vegatrader.upstox.terminal;

import com.vegatrader.upstox.auth.state.OperatorControlState;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Bloomberg-style Command Router for backend administrative actions.
 */
@Component
public class CommandRouter {

    private static final Logger log = LoggerFactory.getLogger(CommandRouter.class);

    private final OperatorControlState operatorControlState;
    private final Map<String, Runnable> commands = new HashMap<>();

    public CommandRouter(OperatorControlState operatorControlState) {
        this.operatorControlState = operatorControlState;
    }

    @PostConstruct
    void init() {
        commands.put("LOGIN", () -> log.info("LOGIN command received (No-op placeholder)"));
        commands.put("LOGOUT", () -> log.info("LOGOUT command received (No-op placeholder)"));
        commands.put("KILL SYSTEM", () -> {
            log.warn("KILL SYSTEM command executing...");
            operatorControlState.setAutomationEnabled(false);
        });
        commands.put("RESUME SYSTEM", () -> {
            log.info("RESUME SYSTEM command executing...");
            operatorControlState.setAutomationEnabled(true);
        });
    }

    public void execute(String input) {
        if (input == null)
            return;
        String normalized = input.toUpperCase().trim();
        commands.entrySet().stream()
                .filter(e -> normalized.startsWith(e.getKey()))
                .findFirst()
                .ifPresentOrElse(
                        e -> e.getValue().run(),
                        () -> log.warn("Unknown command: {}", input));
    }
}
