# catalog-service

Catalog and book discovery service for BookHub.

## Responsibilities

- public catalog search and book detail retrieval
- local PostgreSQL-backed catalog persistence
- external Open Library integration for bootstrap and lookups
- internal book lookup endpoint used by other services (requires authenticated service-to-service access with `ROLE_SERVICE`)
- admin catalog management and book import surfaces (requires `ROLE_ADMIN`)
- Flyway-managed schema evolution

## HTTP API

### Public endpoints

- `GET /api/v1/books` — list books
- `GET /api/v1/books/{id}` — book details

### Admin endpoints (requires ROLE_ADMIN)

- `GET /api/v1/admin/books` — list all books with optional source filter
- `PATCH /api/v1/admin/books/{bookId}` — update book details
- `POST /api/v1/admin/books/import` — import book from provider
- `POST/PUT/DELETE/PATCH /api/v1/books/**` — book mutations are restricted to admins

### Internal endpoint (authenticated service-to-service)

- `GET /api/v1/internal/books/{bookId}` — requires a service JWT with `role=SERVICE` in the `Authorization: Bearer` header

## Configuration highlights

- Runs on port `8082` by default.
- Requires PostgreSQL connection settings via `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
- Open Library integration is configured under `catalog.providers.openlibrary.*`.
- Circuit-breaker behavior is configured under `catalog.providers.openlibrary.circuit-breaker.*`.

## Persistence

Flyway migrations live in `src/main/resources/db/migration/` and currently cover initial catalog schema, trigram indexes, page-count support, and the `source` field for tracking book origins.

## Base package

`com.bookhub.catalog`
