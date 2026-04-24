# Identity Service Postman/Newman (Phase 1)

This folder contains the **Phase 1 mixed API testing setup** for BookHub identity auth flows routed through the local `api-gateway`.

## Files

- `tests/postman/identity-service/bookhub-identity-auth-phase1.postman_collection.json`
  - Postman collection with auth flow requests.
- `tests/postman/identity-service/bookhub-identity-local.postman_environment.json`
  - Local environment template with placeholders and reusable variables.
- `scripts/run-identity-postman-phase1.sh`
  - Repo-local Newman runner.

## Covered Flows

The collection includes:

1. `register`
2. `login`
3. `refresh`
4. `logout`
5. `forgot-password`
6. `reset-password`

The **default Newman runner** executes only the automated subset:

1. `register`
2. `login`
3. `get current user (me)`
4. `refresh`
5. `logout`
6. `forgot-password`

`reset-password` remains in the collection as a **manual-assisted** request because it requires an out-of-band reset token.

## Prerequisites

1. The local Docker stack is running and the public gateway entrypoint is available at `http://localhost:8080`.
2. `newman` is installed for CLI runs:
   - `npm install -g newman`
3. For HTTP local runs, ensure refresh cookie can be sent over HTTP:
   - set `REFRESH_TOKEN_COOKIE_SECURE=false` in your local runtime environment for `identity-service`.
   - If you keep secure cookies enabled, run over HTTPS or use `useManualRefreshCookie=true` with a manually copied token.

## How to Run Locally

From repo root:

```bash
./scripts/run-identity-postman-phase1.sh
```

This runs the automated subset only and skips `reset-password`.

With a custom environment file:

```bash
./scripts/run-identity-postman-phase1.sh tests/postman/identity-service/bookhub-identity-local.postman_environment.json
```

Pass extra Newman args after the env file (example: verbose output):

```bash
./scripts/run-identity-postman-phase1.sh tests/postman/identity-service/bookhub-identity-local.postman_environment.json --verbose
```

## Practical Notes

- `register` auto-generates email/username values if they are empty.
- Requests must target the gateway base URL (`http://localhost:8080`), not the internal service port.
- Set `autoGenerateIdentityData=false` to keep manually provided `testEmail` / `testUsername`.
- `login` stores:
  - `accessToken`
  - `refreshTokenValue` (parsed from `Set-Cookie` when available)
- `refresh` and `logout` can use either:
  - cookie jar behavior (default), or
  - manual `Cookie` header fallback (`useManualRefreshCookie=true` + `refreshTokenValue`).

## Manual Step (Reset Password)

`reset-password` requires `resetToken` from an out-of-band channel (mail inbox, MailHog, logs, or test mail sink).

Before running that request manually in Postman, set:

- `resetToken` in the Postman environment.

If `resetToken` is empty, the request will fail fast in pre-request checks.

## What Is Not Covered in Phase 1

- Contract/schema validation beyond core status/body assertions.
- CI pipeline integration for Newman reports.
- Automated extraction of password reset tokens from mail providers.
