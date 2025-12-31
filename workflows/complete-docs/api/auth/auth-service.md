# Authentication Service

The Authentication Service handles the lifecycle of Upstox Access Tokens, ensuring zero manual intervention.

## 1. Async Token Orchestrator
**Class**: `com.vegatrader.upstox.auth.service.AsyncTokenOrchestrator`

Orchestrates the token generation process asynchronously.
- **Fast Path**: Checks validity of all tokens in parallel (6 threads). If all are valid, exits immediately (< 300ms).
- **Parallel Verification**: Uses `ProfileVerificationService` to validate tokens against live Upstox API.
- **Single-Threaded Generation**: Uses a single Selenium instance to prevent session conflicts during OTP/TOTP entry.

## 2. Resume Orchestrator
**Class**: `com.vegatrader.upstox.auth.service.ResumeOrchestrator`

Provides deterministic resume-on-failure capabilities.
- **State Persistence**: Saves execution state (Successful APIs, Next API) to allow resuming from the exact point of failure.
- **Broker Safety**: Prevents "hammering" the broker login page provided repeated failures.

## 3. Cooldown Manager
**Class**: `com.vegatrader.upstox.auth.service.CooldownManager`

Handles broker-imposed rate limiting (Throttling).
- **Strategy**: 11-minute hard wait (10 min Upstox limit + 1 min buffer).
- **Trigger**: Detects `BrokerCooldownException` or specific timeout patterns (e.g., waiting for PIN).
- **Action**: Stops automation, logs the event, waits for 11 mins, then auto-resumes via `ResumeOrchestrator`.
