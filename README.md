# BookHub

BookHub is a social reading platform inspired by Goodreads and implemented as a microservices-first backend.

## Current scope

This repository contains the backend foundation for BookHub V1, including identity, catalog, library, gateway, local infrastructure, and source-of-truth product and architecture documents.

### Runtime applications

- `services/api-gateway` — single public entrypoint and cross-origin policy.
- `services/identity-service` — authentication, identity, password recovery, and token lifecycle.
- `services/catalog-service` — book discovery and internal catalog lookups.
- `services/library-service` — user library flows, reading state, and progress tracking.

### Supporting areas

- `docs/` — product, architecture, contracts, ADRs, and operational tracking docs.
- `infrastructure/` — Docker Compose setup and observability scaffolding.
- `frontend/web-app/` — reserved for the future web application.
- `tests/k6/` — non-functional performance testing.

## Architecture direction

BookHub V1 follows these principles:

- microservices-first backend
- bounded contexts with clear ownership
- database per service
- synchronous HTTP communication in V1
- Docker Compose for local development
- TDD and strong verification loops
- English-only technical documentation

## Current implementation status

The repository currently includes:

- V1 product and architecture documentation in `docs/`
- a Maven multi-module monorepo for the runtime services
- Flyway-managed PostgreSQL schemas per backend service
- JWT-based identity flows with RS256 signing and refresh-token rotation
- catalog search and detail endpoints backed by local persistence plus Open Library bootstrap/integration
- library endpoints for add/list/get/update-state/update-progress workflows
- local Docker Compose topology for gateway + identity + catalog + library

## Key documentation

### Normative / source of truth

- `docs/prd-bookhub-v1.md`
- `docs/prd-library-service-foundation.md`
- `docs/bounded-contexts-v1.md`
- `docs/service-contracts-v1.md`
- `docs/domain-model-v1.md`
- `docs/repository-structure-v1.md`
- `docs/adr/`

### Operational / status docs

- `docs/security-issues-backlog.md`
- `infrastructure/docker/README.md`
- `services/*/README.md`

## Local development

Use the Docker Compose files under `infrastructure/docker/` to run the current local stack. See `infrastructure/docker/README.md` for prerequisites, environment variables, and host endpoints.

## Branching note

- `legacy/spring-mvc` remains the legacy reference branch.
- microservices work is developed on `main`-based migration branches.

## Notes

- This project intentionally avoids a shared module in V1 to preserve service boundaries.
- Product and architecture documents guide implementation. Operational READMEs must stay aligned with the current executable state.
