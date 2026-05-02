# catalog-service

Catalog and book discovery service for BookHub.

## Responsibilities

- public catalog search and book detail retrieval
- local PostgreSQL-backed catalog persistence
- external Open Library integration for bootstrap and lookups
- internal book lookup endpoint used by other services
- Flyway-managed schema evolution

## HTTP API

### Public endpoints

- `GET /api/v1/books`
- `GET /api/v1/books/{id}`

### Internal endpoint

- `GET /api/v1/internal/books/{bookId}`

## Configuration highlights

- Runs on port `8082` by default.
- Requires PostgreSQL connection settings via `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
- Open Library integration is configured under `catalog.providers.openlibrary.*`.
- Circuit-breaker behavior is configured under `catalog.providers.openlibrary.circuit-breaker.*`.

## Persistence

Flyway migrations live in `src/main/resources/db/migration/` and currently cover initial catalog schema, trigram indexes, and page-count support.

## Base package

`com.bookhub.catalog`
