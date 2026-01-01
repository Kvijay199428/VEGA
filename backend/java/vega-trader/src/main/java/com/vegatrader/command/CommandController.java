package com.vegatrader.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/command")
public class CommandController {

    @Autowired
    private CommandRouter commandRouter;

    @PostMapping
    public ResponseEntity<Map<String, Object>> executeCommand(@RequestBody Map<String, String> payload) {
        String command = payload.get("command");
        if (command == null || command.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Command is empty"));
        }

        return ResponseEntity.ok(commandRouter.execute(command));
    }
}
