# Architecture Decision Records

This directory contains the core Architecture Decision Records (ADRs) for BookHub V1.

## ADR Index

- [ADR-0001 — Use a monorepo with independent services](./0001-monorepo-independent-services.md)
- [ADR-0002 — Start with four runtime applications: gateway plus three business services](./0002-gateway-plus-three-services.md)
- [ADR-0003 — Use database-per-service from V1](./0003-database-per-service.md)
- [ADR-0004 — Use synchronous HTTP for service-to-service communication in V1](./0004-synchronous-http-communication.md)
- [ADR-0005 — Use an external catalog provider with local persistence](./0005-external-catalog-bootstrap.md)
- [ADR-0006 — Use TDD and strong verification as delivery rules](./0006-tdd-and-verification.md)

## ADR Status Values

- **Accepted** — approved and active
- **Superseded** — replaced by a newer ADR
- **Proposed** — not finalized yet

## Notes

These ADRs are intentionally lightweight and focused on the decisions that shape BookHub V1 implementation. If one of these decisions changes later, a new ADR should supersede the old one instead of silently editing history.
