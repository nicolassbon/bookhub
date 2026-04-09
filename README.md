# BookHub

BookHub is a social reading platform inspired by Goodreads and rebuilt as a microservices-based backend.

## Current scope

This repository is currently focused on the backend foundation for BookHub V1.

### Runtime applications

- `services/api-gateway`
- `services/identity-service`
- `services/catalog-service`
- `services/library-service`

### Supporting areas

- `docs/` — product, architecture, contracts, ADRs
- `infrastructure/` — Docker Compose and observability scaffolding
- `frontend/web-app/` — reserved for the Angular application

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

The repository already contains:

- V1 product and architecture documentation
- monorepo structure for the runtime services
- initial Spring Boot bootstrap for each runtime application
- first vertical slice of `identity-service` for user registration

## Key documentation

- `docs/prd-bookhub-v1.md`
- `docs/bounded-contexts-v1.md`
- `docs/service-contracts-v1.md`
- `docs/domain-model-v1.md`
- `docs/repository-structure-v1.md`
- `docs/adr/`

## Branching note

- `legacy/spring-mvc` remains the legacy reference branch
- microservices work is developed on `main`-based migration branches

## Notes

This project intentionally avoids a shared module in V1 to preserve service boundaries.
