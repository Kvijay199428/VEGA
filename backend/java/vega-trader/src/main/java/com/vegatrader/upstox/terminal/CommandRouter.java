package com.vegatrader.upstox.terminal;

import com.vegatrader.upstox.auth.state.OperatorControlState;
import com.vegatrader.util.format.TextFormatter;
import com.vegatrader.util.time.TimeProvider;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Bloomberg-style Command Router for backend administrative actions.
 * 
 * <p>
 * Uses TimeProvider and TextFormatter for deterministic, locale-independent
 * execution.
 */
@Component("upstoxCommandRouter")
public class CommandRouter {

    private static final Logger log = LoggerFactory.getLogger(CommandRouter.class);

    private final OperatorControlState operatorControlState;
    private final TimeProvider timeProvider;
    private final TextFormatter formatter;
    private final Map<String, Runnable> commands = new HashMap<>();

    public CommandRouter(OperatorControlState operatorControlState,
            TimeProvider timeProvider,
            TextFormatter formatter) {
        this.operatorControlState = operatorControlState;
        this.timeProvider = timeProvider;
        this.formatter = formatter;
    }

    @PostConstruct
    void init() {
        commands.put("LOGIN", () -> log.info("[{}] LOGIN command received (No-op placeholder)",
                formatter.formatInstant(timeProvider.now())));
        commands.put("LOGOUT", () -> log.info("[{}] LOGOUT command received (No-op placeholder)",
                formatter.formatInstant(timeProvider.now())));
        commands.put("KILL SYSTEM", () -> {
            log.warn("[{}] KILL SYSTEM command executing...", formatter.formatInstant(timeProvider.now()));
            operatorControlState.setAutomationEnabled(false);
        });
        commands.put("RESUME SYSTEM", () -> {
            log.info("[{}] RESUME SYSTEM command executing...", formatter.formatInstant(timeProvider.now()));
            operatorControlState.setAutomationEnabled(true);
        });
    }

    public void execute(String input) {
        if (input == null)
            return;
        // Use locale-safe uppercase conversion
        String normalized = formatter.upper(input).trim();
        commands.entrySet().stream()
                .filter(e -> normalized.startsWith(e.getKey()))
                .findFirst()
                .ifPresentOrElse(
                        e -> e.getValue().run(),
                        () -> log.warn("[{}] Unknown command: {}", formatter.formatInstant(timeProvider.now()), input));
    }
}
