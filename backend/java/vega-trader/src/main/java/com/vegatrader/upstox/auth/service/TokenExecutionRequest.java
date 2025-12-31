package com.vegatrader.upstox.auth.service;

import com.vegatrader.upstox.auth.db.ApiName;

import java.util.List;

/**
 * Token execution request for async orchestrator.
 *
 * @since 2.3.0
 */
public class TokenExecutionRequest {

    private TokenGenerationService.GenerationMode mode;
    private List<ApiName> apiNames;

    private TokenExecutionRequest(TokenGenerationService.GenerationMode mode, List<ApiName> apiNames) {
        this.mode = mode;
        this.apiNames = apiNames;
    }

    public static TokenExecutionRequest all() {
        return new TokenExecutionRequest(TokenGenerationService.GenerationMode.ALL, null);
    }

    public static TokenExecutionRequest invalidOnly() {
        return new TokenExecutionRequest(TokenGenerationService.GenerationMode.INVALID_ONLY, null);
    }

    public static TokenExecutionRequest partial(List<ApiName> apis) {
        return new TokenExecutionRequest(TokenGenerationService.GenerationMode.PARTIAL, apis);
    }

    public TokenGenerationService.GenerationMode getMode() {
        return mode;
    }

    public List<ApiName> getApiNames() {
        return apiNames;
    }
}
