# library-service

User library service for BookHub.

## Responsibilities

- add books to a user's library
- retrieve a user's library and owned entries
- retrieve a single owned library entry
- update reading state
- update reading progress
- persist stable catalog snapshots for owned books
- validate JWT access tokens for authenticated requests

## HTTP API

- `POST /api/v1/library/books`
- `GET /api/v1/library/me`
- `GET /api/v1/library/me/books`
- `GET /api/v1/library/books/{entryId}`
- `PATCH /api/v1/library/books/{entryId}/state`
- `PATCH /api/v1/library/books/{entryId}/progress`

## Configuration highlights

- Runs on port `8083` by default.
- Requires PostgreSQL connection settings via `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
- Requires JWT settings via `JWT_ISSUER`, `JWT_AUDIENCE`, and `JWT_RSA_PUBLIC_KEY`.
- Depends on `CATALOG_SERVICE_URL` for catalog enrichment and validation.

## Persistence

Flyway migrations live in `src/main/resources/db/migration/` and currently include user books, yearly goals, reviews, notifications, and PRD-alignment changes for the user-books model.

## Scope note

The current implemented HTTP surface is focused on `/api/v1/library/**`. The broader V1 contract also reserves goals, reviews, and notifications as library-owned domains and should remain aligned through future implementation work.

## Base package

`com.bookhub.library`
