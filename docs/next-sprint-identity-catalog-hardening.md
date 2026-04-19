# Next Sprint Identity & Catalog Hardening Notes

## Identity: RS256 Hard Cut Rollout Notes

- `identity-service` now issues and validates **RS256-only** access tokens.
- Legacy `HS256` access tokens are intentionally rejected after rollout.
- Operational implication: treat deployment as a **hard-cut token invalidation window** for previously issued access tokens.
- Required runtime configuration keys:
  - `JWT_ISSUER`
  - `JWT_AUDIENCE`
  - `JWT_RSA_PRIVATE_KEY` (PKCS#8)
  - `JWT_RSA_PUBLIC_KEY` (X.509)
- Startup fails fast when RSA key material is missing, malformed, mismatched, or below 2048 bits.

## Catalog: Circuit Breaker + Degraded Contract Notes

- OpenLibrary calls are protected by a named circuit breaker (`catalog.providers.openlibrary.circuit-breaker.*`).
- During sustained downstream failures:
  - search requests degrade to local-only results,
  - uncached detail requests return **HTTP 503** with degraded payload.
- Degraded detail response contract:
  - `code = OPENLIBRARY_UNAVAILABLE`
  - `degraded = true`
  - `retryAfterSeconds`
  - `Retry-After` response header (seconds)

## Initial Breaker Tuning Guidance

Start conservative and tune from production metrics:

- `sliding-window-size`: 10
- `minimum-number-of-calls`: 5
- `failure-rate-threshold`: 50
- `wait-duration-open-state-ms`: 30000
- `permitted-number-of-calls-in-half-open-state`: 2

Tune based on:

- fallback counter growth (`catalog.provider.openlibrary.search.fallbacks`),
- detail degraded-response rate,
- breaker oscillation frequency between open/half-open/closed states.
