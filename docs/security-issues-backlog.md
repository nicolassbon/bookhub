# Security Issues Backlog

This document tracks security issues identified during repository reviews that remain pending and should be addressed in a future implementation block.

## Scope

- Reviewed services: `identity-service`, `catalog-service`, Docker local setup
- Review date: 2026-04-20
- Status legend: `Open` | `Mitigated` | `Accepted for local dev`

## Open Issues

### 1. Catalog service lacks explicit HTTP security boundaries

- **Severity**: High
- **Service**: `catalog-service`
- **Status**: Open
- **Where**:
  - `services/catalog-service/pom.xml`
  - `services/catalog-service/src/main/java/com/bookhub/catalog/web/internal/InternalBookController.java`
- **Risk**:
  Public and internal catalog HTTP endpoints are exposed without an explicit Spring Security policy.
- **Why it matters**:
  Internal routes are only internal by convention unless authentication and authorization rules enforce that boundary.
- **Recommended direction**:
  Add Spring Security to the service, define a deny-by-default `SecurityFilterChain`, and make public versus internal access rules explicit.

### 2. Rate limiting still depends on trusted proxy configuration and local in-memory state

- **Severity**: Medium
- **Service**: `identity-service`
- **Status**: Open
- **Where**:
  - `services/identity-service/src/main/java/com/bookhub/identity/web/auth/ratelimit/AuthRateLimitInterceptor.java`
  - `services/identity-service/src/main/java/com/bookhub/identity/web/auth/AuthWebMvcConfig.java`
- **Risk**:
  Attackers can spoof `X-Forwarded-For` to bypass rate limiting, and the in-memory key map can grow without eviction.
- **Why it matters**:
  This weakens abuse protection on `login`, `register`, and `forgot-password`, and may create avoidable memory pressure over time.
- **Recommended direction**:
  - Trust forwarded headers only when requests come through a known proxy or gateway
  - Move rate limiting to a bounded shared store with TTL/eviction
  - Keep semantics consistent across instances if the service is scaled out

## Recently Mitigated Issues

### 3. Refresh token replay during concurrent rotation

- **Severity**: Previously High
- **Service**: `identity-service`
- **Status**: Mitigated
- **Resolution summary**:
  The JPA query `findByTokenHashAndRevokedFalseAndExpiresAtAfter` now uses `@Lock(LockModeType.PESSIMISTIC_WRITE)`, which issues a `SELECT ... FOR UPDATE` at the database level. This serializes concurrent refresh attempts on the same token row, ensuring only one request succeeds.
  Mitigated in commit `13163e0 fix(identity): harden refresh token storage and auth throttling`.
  Verified by `RefreshConcurrentReplayIntegrationTest`, which submits two simultaneous refresh requests with the same token against a real PostgreSQL database and asserts exactly one succeeds.

### 4. Refresh tokens no longer stored in plaintext in the database

- **Severity**: Previously Medium
- **Service**: `identity-service`
- **Status**: Mitigated
- **Where**:
  - `services/identity-service/src/main/java/com/bookhub/identity/domain/auth/RefreshToken.java`
  - `services/identity-service/src/main/resources/db/migration/V5__hash_refresh_tokens.sql`
- **Resolution summary**:
  Refresh tokens are now stored as hashed values instead of raw bearer credentials. This reduces the blast radius of database reads, dumps, or backup exposure.

### 5. Unsanitized `X-Request-Id` in HTTP logging

- **Severity**: Previously Medium
- **Services**: `identity-service`, `catalog-service`
- **Status**: Mitigated
- **Resolution summary**:
  Request IDs are now accepted only when they match a strict allowlist and maximum length. Invalid values are replaced with a server-generated UUID.

## Accepted for Local Development

### 6. Host-exposed database ports in Docker dev override

- **Severity**: Low
- **Scope**: `infrastructure/docker/compose.dev.yml`
- **Status**: Accepted for local dev
- **Rationale**:
  Host-exposed DB ports are intentional for local development and debugging. The base `compose.yml` no longer exposes them by default.
- **Follow-up**:
  Keep this separation between base and dev override. Do not treat `compose.dev.yml` as a production-like deployment artifact.

## Suggested Next Security Block

Recommended implementation order:

1. Add explicit HTTP security boundaries to `catalog-service`
2. Harden or redesign rate limiting for multi-instance and proxy-aware deployments
3. Continue security review across gateway-to-service and service-to-service boundaries

These items provide the highest security value for the current repository state.
