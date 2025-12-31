# Token Persistence Implementation

## Phase 1: Database Layer
- [x] Create `ApiName` enum (6 API types)
- [x] Create `UpstoxTokenEntity` (exact schema mapping)
- [x] Create `SqliteDataSourceFactory` (auto-detection)
- [x] Create `UpstoxTokenRepository` interface
- [x] Create `UpstoxTokenRepositoryImpl` (REPLACE semantics)

## Phase 2: Validity Engine
- [x] Create `TokenValidityService` (time-based check)
- [x] Create `ProfileVerificationService` (API check)
- [x] Create `TokenDecisionReport` DTO
- [x] Create `TokenDecisionEngine` (classify tokens)

## Phase 3: Integration
- [x] Create `UpstoxTokenMapper` (result → entity)
- [x] Create `TokenGenerationService` (orchestrator)
- [ ] Update `OAuthLoginAutomationV2` to persist tokens (handled by TokenGenerationService)

## Phase 4: REST API
- [x] Create `TokenStatusController` (GET /api/auth/upstox/tokens/status)
- [x] Create `TokenGenerationController` (POST /api/auth/upstox/tokens/generate)

## Phase 5: Testing
- [/] Compile and verify

