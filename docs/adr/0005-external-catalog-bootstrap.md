# ADR-0005 — Use an external catalog provider with local persistence

- **Status**: Accepted
- **Date**: 2026-04-09

## Context

BookHub needs searchable book data, but manually building a large catalog for V1 would slow delivery and add little architectural value.

At the same time, the system should not depend entirely on an external provider at read time for all user-facing domain operations.

## Decision

Use an **external book provider** to bootstrap search and metadata discovery, and **persist locally** the books that users actually interact with.

This means:

- external provider for discovery/bootstrap
- local canonical persistence for active books
- normalization/import logic owned by `catalog-service`

## Consequences

### Positive

- Faster time to a realistic product experience.
- Avoids manually populating a catalog.
- Keeps control over relevant catalog data once users start using it.

### Negative

- Requires import/normalization logic.
- External provider inconsistencies must be handled explicitly.

## Rejected alternatives

### Fully manual catalog from day one

Rejected because it slows down delivery without enough product value.

### Fully remote catalog without local persistence

Rejected because it would make domain behavior overly dependent on external provider availability and data shape.
