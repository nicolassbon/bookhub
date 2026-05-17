# identity-service

Identity and authentication service for BookHub.

## Responsibilities

- user registration and login
- JWT access-token issuance using RS256
- refresh-token rotation and logout
- forgot-password and reset-password flows
- service token issuance for machine-to-machine authentication
- authenticated identity endpoint (`GET /api/v1/users/me`)
- Flyway-managed PostgreSQL persistence
- Shared Redis-backed abuse throttling for auth and service-token endpoints (multi-instance consistent)
- admin-only user management surfaces (role-gated)

## HTTP API

### Authentication

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `POST /api/v1/auth/forgot-password`
- `POST /api/v1/auth/reset-password`
- `POST /api/v1/auth/service-token` — issues a service JWT for machine-to-machine access (HTTP Basic auth)

### User

- `GET /api/v1/users/me`

### Admin (requires ROLE_ADMIN)

- `GET /api/v1/admin/users` — list all users (paged)
- `PATCH /api/v1/admin/users/{userId}/role` — change user role

## Configuration highlights

- Runs on port `8081` by default.
- Requires PostgreSQL connection settings via `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
- Requires JWT settings via `JWT_ISSUER`, `JWT_AUDIENCE`, `JWT_RSA_PRIVATE_KEY`, and `JWT_RSA_PUBLIC_KEY`.
- Requires service credentials via `SERVICE_CLIENT_ID` and `SERVICE_CLIENT_SECRET` for service-token issuance.
- Requires password-reset hashing secret via `PASSWORD_RESET_HASH_SECRET`.
- Uses refresh-token hashing and configurable cookie settings under `auth.refresh-token.*`.

## Persistence

Flyway migrations live in `src/main/resources/db/migration/` and currently cover users, refresh tokens, password-reset tokens, refresh-token hashing, and password-reset token constraints.

## Base package

`com.bookhub.identity`
