# ADR-0003 — Use database-per-service from V1

- **Status**: Accepted
- **Date**: 2026-04-09

## Context

BookHub V1 aims to demonstrate actual service ownership, not just separate deployable jars. Shared persistence would weaken service boundaries and make business ownership unclear.

## Decision

Adopt **database-per-service** from the beginning.

For local development, this can be implemented with one PostgreSQL container using separate databases or schemas per service, but ownership remains service-specific.

Recommended names:

- `bookhub_identity`
- `bookhub_catalog`
- `bookhub_library`

## Consequences

### Positive

- Forces clear ownership and boundary discipline.
- Makes future extraction and scaling easier.
- Prevents direct table-level shortcuts across services.

### Negative

- More setup and migration management.
- Read-model duplication may be needed across services.
- Cross-service consistency must be handled through APIs, not SQL joins.

## Rejected alternatives

### Shared database with service-specific tables only

Rejected because it still invites direct access and weakens ownership boundaries.

### Shared schema with logical separation

Rejected because it is too easy to bypass service APIs once everything lives in the same persistence space.
