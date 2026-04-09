# ADR-0004 — Use synchronous HTTP for service-to-service communication in V1

- **Status**: Accepted
- **Date**: 2026-04-09

## Context

The system needs service-to-service communication for identity and catalog validation, but the V1 scope and timeline do not justify introducing a message broker and event-driven delivery from day one.

The user also expressed doubts about async messaging being worth the complexity in V1.

## Decision

Use **synchronous HTTP** as the default communication style between services in V1.

Typical examples:

- `library-service` validates `bookId` with `catalog-service`
- security and gateway layers propagate authenticated user identity from `identity-service`

## Consequences

### Positive

- Lower operational complexity.
- Faster delivery for the first working version.
- Easier debugging and clearer request flow.

### Negative

- Tighter runtime coupling than an event-driven model.
- Some future scalability and resilience patterns are postponed.

## Rejected alternatives

### Event-driven architecture from V1

Rejected because it would add broker setup, delivery semantics, failure handling, and coordination complexity too early.

### Shared in-memory or direct code integration

Rejected because services must interact through explicit APIs, not hidden runtime shortcuts.
