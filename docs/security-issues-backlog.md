# Security Issues Backlog

This document tracks security issues identified during repository reviews that remain pending and should be addressed in a future implementation block.

## Scope

- Reviewed services: `identity-service`, `catalog-service`, Docker local setup
- Review date: 2026-04-20
- Status legend: `Open` | `Partially Mitigated` | `Mitigated` | `Accepted for local dev`

## Open Issues

### 1. Catalog service HTTP boundary is explicit, but internal route trust is still network-based

- **Severity**: High
- **Service**: `catalog-service`
- **Status**: Partially Mitigated
- **Where**:
  - `services/catalog-service/pom.xml`
  - `services/catalog-service/src/main/java/com/bookhub/catalog/web/internal/InternalBookController.java`
- **Current evidence**:
  - `spring-boot-starter-security` and resource-server support are active in `pom.xml`.
  - `services/catalog-service/src/main/java/com/bookhub/catalog/config/SecurityConfig.java` defines explicit rules and deny-by-default behavior (`anyRequest().authenticated()`).
  - `GET /api/v1/books/**` is public; admin mutations require `ROLE_ADMIN`.
  - `services/catalog-service/src/test/java/com/bookhub/catalog/SecurityIntegrationTest.java` covers core boundary behavior.
- **Residual risk**:
  `GET /api/v1/internal/**` is currently `permitAll()`. The route is logically internal but still relies on trusted network topology/gateway placement rather than service-level authn/authz.
- **Next hardening step**:
  Require authenticated service-to-service access (or equivalent trusted principal enforcement) for `/api/v1/internal/**`.

### 2. Auth rate limiting is proxy-aware and bounded, but still node-local

- **Severity**: Medium
- **Service**: `identity-service`
- **Status**: Partially Mitigated
- **Where**:
  - `services/identity-service/src/main/java/com/bookhub/identity/web/auth/ratelimit/AuthRateLimitInterceptor.java`
  - `services/identity-service/src/main/java/com/bookhub/identity/web/auth/AuthWebMvcConfig.java`
- **Current evidence**:
  - `AuthRateLimitInterceptor` only uses `X-Forwarded-For` when requests come from configured trusted proxy CIDRs.
  - The forwarded chain is sanitized and bounded (`MAX_FORWARDED_HOPS`).
  - In-memory state now has bounded lifecycle controls (`maxTrackedKeys`, stale-entry TTL cleanup).
  - `AuthRateLimitForwardedHeaderWebTest` verifies trusted-proxy forwarded-header behavior.
- **Residual risk**:
  Rate limiting remains in-process memory per instance, so limits are not globally consistent under horizontal scale and can still be reset by restarts.
- **Next hardening step**:
  Move counters to a shared TTL-backed store and keep the same proxy-trust model.

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

1. Require authenticated/authorized service-to-service access on `catalog-service` internal routes
2. Harden or redesign rate limiting for multi-instance and proxy-aware deployments
3. Continue security review across gateway-to-service and service-to-service boundaries

These items provide the highest security value for the current repository state.
