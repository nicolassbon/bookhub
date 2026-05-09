# Local Docker setup (gateway + identity + catalog + library)

This setup runs the following services for local development:

- `api-gateway`
- `identity-service` + `identity-db` + `identity-redis`
- `catalog-service` + `catalog-db`
- `library-service` + `library-db`

This setup follows an **Option B** split:

- `compose.yml` (base): internal Docker network topology (service-to-service and service-to-db)
- `compose.dev.yml` (dev override): host-exposed ports for Postman and local DB tools

## Prerequisites

- Docker + Docker Compose plugin
- A local `.env` file with identity secrets (do not commit it)

## 1) Load required identity environment variables

From repository root:

```bash
cp .env.example .env
#edit .env and replace placeholders
```

At minimum, ensure these are set:

- `JWT_RSA_PRIVATE_KEY`
- `JWT_RSA_PUBLIC_KEY`
- `PASSWORD_RESET_HASH_SECRET`

`docker compose` reads the repository-root `.env` through the service-level `env_file` entries, so you do not need to export these variables manually before running the stack.

## 2) Build and run with Docker Compose

From `infrastructure/docker`:

```bash
docker compose -f compose.yml -f compose.dev.yml up --build -d
```

## 3) Useful commands

Check status:

```bash
docker compose -f compose.yml -f compose.dev.yml ps
```

Follow logs:

```bash
docker compose -f compose.yml -f compose.dev.yml logs -f api-gateway identity-service catalog-service library-service
```

Stop and remove containers:

```bash
docker compose -f compose.yml -f compose.dev.yml down
```

Stop and remove containers + DB volumes:

```bash
docker compose -f compose.yml -f compose.dev.yml down -v
```

## Optional: run internal-only (no host ports)

If you want to run the stack only for container-to-container communication (no host access), use only the base file:

```bash
docker compose -f compose.yml up --build -d
```

## Host endpoints

- Gateway API (public entrypoint): `http://localhost:8080`
- Identity DB: `localhost:5432`
- Identity Redis: `localhost:6379`
- Catalog DB: `localhost:5433`

Library DB is **not** exposed on the host in `compose.dev.yml`.
Use Docker-network access (`library-db:5432`) from containers, or add an explicit host port mapping in `compose.dev.yml` if local DB tooling access is needed.

## Gateway route surface and downstream access behavior

- `/api/v1/auth/**` -> `identity-service` (**routed by gateway; identity-service currently permits only `/api/v1/auth/register`, `/login`, `/refresh`, `/logout`, `/forgot-password`, and `/reset-password` without authentication**)
- `GET /api/v1/books/**` -> `catalog-service` (**public in catalog-service security config**)
- `/api/v1/users/**` -> `identity-service` (**routed by gateway and requires authentication in identity-service security config**)
- `/api/v1/library/**` -> `library-service` (**routed by gateway and requires authentication in library-service security config**)

## Notes on route alignment

- The base gateway configuration (`services/api-gateway/src/main/resources/application.yml`) also reserves `/api/v1/goals/**`, `/api/v1/reviews/**`, and `/api/v1/notifications/**` for `library-service` as part of the V1 contract surface.
- Keep the local gateway profile aligned with the intended route map whenever routes are added or removed.

Downstream service ports are intentionally not published in `compose.dev.yml` so all API traffic enters through `api-gateway`.

## Notes

- `identity-service` uses `identity-redis:6379` for shared auth rate-limit counters in the local stack.
- Redis stores ephemeral TTL-backed counters under `bookhub:identity:auth-rate-limit:v1` by default.
- Containers use Docker DNS hostnames (`identity-db`, `identity-redis`, `catalog-db`, `library-db`) for internal connectivity.
- Docker images are built from source inside Docker (no prebuilt local jars required).
- Host-port publishing is intentionally isolated in `compose.dev.yml` to keep `compose.yml` reusable across local scenarios.
