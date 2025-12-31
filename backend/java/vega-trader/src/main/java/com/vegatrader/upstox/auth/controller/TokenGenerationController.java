package com.vegatrader.upstox.auth.controller;

import com.vegatrader.upstox.auth.db.ApiName;
import com.vegatrader.upstox.auth.service.TokenGenerationService;
import com.vegatrader.upstox.auth.service.TokenGenerationService.GenerationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for token generation.
 * POST /api/auth/upstox/tokens/generate
 *
 * @since 2.2.0
 */
@RestController
@RequestMapping("/api/auth/upstox/tokens")
public class TokenGenerationController {

    private static final Logger logger = LoggerFactory.getLogger(TokenGenerationController.class);

    private final TokenGenerationService generationService;

    public TokenGenerationController() {
        this.generationService = new TokenGenerationService();
    }

    /**
     * Generate tokens.
     * 
     * Request body:
     * {
     * "mode": "ALL" | "INVALID_ONLY" | "PARTIAL",
     * "apiNames": ["WEBSOCKET2", "WEBSOCKET3"] // Required if mode=PARTIAL
     * }
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateTokens(@RequestBody GenerateRequest request) {
        logger.info("Token generation request: mode={}", request.getMode());

        List<GenerationResult> results;

        switch (request.getMode()) {
            case ALL:
                results = generationService.generateAll();
                break;
            case INVALID_ONLY:
                results = generationService.generateInvalidOnly();
                break;
            case PARTIAL:
                if (request.getApiNames() == null || request.getApiNames().isEmpty()) {
                    return ResponseEntity.badRequest().body(
                            Map.of("error", "apiNames required for PARTIAL mode"));
                }
                List<ApiName> apis = request.getApiNames().stream()
                        .map(ApiName::fromString)
                        .collect(Collectors.toList());
                results = generationService.generatePartial(apis);
                break;
            default:
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Invalid mode: " + request.getMode()));
        }

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("mode", request.getMode().name());
        response.put("totalRequested", results.size());
        response.put("successCount", results.stream().filter(GenerationResult::isSuccess).count());
        response.put("failureCount", results.stream().filter(r -> !r.isSuccess()).count());
        response.put("results", results.stream()
                .map(r -> Map.of(
                        "apiName", r.getApiName().name(),
                        "success", r.isSuccess(),
                        "message", r.getMessage()))
                .collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    /**
     * Generate single token.
     */
    @PostMapping("/generate/{apiName}")
    public ResponseEntity<Map<String, Object>> generateSingle(@PathVariable String apiName) {
        logger.info("Single token generation request: {}", apiName);

        try {
            ApiName api = ApiName.fromString(apiName);
            List<GenerationResult> results = generationService.generatePartial(List.of(api));

            GenerationResult result = results.isEmpty() ? new GenerationResult(api, false, "No result")
                    : results.get(0);

            Map<String, Object> response = new HashMap<>();
            response.put("apiName", result.getApiName().name());
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid API name: " + apiName));
        }
    }

    /**
     * Request DTO for token generation.
     */
    public static class GenerateRequest {
        private TokenGenerationService.GenerationMode mode;
        private List<String> apiNames;

        public TokenGenerationService.GenerationMode getMode() {
            return mode;
        }

        public void setMode(TokenGenerationService.GenerationMode mode) {
            this.mode = mode;
        }

        public List<String> getApiNames() {
            return apiNames;
        }

        public void setApiNames(List<String> apiNames) {
            this.apiNames = apiNames;
        }
    }
}
