# ADR-0008 — Use MapStruct for DTO and web mapping

- **Status**: Accepted
- **Date**: 2026-04-09

## Context

The current identity-service slices already require repeated mappings such as:

- request DTO → application command
- application result → response DTO
- nested result model → nested response model

Manual mapping inside controllers is simple at first, but it grows noisy and distracts controllers from HTTP concerns. The project also wants explicit, readable mapping without overengineering.

## Decision

Use **MapStruct** for DTO and web mapping where manual mapping is already present and meaningful.

Guidelines:

- use `@Mapper(componentModel = "spring")`
- keep mapper interfaces small and local to the feature/module
- apply it where it replaces real repetitive mapping
- do not introduce mapper layers where there is nothing to map yet

MapStruct is integrated with Lombok through `lombok-mapstruct-binding`.

## Consequences

### Positive

- Controllers stay thinner and more focused on HTTP behavior.
- Mapping logic becomes explicit, testable, and generated instead of handwritten boilerplate.
- Works well with records and builder-based models.

### Negative

- Adds annotation processing complexity to the build.
- Requires compile-time discipline when DTOs or result models change.

## Rejected alternatives

### Manual mapping everywhere

Rejected because the project already started repeating mapping code in controllers and response construction.

### Generic reflection-based mappers

Rejected because MapStruct is compile-time, explicit, fast, and safer for a Java/Spring backend.
