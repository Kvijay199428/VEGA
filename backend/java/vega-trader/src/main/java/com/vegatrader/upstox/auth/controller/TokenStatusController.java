package com.vegatrader.upstox.auth.controller;

import com.vegatrader.upstox.auth.db.ApiName;
import com.vegatrader.upstox.auth.service.TokenDecisionReport;
import com.vegatrader.upstox.auth.service.TokenGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for token status.
 * GET /api/auth/upstox/tokens/status
 *
 * @since 2.2.0
 */
@RestController
@RequestMapping("/api/auth/upstox/tokens")
public class TokenStatusController {

    private final TokenGenerationService generationService;

    public TokenStatusController() {
        this.generationService = new TokenGenerationService();
    }

    /**
     * Get status of all tokens.
     * 
     * Response:
     * {
     * "valid": ["PRIMARY", "WEBSOCKET1"],
     * "invalid": ["WEBSOCKET2"],
     * "missing": ["OPTIONCHAIN2"],
     * "total": 6
     * }
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        TokenDecisionReport report = generationService.getStatus();

        Map<String, Object> response = new HashMap<>();
        response.put("valid", toStringList(report.getValid()));
        response.put("invalid", toStringList(report.getInvalid()));
        response.put("missing", toStringList(report.getMissing()));
        response.put("total", ApiName.count());
        response.put("validCount", report.getValidCount());
        response.put("invalidCount", report.getInvalidCount());
        response.put("missingCount", report.getMissingCount());
        response.put("allValid", report.allValid());

        return ResponseEntity.ok(response);
    }

    /**
     * Get detailed status (includes CLI-friendly summary).
     */
    @GetMapping("/status/detailed")
    public ResponseEntity<Map<String, Object>> getDetailedStatus() {
        TokenDecisionReport report = generationService.getStatus();

        Map<String, Object> response = new HashMap<>();
        response.put("valid", toStringList(report.getValid()));
        response.put("invalid", toStringList(report.getInvalid()));
        response.put("missing", toStringList(report.getMissing()));
        response.put("total", ApiName.count());
        response.put("summary", report.prettyPrint());
        response.put("needRegeneration", toStringList(report.getNeedRegeneration()));

        return ResponseEntity.ok(response);
    }

    private List<String> toStringList(List<ApiName> apis) {
        return apis.stream().map(Enum::name).collect(Collectors.toList());
    }
}
