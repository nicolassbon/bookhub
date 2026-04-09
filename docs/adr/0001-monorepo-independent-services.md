# ADR-0001 — Use a monorepo with independent services

- **Status**: Accepted
- **Date**: 2026-04-09

## Context

BookHub V1 will be developed by a single developer within a limited timeline. The system needs to demonstrate real microservice boundaries, but also remain practical to build, run, document, and evolve.

The project also needs:

- centralized documentation
- local operability with Docker Compose
- coherent versioning across gateway and services
- a backend-first workflow

## Decision

Use a **monorepo** for BookHub V1, with **independent applications** under `services/`.

This means:

- one Git repository for the platform
- one runtime application per service
- no shared runtime database
- no shared code module by default
- centralized docs and local infrastructure support

## Consequences

### Positive

- Easier coordination for a solo developer.
- Simpler local setup and documentation.
- Easier cross-service refactors during the early phase.
- Better fit for a platform that must be explained as one portfolio system.

### Negative

- Risk of accidental service coupling through convenience changes.
- Requires discipline to avoid building a distributed monolith.

## Rejected alternatives

### Multi-repo from the start

Rejected because it adds operational and coordination overhead too early for a V1 built by one developer.

### Monorepo with shared common module from day one

Rejected because it encourages premature coupling and hides weak service boundaries.
