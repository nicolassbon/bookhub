# Security Issues Backlog

This document tracks security issues identified during repository reviews that remain pending and should be addressed in a future implementation block.

## Scope

- Reviewed services: `identity-service`, `catalog-service`, Docker local setup
- Review date: 2026-04-20
- Status legend: `Open` | `Mitigated` | `Accepted for local dev`

## Open Issues

### 1. Refresh token replay during concurrent rotation

- **Severity**: High
- **Service**: `identity-service`
- **Status**: Open
- **Where**:
  - `services/identity-service/src/main/java/com/bookhub/identity/application/auth/RefreshSessionService.java`
  - refresh token persistence lookup/update flow
- **Risk**:
  Two concurrent refresh requests can consume the same active refresh token before revocation is durably enforced, resulting in multiple valid successor sessions.
- **Why it matters**:
  This weakens refresh-token rotation guarantees and increases the impact of stolen refresh tokens.
- **Recommended direction**:
  Make refresh-token consumption atomic at the persistence boundary.
  Options:
  - `SELECT ... FOR UPDATE`
  - optimistic locking with a version field
  - conditional update (`UPDATE ... WHERE revoked = false`) that must affect exactly one row before issuing the next token

### 2. Refresh tokens stored in plaintext in the database

- **Severity**: Medium
- **Service**: `identity-service`
- **Status**: Open
- **Where**:
  - `services/identity-service/src/main/java/com/bookhub/identity/domain/auth/RefreshToken.java`
  - `services/identity-service/src/main/resources/db/migration/V2__create_refresh_tokens_table.sql`
- **Risk**:
  A database read, dump, or backup exposure reveals active refresh tokens that can be reused directly as bearer credentials.
- **Why it matters**:
  This turns a data exposure event into active session hijacking until token expiry or revocation.
- **Recommended direction**:
  Store only a hashed or HMACed representation of refresh tokens.
  A practical model is `token_id + secret`, where the database stores the identifier plus a non-reversible representation of the secret portion.

### 3. Rate limiting trusts unverified forwarded headers and uses unbounded in-memory state

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

### 4. Unsanitized `X-Request-Id` in HTTP logging

- **Severity**: Previously Medium
- **Services**: `identity-service`, `catalog-service`
- **Status**: Mitigated
- **Resolution summary**:
  Request IDs are now accepted only when they match a strict allowlist and maximum length. Invalid values are replaced with a server-generated UUID.

## Accepted for Local Development

### 5. Host-exposed database ports in Docker dev override

- **Severity**: Low
- **Scope**: `infrastructure/docker/compose.dev.yml`
- **Status**: Accepted for local dev
- **Rationale**:
  Host-exposed DB ports are intentional for local development and debugging. The base `compose.yml` no longer exposes them by default.
- **Follow-up**:
  Keep this separation between base and dev override. Do not treat `compose.dev.yml` as a production-like deployment artifact.

## Suggested Next Security Block

Recommended implementation order:

1. Fix concurrent refresh-token replay
2. Stop storing refresh tokens in plaintext
3. Harden or redesign rate limiting

These three items provide the highest security value for the current repository state.
