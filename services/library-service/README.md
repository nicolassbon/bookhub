# library-service

User library service for BookHub.

## Responsibilities

- add books to a user's library
- retrieve a user's library and owned entries
- retrieve a single owned library entry
- update reading state
- update reading progress
- manage yearly reading goals
- create and update user reviews
- retrieve reviews by catalog book
- retrieve user notifications and mark them as read
- admin review moderation and platform-wide metrics (requires `ROLE_ADMIN`)
- persist stable catalog snapshots for owned books

## HTTP API

### Library

- `POST /api/v1/library/books`
- `GET /api/v1/library/me`
- `GET /api/v1/library/me/books`
- `GET /api/v1/library/books/{entryId}`
- `PATCH /api/v1/library/books/{entryId}/state`
- `PATCH /api/v1/library/books/{entryId}/progress`

### Goals

- `PUT /api/v1/goals/yearly`
- `GET /api/v1/goals/yearly`

### Reviews

- `POST /api/v1/reviews`
- `PATCH /api/v1/reviews/{reviewId}`
- `GET /api/v1/books/{bookId}/reviews`

### Notifications

- `GET /api/v1/notifications`
- `PATCH /api/v1/notifications/{id}/read`

### Admin (requires ROLE_ADMIN)

- `GET /api/v1/admin/reviews` — list reviews for moderation
- `PATCH /api/v1/admin/reviews/{reviewId}/status` — moderate a review (APPROVE/REJECT/FLAG)
- `GET /api/v1/admin/metrics/library` — platform-wide library and social metrics


## Configuration highlights

- Runs on port `8083` by default.
- Requires PostgreSQL connection settings via `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
- Requires JWT settings via `JWT_ISSUER`, `JWT_AUDIENCE`, and `JWT_RSA_PUBLIC_KEY`.
- Depends on `CATALOG_SERVICE_URL` for catalog enrichment and validation.
- Depends on `IDENTITY_SERVICE_URL` for service token acquisition.
- Requires `SERVICE_CLIENT_ID` and `SERVICE_CLIENT_SECRET` for authenticating to identity-service's service-token endpoint.

## Persistence

Flyway migrations live in `src/main/resources/db/migration/` and currently include user books, yearly goals, reviews, notifications, and PRD-alignment changes for the user-books model.

## Scope note

The implemented HTTP surface currently spans library entries, yearly goals, reviews, and notifications under `/api/v1/**` as listed above.

## Base package

`com.bookhub.library`
