# ADR-0002 — Start with four runtime applications: gateway plus three business services

- **Status**: Accepted
- **Date**: 2026-04-09

## Context

BookHub V1 must support:

- authentication and user identity
- book search and canonical metadata
- personal library management
- yearly goals
- reviews
- in-app notifications

At the same time, the system must remain realistic to deliver within the V1 scope and timeline.

## Decision

Start with these runtime applications:

- `api-gateway`
- `identity-service`
- `catalog-service`
- `library-service`

The business boundaries are:

- **Identity** → who the user is
- **Catalog** → what a book is
- **Library** → what the user does with a book

## Consequences

### Positive

- Strong enough to demonstrate real service boundaries.
- Small enough to remain buildable and explainable.
- Keeps reviews and notifications close to the reading lifecycle for V1.

### Negative

- `library-service` will be denser than the other two business services.
- Future extraction of reviews or notifications may be needed if those domains grow.

## Rejected alternatives

### Two-service split

Rejected because it would blur book metadata ownership and user-reading lifecycle responsibilities.

### Five-plus business services in V1

Rejected because the extra fragmentation would add complexity without enough product value for the first delivery.
