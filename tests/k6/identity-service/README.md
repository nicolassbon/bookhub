# Identity Service k6 (Phase 2)

This folder contains the **Phase 2 mixed API testing setup** for BookHub `identity-service`, focused on **rate-limiting** and refresh-token replay behavior split into **semantic correctness** and **pressure/limiter interaction**.

## File Layout

```text
tests/k6/identity-service/
├── README.md
├── lib/
│   ├── auth-api.js
│   └── config.js
└── scenarios/
    ├── login-rate-limit.js
    ├── refresh-rate-limit.js
    ├── refresh-replay-semantics.js
    └── refresh-replay-pressure.js
```

Runner:

- `scripts/run-identity-k6-phase2.sh`

Recommended suite usage:

- `rate-limit-suite` — stable validation for auth throttling behavior
- `replay-suite` — focused replay validation, ideally with bootstrap credentials for an existing local user
- `all` — convenience mode; useful, but still more sensitive to hot limiter windows and scenario ordering

## Prerequisites

1. `identity-service` is running (default: `http://localhost:8081`).
2. `k6` is installed and available on `PATH`.
3. Local auth dependencies are configured so login/register/refresh endpoints work.

Optional but recommended for local HTTP runs:

- `REFRESH_TOKEN_COOKIE_SECURE=false` in local service runtime.

> The scripts set the refresh cookie manually in request headers, but keeping local cookie security aligned with HTTP testing avoids confusion during troubleshooting.

## Scenarios

### 1) Login rate limiting

- Script: `tests/k6/identity-service/scenarios/login-rate-limit.js`
- Intent: verify `/api/v1/auth/login` transitions from `401 INVALID_CREDENTIALS` to `429 RATE_LIMIT_EXCEEDED` under sustained invalid-login pressure.

Default behavior:

- Constant arrival rate: `20 req/s` for `30s`.
- Uses invalid credentials from env (`LOGIN_INVALID_EMAIL`, `LOGIN_INVALID_PASSWORD`) or defaults.
- Pass/fail thresholds include:
  - minimum ratio of `429` responses,
  - zero unexpected statuses,
  - valid error code for 429 responses.

### 2) Refresh rate limiting

- Script: `tests/k6/identity-service/scenarios/refresh-rate-limit.js`
- Intent: verify `/api/v1/auth/refresh` is rate-limited under repeated invalid-token requests.

Default behavior:

- Constant arrival rate: `20 req/s` for `30s`.
- Uses a configurable UUID-shaped but invalid token (`REFRESH_INVALID_TOKEN`).
- Expects only:
  - `401 INVALID_REFRESH_TOKEN`, or
  - `429 RATE_LIMIT_EXCEEDED`.

### 3) Refresh replay semantics (deterministic correctness)

- Script: `tests/k6/identity-service/scenarios/refresh-replay-semantics.js`
- Intent: validate **single-use refresh token semantics** with conservative concurrency and strict expectations.

Default behavior:

1. `setup()` bootstraps credentials in this order:
   - if `REFRESH_REPLAY_BOOTSTRAP_EMAIL` + `REFRESH_REPLAY_BOOTSTRAP_PASSWORD` are set, it logs in directly and reuses that account;
   - otherwise, it falls back to register a random user and then log in.
2. `N` VUs (`REFRESH_REPLAY_SEMANTICS_CONCURRENCY`, default `2`) each perform one simultaneous refresh attempt using the **same** token.
3. Thresholds require:
   - exactly one successful refresh (`200`),
   - at least one rejected replay attempt with `401 INVALID_REFRESH_TOKEN`,
   - no rate-limit domination (`429` threshold is `rate==0`).

> If this scenario fails because of `429`, treat that as an environment/limiter signal: semantics are not observable under the current limiter pressure and the run must not be interpreted as a semantic pass.

### 4) Refresh replay pressure (interaction with limiter)

- Script: `tests/k6/identity-service/scenarios/refresh-replay-pressure.js`
- Intent: observe replay outcomes under higher concurrency where limiter interaction may dominate.

Default behavior:

1. `setup()` bootstraps credentials in this order:
   - if `REFRESH_REPLAY_BOOTSTRAP_EMAIL` + `REFRESH_REPLAY_BOOTSTRAP_PASSWORD` are set, it logs in directly and reuses that account;
   - otherwise, it falls back to register a random user and then log in.
2. `N` VUs (`REFRESH_REPLAY_PRESSURE_CONCURRENCY`, default `12`) each perform one refresh attempt concurrently.
3. Thresholds are intentionally non-semantic:
   - ensure expected statuses only (`200`, `401`, `429`),
   - ensure at least two attempts,
   - ensure a meaningful rejected ratio,
   - allow `429` to dominate.

This scenario is **not** proof of deterministic replay semantics. It is a pressure/interaction signal.

## How to Run

From repo root.

### Run all Phase 2 scenarios

```bash
./scripts/run-identity-k6-phase2.sh all
```

`all` is a convenience command. For the most reliable local verification, prefer the dedicated suites below.

### Run the recommended suites

```bash
./scripts/run-identity-k6-phase2.sh rate-limit-suite
```

```bash
REFRESH_REPLAY_BOOTSTRAP_EMAIL=test-user@example.com \
REFRESH_REPLAY_BOOTSTRAP_PASSWORD='BookHub!2345' \
./scripts/run-identity-k6-phase2.sh replay-suite
```

### Run one scenario

```bash
./scripts/run-identity-k6-phase2.sh login-rate-limit
./scripts/run-identity-k6-phase2.sh refresh-rate-limit
./scripts/run-identity-k6-phase2.sh refresh-replay-semantics
./scripts/run-identity-k6-phase2.sh refresh-replay-pressure
```

### Pass extra k6 args

```bash
./scripts/run-identity-k6-phase2.sh login-rate-limit --summary-trend-stats="avg,p(95),p(99),max"
```

### Override environment variables

Examples:

```bash
BASE_URL=http://localhost:8081 \
LOGIN_RATE_PER_SECOND=25 \
LOGIN_DURATION=45s \
./scripts/run-identity-k6-phase2.sh login-rate-limit
```

```bash
BASE_URL=http://localhost:8081 \
REFRESH_REPLAY_SEMANTICS_CONCURRENCY=2 \
REFRESH_REPLAY_BOOTSTRAP_EMAIL=test-user@example.com \
REFRESH_REPLAY_BOOTSTRAP_PASSWORD='BookHub!2345' \
./scripts/run-identity-k6-phase2.sh refresh-replay-semantics
```

```bash
BASE_URL=http://localhost:8081 \
REFRESH_REPLAY_PRESSURE_CONCURRENCY=16 \
REFRESH_REPLAY_BOOTSTRAP_EMAIL=test-user@example.com \
REFRESH_REPLAY_BOOTSTRAP_PASSWORD='BookHub!2345' \
./scripts/run-identity-k6-phase2.sh refresh-replay-pressure
```

## Environment Variables

### Shared

- `BASE_URL` (default: `http://localhost:8081`)
- `HTTP_TIMEOUT` (default: `10s`)
- `REFRESH_COOKIE_NAME` (default: `refresh_token`)

### Login rate-limit scenario

- `LOGIN_RATE_PER_SECOND` (default: `20`)
- `LOGIN_DURATION` (default: `30s`)
- `LOGIN_PRE_ALLOCATED_VUS` (default: `8`)
- `LOGIN_MAX_VUS` (default: `24`)
- `LOGIN_INVALID_EMAIL` (default: `rate-limit-login@example.test`)
- `LOGIN_INVALID_PASSWORD` (default: `invalid-password-1`)
- `LOGIN_MIN_RATE_LIMITED_RATIO` (default: `0.5`)
- `LOGIN_MIN_CHECK_RATE` (default: `0.99`)

### Refresh rate-limit scenario

- `REFRESH_RATE_PER_SECOND` (default: `20`)
- `REFRESH_DURATION` (default: `30s`)
- `REFRESH_PRE_ALLOCATED_VUS` (default: `8`)
- `REFRESH_MAX_VUS` (default: `24`)
- `REFRESH_INVALID_TOKEN` (default: `11111111-1111-4111-8111-111111111111`)
- `REFRESH_MIN_RATE_LIMITED_RATIO` (default: `0.5`)
- `REFRESH_MIN_CHECK_RATE` (default: `0.99`)

### Refresh replay semantics scenario

- `REFRESH_REPLAY_BOOTSTRAP_EMAIL` (optional, shared with replay pressure)
- `REFRESH_REPLAY_BOOTSTRAP_PASSWORD` (optional, shared with replay pressure)
  - If one is set, both must be set.
  - When both are set, replay `setup()` skips `/register` and logs in directly.
- `REFRESH_REPLAY_SEMANTICS_CONCURRENCY` (default: `2`)
- `REFRESH_REPLAY_SEMANTICS_MAX_DURATION` (default: `30s`)
- `REFRESH_REPLAY_SEMANTICS_USER_PREFIX` (default: `k6-refresh-replay-semantics`)
- `REFRESH_REPLAY_SEMANTICS_MIN_CHECK_RATE` (default: `0.99`)
- `REFRESH_REPLAY_SEMANTICS_MIN_INVALID_TOKEN_REJECTED_RATE` (default: `0.49`)

### Refresh replay pressure scenario

- `REFRESH_REPLAY_BOOTSTRAP_EMAIL` (optional, shared with replay semantics)
- `REFRESH_REPLAY_BOOTSTRAP_PASSWORD` (optional, shared with replay semantics)
  - If one is set, both must be set.
  - When both are set, replay `setup()` skips `/register` and logs in directly.
- `REFRESH_REPLAY_PRESSURE_CONCURRENCY` (default: `12`)
- `REFRESH_REPLAY_PRESSURE_MAX_DURATION` (default: `30s`)
- `REFRESH_REPLAY_PRESSURE_USER_PREFIX` (default: `k6-refresh-replay-pressure`)
- `REFRESH_REPLAY_PRESSURE_MIN_CHECK_RATE` (default: `0.99`)
- `REFRESH_REPLAY_PRESSURE_MIN_REJECTED_RATE` (default: `0.5`)

## Result Interpretation

Likely success indicators:

- Login and refresh rate-limit scenarios show a meaningful proportion of `429` with `RATE_LIMIT_EXCEEDED`.
- Replay semantics scenario shows **exactly one** refresh success, at least one `401 INVALID_REFRESH_TOKEN`, and no `429`.
- Replay pressure scenario remains within expected status codes while producing mostly rejected attempts (`401` and/or `429`).
- No unexpected statuses and check thresholds pass.

Likely failure indicators:

- No or very few `429` responses under configured pressure.
- Replay semantics scenario yields more than one successful refresh, no replay rejection, or any meaningful `429` rate (semantic signal compromised by limiter interaction).
- Replay pressure scenario shows unexpected statuses or more than one success.
- Significant unexpected statuses (`5xx`, malformed responses, missing expected error codes).

## Assumptions and Notes

- These scripts target **auth limiter and replay behavior**, not full-system load benchmarking.
- Replay semantics is intentionally conservative so replay correctness is observable without rate-limit domination.
- Replay pressure is intentionally higher-concurrency and can be limiter-dominant by design.
- The most reliable local workflow is:
  1. `./scripts/run-identity-k6-phase2.sh rate-limit-suite`
  2. `REFRESH_REPLAY_BOOTSTRAP_EMAIL=... REFRESH_REPLAY_BOOTSTRAP_PASSWORD=... ./scripts/run-identity-k6-phase2.sh replay-suite`
- `all` remains useful, but it can still become flaky when earlier scenarios leave limiter windows hot for later replay checks.
- Legacy runner alias `refresh-replay` maps to `refresh-replay-semantics` for backward compatibility.
