# ADR-0006 — Use TDD and strong verification as delivery rules

- **Status**: Accepted
- **Date**: 2026-04-09

## Context

BookHub V1 is intended to be both a real product foundation and a portfolio project that demonstrates backend engineering maturity.

The project-level skills in `.agents/skills` strongly emphasize:

- TDD for Spring Boot changes
- strong verification before PR/release
- secure-by-design implementation

## Decision

Adopt the following delivery rules for V1:

- implement features with TDD as the default approach
- maintain strong unit coverage on core business rules
- add integration tests for critical flows
- use MockMvc and Testcontainers where appropriate
- verify major changes through a repeatable build/test/security/diff-review loop

## Consequences

### Positive

- Higher confidence in business rules and service contracts.
- Better portfolio quality and easier reasoning about regressions.
- Security and correctness become part of delivery, not post-processing.

### Negative

- Slower short-term feature throughput.
- Requires discipline and stable test design from the start.

## Rejected alternatives

### Test later after implementation

Rejected because it would undermine the quality target of the project and increase the cost of architectural changes.

### Rely mostly on manual Postman testing

Rejected because manual testing alone is not enough for a system that aims to demonstrate serious backend engineering practices.
