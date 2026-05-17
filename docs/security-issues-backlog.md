# Security Issues Backlog

This document tracks security issues identified during repository reviews that remain pending and should be addressed in a future implementation block.

## Scope

- Reviewed services: `identity-service`, `catalog-service`, `library-service`, Docker local setup
- Review date: 2026-05-17
- Status legend: `Open` | `Partially Mitigated` | `Mitigated` | `Accepted for local dev`

## Open Issues

[None - all identified V1 security issues have been mitigated or accepted for local development.]

## Mitigated History

### 1. Auth rate limiting is shared across instances and proxy-aware

- **Severity**: Previously Medium
- **Service**: `identity-service`
- **Status**: Mitigated
- **Where**:
  - `services/identity-service/src/main/java/com/bookhub/identity/web/auth/ratelimit/AuthRateLimitInterceptor.java`
  - `services/identity-service/src/main/java/com/bookhub/identity/infrastructure/ratelimit/RedisAuthRateLimitStore.java`
- **Resolution summary**:
  Moved rate-limit counters from in-process memory to a shared Redis store. The system now uses fixed-window counters with TTL-backed expiration, ensuring globally consistent limits across multiple service instances.
  - Mitigated in commit `89d4f3e`.
  - Maintains trusted-proxy fingerprinting and forwarded-header sanitization model.
  - Fail-closed behavior on Redis unavailability (returns 503 Service Unavailable).
- **Audit Note**: The implementation is currently fixed-window, not sliding-window as previously claimed in documentation.

### 2. Refresh token replay during concurrent rotation

- **Severity**: Previously High
- **Service**: `identity-service`
- **Status**: Mitigated
- **Where**:
  - `services/identity-service/src/main/java/com/bookhub/identity/infrastructure/persistence/RefreshTokenJpaRepository.java`
- **Resolution summary**:
  The JPA query `findByTokenHashAndRevokedFalseAndExpiresAtAfter` now uses `@Lock(LockModeType.PESSIMISTIC_WRITE)`, which issues a `SELECT ... FOR UPDATE` at the database level. This serializes concurrent refresh attempts on the same token row, ensuring only one request succeeds.
  - Mitigated in commit `13163e0`.
  - Verified by `RefreshConcurrentReplayIntegrationTest`.

### 3. Refresh tokens stored as hashes

- **Severity**: Previously Medium
- **Service**: `identity-service`
- **Status**: Mitigated
- **Where**:
  - `services/identity-service/src/main/java/com/bookhub/identity/domain/auth/RefreshToken.java`
  - `services/identity-service/src/main/resources/db/migration/V5__hash_refresh_tokens.sql`
- **Resolution summary**:
  Refresh tokens are now stored as hashed values (SHA-256) instead of raw bearer credentials. This reduces the blast radius of database reads, dumps, or backup exposure.

### 4. Unsanitized `X-Request-Id` in HTTP logging

- **Severity**: Previously Medium
- **Services**: `identity-service`, `catalog-service`
- **Status**: Mitigated
- **Resolution summary**:
  Request IDs are accepted only when they match a strict allowlist (alphanumeric + `._-`) and maximum length (100). Invalid values are replaced with a server-generated UUID.

### 5. Catalog internal routes require authenticated service-to-service access

- **Severity**: Previously High
- **Service**: `catalog-service`
- **Status**: Mitigated
- **Where**:
  - `services/catalog-service/src/main/java/com/bookhub/catalog/config/SecurityConfig.java`
- **Resolution summary**:
  Catalog internal routes (`/api/v1/internal/**`) now require `ROLE_SERVICE`. Identity-service issues machine tokens for authorized callers.
- **Residual risk**:
  Operational hardening (e.g., token rotation, broader S2S auth consistency) remains a future block.

## Accepted for Local Development

### 6. Host-exposed database ports in Docker dev override

- **Severity**: Low
- **Scope**: `infrastructure/docker/compose.dev.yml`
- **Status**: Accepted for local dev
- **Rationale**:
  Host-exposed DB ports are intentional for local development and debugging. The base `compose.yml` does not expose them.
- **Audit Note**: `library-db` is currently missing from the dev override port mappings; this should be added for consistency.

## Current Verdict

After the 2026-05-17 audit of the reviewed scope, no active security issues remain in this backlog.

- The previously tracked auth rate-limiting issue is mitigated.
- The previously tracked refresh-token storage and replay issues are mitigated.
- The previously tracked request-ID sanitization issue is mitigated.
- The previously tracked catalog internal-route trust issue is mitigated.
- The Docker host-port note remains an intentional local-development exception, not an open security defect.

Future hardening ideas may still exist, but they should be tracked as new evidence-based findings rather than kept here as stale backlog items.
