# Identity Service k6 (Pressure-Only Scope)

This folder contains the **non-functional k6 suite** for BookHub `identity-service`.

Functional correctness now lives in Java tests (`@WebMvcTest` and `PostgreSqlIntegrationTest`).
k6 is intentionally limited to pressure/load observation.

## File Layout

```text
tests/k6/identity-service/
├── README.md
├── lib/
│   ├── auth-api.js
│   └── config.js
└── scenarios/
    └── refresh-replay-pressure.js
```

Runner:

- `scripts/run-identity-k6-phase2.sh`

## Scenario

### Refresh replay pressure

- Script: `tests/k6/identity-service/scenarios/refresh-replay-pressure.js`
- Intent: apply concurrent refresh pressure and observe throughput/rejection behavior under contention.
- Scope: **non-functional only**. The script does not assert business correctness semantics.

## Prerequisites

1. The local Docker stack is running and gateway entrypoint is available at `http://localhost:8080`.
2. `k6` is installed and available on `PATH`.

## How to Run

From repo root:

```bash
./scripts/run-identity-k6-phase2.sh all
```

```bash
./scripts/run-identity-k6-phase2.sh refresh-replay-pressure
```

## Environment Variables

### Shared

- `BASE_URL` (default: `http://localhost:8080`)
- `HTTP_TIMEOUT` (default: `10s`)
- `REFRESH_COOKIE_NAME` (default: `refresh_token`)

### Refresh replay pressure

- `REFRESH_REPLAY_BOOTSTRAP_EMAIL` (optional)
- `REFRESH_REPLAY_BOOTSTRAP_PASSWORD` (optional)
- `REFRESH_REPLAY_PRESSURE_CONCURRENCY` (default: `12`)
- `REFRESH_REPLAY_PRESSURE_MAX_DURATION` (default: `30s`)
- `REFRESH_REPLAY_PRESSURE_USER_PREFIX` (default: `k6-refresh-replay-pressure`)
- `REFRESH_REPLAY_PRESSURE_MIN_CHECK_RATE` (default: `0.99`)
- `REFRESH_REPLAY_PRESSURE_MIN_REJECTED_RATE` (default: `0.5`)

## Result Interpretation

- Expected statuses are limited to `200`, `401`, `429`.
- Rejection ratio should remain meaningful under configured concurrency.
- Treat this suite as pressure telemetry, not functional acceptance.
