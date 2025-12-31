package com.vegatrader.upstox.api.cli.controller;

import com.vegatrader.upstox.api.order.ratelimit.RateLimitService;
import com.vegatrader.upstox.api.order.settings.OrderSettingsService;
import com.vegatrader.upstox.api.settings.service.AdminSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * CLI Controller for administrative commands.
 * Replaces Spring Shell integration per requirement for web-based CLI.
 * 
 * Supports commands:
 * - system status
 * - rate-limit status
 * - settings get/set
 * 
 * @since 4.9.0
 */
@RestController
@RequestMapping("/api/v1/cli")
public class AdminCLIController {

    private final AdminSettingsService adminSettingsService;
    private final RateLimitService rateLimitService;
    private final OrderSettingsService orderSettingsService;

    public AdminCLIController(
            AdminSettingsService adminSettingsService,
            RateLimitService rateLimitService,
            OrderSettingsService orderSettingsService) {
        this.adminSettingsService = adminSettingsService;
        this.rateLimitService = rateLimitService;
        this.orderSettingsService = orderSettingsService;
    }

    /**
     * Execute CLI command.
     */
    @PostMapping("/execute")
    public ResponseEntity<CLIResponse> executeCommand(@RequestBody CLIRequest request) {
        String commandBase = request.command().trim();
        String[] parts = commandBase.split("\\s+");

        if (parts.length == 0) {
            return ResponseEntity.badRequest().body(new CLIResponse("Empty command", false));
        }

        try {
            String output = switch (parts[0].toLowerCase()) {
                case "help" -> showHelp();
                case "system" -> handleSystemCommand(parts);
                case "settings" -> handleSettingsCommand(parts);
                case "ratelimit" -> handleRateLimitCommand(parts);
                default -> "Unknown command: " + parts[0] + ". Type 'help' for usage.";
            };

            return ResponseEntity.ok(new CLIResponse(output, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new CLIResponse("Error: " + e.getMessage(), false));
        }
    }

    private String showHelp() {
        return """
                Available commands:
                - system status
                - settings get <key>
                - settings set <key> <value>
                - ratelimit status <user_id> <ip>
                """;
    }

    private String handleSystemCommand(String[] parts) {
        if (parts.length < 2)
            return "Usage: system status";

        if ("status".equalsIgnoreCase(parts[1])) {
            return "System Status: ONLINE\n" +
                    "Kill Switch: " + (adminSettingsService.isKillSwitchEnabled() ? "ENABLED" : "DISABLED") + "\n" +
                    "Maintenance Window: "
                    + (orderSettingsService.isMaintenanceWindowEnabled() ? "ACTIVE" : "INACTIVE");
        }
        return "Unknown system command";
    }

    private String handleSettingsCommand(String[] parts) {
        if (parts.length < 3)
            return "Usage: settings <get|set> <key> [value]";

        String action = parts[1];
        String key = parts[2];

        if ("get".equalsIgnoreCase(action)) {
            String value = adminSettingsService.getSettingValue(key);
            return key + " = " + (value != null ? value : "(not set)");
        } else if ("set".equalsIgnoreCase(action)) {
            if (parts.length < 4)
                return "Usage: settings set <key> <value>";
            String value = parts[3];
            var result = adminSettingsService.updateSetting(key, value, "CLI_USER", "MANUAL", "CLI Update");
            return result.success() ? "Updated " + key : "Failed: " + result.error();
        }
        return "Unknown settings command";
    }

    private String handleRateLimitCommand(String[] parts) {
        if (parts.length < 4)
            return "Usage: ratelimit status <user_id> <ip>";

        String userId = parts[2];
        String ip = parts[3];

        var status = rateLimitService.getStatus(userId, ip);
        return String.format("Rate Limit Status:\nRemaining: %d\nReset (ms): %d",
                status.remainingRequests(), status.resetWindowMs());
    }

    public record CLIRequest(String command) {
    }

    public record CLIResponse(String output, boolean success) {
    }
}
