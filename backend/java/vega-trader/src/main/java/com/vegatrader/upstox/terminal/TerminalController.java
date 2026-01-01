package com.vegatrader.upstox.terminal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/terminal")
public class TerminalController {

    private static final Logger log = LoggerFactory.getLogger(TerminalController.class);

    private final CommandRouter commandRouter;

    public TerminalController(CommandRouter commandRouter) {
        this.commandRouter = commandRouter;
    }

    @PostMapping("/command")
    public void executeCommand(@RequestBody String command) {
        log.info("[TERMINAL] Executing: {}", command);
        try {
            commandRouter.execute(command);
        } catch (Exception e) {
            log.error("[TERMINAL] Execution failed", e);
            throw e;
        }
    }
}
