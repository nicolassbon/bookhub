# Local Docker setup (identity + catalog)

This setup runs the following services for local development:

- `identity-service` + `identity-db`
- `catalog-service` + `catalog-db`

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
docker compose -f compose.yml -f compose.dev.yml logs -f identity-service catalog-service
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

- Identity API: `http://localhost:8081`
- Catalog API: `http://localhost:8082`
- Identity DB: `localhost:5432`
- Catalog DB: `localhost:5433`

## Notes

- Containers use Docker DNS hostnames (`identity-db`, `catalog-db`) for internal DB connectivity.
- Docker images are built from source inside Docker (no prebuilt local jars required).
- Host-port publishing is intentionally isolated in `compose.dev.yml` to keep `compose.yml` reusable across local scenarios.
