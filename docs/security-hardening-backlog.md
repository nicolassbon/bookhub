# Security Hardening Backlog

This document tracks future security hardening opportunities for BookHub after the active issues previously reviewed for V1 were closed. It is intentionally separate from an incident or defect backlog.

## Current status

- No active security defects are currently tracked in this document.
- Items listed here are hardening opportunities, consistency improvements, or future review targets.
- Add a new item only when there is clear evidence, scope, and an expected security outcome.

## Candidate hardening tracks

### 1. Service-to-service authentication consistency

**Why it matters:** BookHub now protects catalog internal routes with service tokens. The next maturity step is to make service-to-service authentication expectations equally explicit across every internal integration that may appear later.

**Potential scope:**
- standardize internal-route naming and auth rules across services
- document token rotation and secret rotation expectations for service credentials
- define audit expectations for service-token issuance and failed machine-auth attempts

### 2. Auth abuse-control maturity

**Why it matters:** The current Redis-backed rate limiter is multi-instance safe and fail-closed, but abuse controls can still be hardened over time as traffic patterns become clearer.

**Potential scope:**
- evaluate whether fixed-window semantics are sufficient for login and service-token abuse patterns
- add endpoint-specific telemetry and alert thresholds for repeated blocking events
- review whether administrative or machine-auth endpoints need separate operational thresholds

### 3. Library-service security review deepening

**Why it matters:** Library now owns the broadest user-facing surface in V1, including reviews, goals, notifications, admin moderation, and metrics.

**Potential scope:**
- re-review ownership and authorization checks across review moderation and metrics endpoints
- verify that future internal integrations do not reuse end-user tokens for machine flows
- review abuse resistance for review and notification endpoints under higher load assumptions

### 4. Local-to-production security boundary hygiene

**Why it matters:** The repository intentionally keeps some developer conveniences in local Docker overrides. Those choices should stay clearly isolated from production-like expectations.

**Potential scope:**
- keep host-exposed database ports limited to local-only overrides
- periodically verify that dev-only settings are not copied into base or deployment-like artifacts
- document the intended security boundary between `compose.yml` and `compose.dev.yml`

## How to use this file

When a future hardening idea becomes concrete, add:

- the asset or boundary being protected
- the current limitation
- the proposed control
- the code/config area affected
- whether it is still a hardening item or has become an active defect
