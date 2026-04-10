# ADR-0007 — Use a root Maven parent POM for platform alignment

- **Status**: Accepted
- **Date**: 2026-04-09

## Context

BookHub uses a monorepo with multiple independent Spring Boot applications. The service POM files were repeating the same platform configuration:

- Spring Boot parent version
- Java version
- annotation processor setup
- shared build tooling versions

That duplication increases maintenance cost and makes coordinated upgrades harder.

At the same time, the project rule remains clear: services must stay independent and the root must not become a shared runtime module.

## Decision

Use the repository root `pom.xml` as a **parent and aggregator POM**.

The root POM will:

- inherit from `spring-boot-starter-parent`
- aggregate all runtime modules
- centralize platform and build versions
- centralize annotation processor configuration where useful
- declare **no direct runtime dependencies**

Each service POM keeps its own service-specific dependencies.

## Consequences

### Positive

- Spring Boot and Java upgrades happen in one place.
- Annotation processor setup is consistent across modules.
- Reduces duplicated boilerplate in child POMs.
- Preserves service independence because runtime dependencies still live in each child module.

### Negative

- Platform upgrades are coordinated across the monorepo.
- A mistaken root configuration can affect all services at once.

## Rejected alternatives

### No root parent POM

Rejected because it would keep repeating platform configuration in every service and create unnecessary drift risk.

### Root POM with shared runtime dependencies

Rejected because it would blur service boundaries and violate the project rule against shared runtime modules by default.
