# BookHub V1 — Repository Structure

## Purpose

This document defines the exact repository structure for BookHub V1. The goal is to keep the monorepo easy to navigate while preserving strong service boundaries.

## Monorepo strategy

BookHub V1 will use a **monorepo** with **independent Spring Boot services**.

This means:

- one repository for the whole platform
- separate applications for each service
- no shared runtime database
- no shared code module by default
- centralized docs and local infrastructure

## Top-level structure

```text
bookhub/
  .atl/
  docs/
  infrastructure/
    docker/
    observability/
  services/
    api-gateway/
    identity-service/
    catalog-service/
    library-service/
  frontend/
    web-app/
  .gitignore
  README.md
```

## Top-level folder responsibilities

### `.atl/`

Agent and orchestration artifacts.

Examples:

- `skill-registry.md`

### `docs/`

Project documentation in English.

Examples:

- PRD
- bounded contexts
- service contracts
- domain model
- ADRs
- repository structure

### `infrastructure/`

Local platform support only.

This folder is for:

- Docker Compose
- local observability stack
- infra configuration used to run the platform locally

This folder is **not** where business services live.

### `services/`

Runtime backend applications.

This folder contains one Spring Boot application per service.

### `frontend/`

Reserved for Angular later.

Frontend is intentionally separated from backend services and should not block backend delivery.

---

## Services included in V1

```text
services/
  api-gateway/
  identity-service/
  catalog-service/
  library-service/
```

## Why `api-gateway` belongs under `services/`

Because the gateway is a runtime application, not just deployment plumbing.

It is responsible for:

- routing
- auth propagation
- edge policies
- public API entrypoint behavior

So it should be treated as a first-class service.

---

## Exact service template

Each Spring Boot service should follow the same internal layout.

```text
services/identity-service/
  src/
    main/
      java/com/bookhub/identity/
        application/
        domain/
        infrastructure/
        web/
        config/
      resources/
        db/migration/
        application.yml
        application-local.yml
        application-test.yml
    test/
      java/com/bookhub/identity/
  Dockerfile
  pom.xml
  README.md
```

The same pattern applies to:

- `catalog-service`
- `library-service`
- `api-gateway` (with a lighter domain footprint)

---

## Package responsibilities inside each service

### `domain/`

Pure business concepts and rules.

Allowed contents:

- entities / aggregates
- value objects
- domain services
- domain exceptions
- repository ports if needed

Should avoid:

- Spring MVC annotations
- persistence annotations when they pollute the model too much
- transport logic

### `application/`

Use-case orchestration.

Allowed contents:

- application services
- command/query handlers
- transactional orchestration
- mapping between ports and use cases

Responsibilities:

- call domain logic
- coordinate repositories and external ports
- enforce use-case flow

### `infrastructure/`

Technical implementation details.

Allowed contents:

- JPA repositories
- persistence adapters
- HTTP clients
- email adapters
- external provider integrations
- security support classes that are infra-facing

### `web/`

HTTP API boundary.

Allowed contents:

- controllers
- request DTOs
- response DTOs
- API mappers
- exception-to-response translation support

Rules:

- controllers stay thin
- never expose entities directly
- validate input at the boundary

### `config/`

Spring configuration only.

Examples:

- security config
- OpenAPI config
- bean definitions
- CORS config
- cache config

---

## Recommended package examples by service

## `identity-service`

```text
com/bookhub/identity/
  application/
    auth/
    user/
  domain/
    auth/
    user/
  infrastructure/
    persistence/
    security/
    mail/
  web/
    auth/
    user/
  config/
```

## `catalog-service`

```text
com/bookhub/catalog/
  application/
    book/
    author/
    importing/
  domain/
    book/
    author/
    importing/
  infrastructure/
    persistence/
    providers/
  web/
    book/
    admin/
  config/
```

## `library-service`

```text
com/bookhub/library/
  application/
    library/
    review/
    goal/
    notification/
  domain/
    library/
    review/
    goal/
    notification/
  infrastructure/
    persistence/
    catalogclient/
  web/
    library/
    review/
    goal/
    notification/
    admin/
  config/
```

## `api-gateway`

```text
com/bookhub/gateway/
  config/
  infrastructure/
    routing/
    security/
  web/
```

The gateway should stay lightweight. Do not turn it into a business service.

---

## Resource structure

Each service should include:

```text
src/main/resources/
  application.yml
  application-local.yml
  application-test.yml
  db/migration/
```

### Rules

- Use Flyway for schema evolution.
- Keep secrets out of YAML files.
- Use environment variables for credentials and sensitive config.
- Keep profile-specific configuration explicit.

---

## Test structure

Tests should mirror production packages.

```text
src/test/java/com/bookhub/library/
  application/
  domain/
  infrastructure/
  web/
```

## Testing expectations per service

- `domain/` → unit tests
- `application/` → unit tests and focused orchestration tests
- `web/` → MockMvc/controller tests
- `infrastructure/` → integration tests where valuable
- repository/database integration → Testcontainers where correctness matters

---

## Infrastructure folder structure

```text
infrastructure/
  docker/
    compose.yml
    compose.observability.yml
  observability/
    prometheus/
      prometheus.yml
    grafana/
      provisioning/
      dashboards/
```

## Responsibility split

### `infrastructure/docker/`

Contains:

- the local platform compose file
- optional compose extensions
- local bootstrapping helpers if needed later

### `infrastructure/observability/`

Contains:

- Prometheus config
- Grafana provisioning
- dashboards

Avoid putting service-specific business configs here.

---

## Frontend structure for later

```text
frontend/
  web-app/
```

When Angular starts, it should become its own application with a modern feature-based structure.

But for V1 backend execution, `frontend/` is optional and non-blocking.

---

## Files each service should have from day one

Each service should include:

- `pom.xml`
- `Dockerfile`
- `README.md`
- `src/main/resources/application.yml`
- `src/main/resources/db/migration/`

Optional later:

- `openapi/`
- `scripts/`

---

## Naming conventions

## Repository-level

- folder names use kebab-case
- docs use kebab-case
- services end with `-service` except `api-gateway`

## Java packages

- base package format:
  - `com.bookhub.identity`
  - `com.bookhub.catalog`
  - `com.bookhub.library`
  - `com.bookhub.gateway`

## Database naming

Recommended local database names:

- `bookhub_identity`
- `bookhub_catalog`
- `bookhub_library`

---

## What we should NOT create in V1

### Do NOT create a shared code module yet

Avoid:

```text
shared/
common/
core-lib/
platform-common/
```

Why:

- it quickly becomes a coupling trap
- it hides weak service boundaries
- it creates fake reuse too early

If duplication appears, prefer intentional duplication first.

### Do NOT create extra services yet

Avoid creating these in V1:

- `review-service`
- `notification-service`
- `subscription-service`
- `recommendation-service`
- `social-service`

They may become valid later, but they are premature now.

### Do NOT create a separate infra repo

Local operability is part of the product delivery. Keep infra support close to the services for now.

---

## Final recommendation

BookHub V1 should use a monorepo with:

- centralized documentation
- independent Spring Boot services
- one gateway
- local-first infrastructure support
- no shared module by default

This gives the project the right balance of architectural clarity, delivery speed, and portfolio quality.
