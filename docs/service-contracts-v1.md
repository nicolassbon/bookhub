# BookHub V1 — Service Contracts

## Purpose

This document translates the bounded contexts into implementable service contracts for V1. It defines what each service exposes, what it consumes, and how services are expected to interact without breaking ownership boundaries.

## Contract design rules

- Contracts must reflect business capabilities, not internal tables.
- Each service owns its write model.
- Cross-service calls must happen through explicit APIs only.
- V1 favors synchronous HTTP communication.
- Error contracts must be predictable and consistent.
- Admin endpoints are allowed, but must stay scoped to each service's owned domain.

---

## 1. identity-service contract

## Mission

Provide authentication, user identity, access control, and password recovery.

## Public responsibilities

- Register users
- Authenticate users
- Issue and validate Access Tokens
- Rotate Refresh Tokens
- Expose current user identity
- Manage base profile data
- Manage roles
- Handle password recovery

## API surface

### Authentication

#### `POST /api/v1/auth/register`

Creates a new user account.

**Request**

```json
{
  "username": "nico",
  "email": "nico@example.com",
  "password": "StrongPassword123!",
  "displayName": "Nicolas Bon"
}
```

**Response — 201 Created**

```json
{
  "userId": "usr_123",
  "username": "nico",
  "email": "nico@example.com",
  "displayName": "Nicolas Bon",
  "role": "USER"
}
```

#### `POST /api/v1/auth/login`

Authenticates a user and returns an Access Token plus Refresh Token cookie.

**Request**

```json
{
  "email": "nico@example.com",
  "password": "StrongPassword123!"
}
```

**Response — 200 OK**

```json
{
  "accessToken": "jwt-access-token",
  "expiresIn": 3600,
  "user": {
    "userId": "usr_123",
    "username": "nico",
    "displayName": "Nicolas Bon",
    "role": "USER"
  }
}
```

#### `POST /api/v1/auth/refresh`

Rotates the current Refresh Token and returns a new Access Token.

**Request**

- No JSON body.
- Requires `refresh_token` cookie.

**Response — 200 OK**

```json
{
  "accessToken": "new-jwt-access-token",
  "expiresIn": 3600,
  "user": {
    "userId": "usr_123",
    "username": "nico",
    "displayName": "Nicolas Bon",
    "role": "USER"
  }
}
```

- Returns rotated `refresh_token` in `Set-Cookie` (`HttpOnly`, `SameSite=Strict`).
- Returns `401 Unauthorized` when the Refresh Token is invalid, expired, or revoked.

#### `POST /api/v1/auth/logout`

Revokes the current Refresh Token and clears the refresh cookie.

**Request**

- No JSON body.
- `refresh_token` cookie is optional.

**Response — 204 No Content**

- Clears `refresh_token` through `Set-Cookie` with `Max-Age=0`.

#### `POST /api/v1/auth/forgot-password`

Starts the password reset flow.
This endpoint is anti-enumeration safe: it always returns the same response, even if the email does not exist.

**Request**

```json
{
  "email": "nico@example.com"
}
```

**Response — 200 OK**

- Empty body.
- Always returned for syntactically valid requests.

#### `POST /api/v1/auth/reset-password`

Completes the password reset flow using a Password Reset Token.

**Request**

```json
{
  "token": "password-reset-token",
  "newPassword": "NewStrongPassword123!"
}
```

**Response — 200 OK**

- Empty body when token is valid and password is updated.

**Error — 400 Bad Request**

```json
{
  "timestamp": "2026-04-12T20:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "INVALID_PASSWORD_RESET_TOKEN",
  "message": "Invalid or expired password reset token",
  "path": "/api/v1/auth/reset-password"
}
```

#### `POST /api/v1/auth/service-token`

Issues a service JWT for machine-to-machine access. Used by downstream services (e.g., library-service) to authenticate when calling internal endpoints on other services (e.g., catalog-service `/api/v1/internal/**`).

**Authentication**: HTTP Basic with pre-shared service credentials (`SERVICE_CLIENT_ID` / `SERVICE_CLIENT_SECRET`).

**Response — 200 OK**

```json
{
  "accessToken": "service-jwt-token",
  "expiresIn": 3600
}
```

**Error — 401 Unauthorized**

- Returned when the `Authorization` header is missing, is not valid HTTP Basic, or the credentials do not match the expected service identity.

**Rate limiting**: This endpoint is rate-limited independently (default: 20 requests per 60-second window).

### Auth rate limiting

The following auth endpoints are rate-limited independently per endpoint and client fingerprint:

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/forgot-password`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/service-token`

Rate-limit state is shared across `identity-service` instances through a TTL-backed Redis store.
Forwarded client IP headers are trusted only when the immediate source matches configured trusted proxy CIDRs.

**Error — 429 Too Many Requests**

```json
{
  "timestamp": "2026-04-17T00:00:00Z",
  "status": 429,
  "error": "Too Many Requests",
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "Too many requests for this endpoint. Try again later.",
  "path": "/api/v1/auth/login"
}
```

**Error — 503 Service Unavailable**

Returned when the shared rate-limit store is unavailable. The default operational policy is fail-closed.

```json
{
  "timestamp": "2026-04-17T00:00:00Z",
  "status": 503,
  "error": "Service Unavailable",
  "code": "AUTH_RATE_LIMIT_UNAVAILABLE",
  "message": "Authentication rate limiting is temporarily unavailable",
  "path": "/api/v1/auth/login"
}
```

### Users

> Status key used in this document:
> - **Implemented (current)**: endpoint exists in current controllers.
> - **Planned (roadmap)**: endpoint is a contract target, not implemented yet.

#### `GET /api/v1/users/me`

**Implemented (current)**

Returns the authenticated user's profile from persistence (token subject is only used for authorization).

**Response — 200 OK**

```json
{
  "userId": "usr_123",
  "username": "nico",
  "displayName": "Nicolas Bon",
  "email": "nico@example.com",
  "role": "USER",
  "bio": "Backend engineer and book lover",
  "avatarUrl": "https://cdn.bookhub.app/avatars/usr_123.png"
}
```

#### `GET /api/v1/users/{userId}`

**Implemented (current)**

Returns public/basic user profile data.

**Response — 200 OK**

```json
{
  "userId": "usr_123",
  "username": "nico",
  "displayName": "Nicolas Bon",
  "bio": "Backend engineer and book lover",
  "avatarUrl": "https://cdn.bookhub.app/avatars/usr_123.png"
}
```

Sensitive fields (`email`, `role`, credentials, and account status) are never exposed in this endpoint.

#### `PATCH /api/v1/users/me`

**Implemented (current)**

Updates owner editable base profile fields.

**Request**

```json
{
  "displayName": "Nicolas Bon",
  "bio": "Backend engineer and book lover",
  "avatarUrl": "https://cdn.bookhub.app/avatars/usr_123.png"
}
```

Partial updates are accepted: any subset of `displayName`, `bio`, and `avatarUrl` can be sent.

**Error — 400 Bad Request**

- Returned when payload validation fails (blank `displayName`, oversized fields, invalid `avatarUrl`, or out-of-scope field mutation).

**Request**

```json
{
  "displayName": "Nico",
  "bio": "Backend developer and reader",
  "avatarUrl": "https://..."
}
```

### Admin

**Implemented (current)**

#### `GET /api/v1/admin/users`

Lists users with pagination/filtering.

#### `PATCH /api/v1/admin/users/{userId}/role`

Changes a user's role.

## Data exposed to other services

identity-service is allowed to expose lightweight user identity data:

```json
{
  "userId": "usr_123",
  "username": "nico",
  "displayName": "Nicolas Bon",
  "role": "USER",
  "status": "ACTIVE"
}
```

## Internal invariants

- Email must be unique.
- Username must be unique.
- Passwords must never be stored in plain text.
- Role changes must be restricted to authorized admins.
- Recovery tokens must expire.

---

## 2. catalog-service contract

## Mission

Provide canonical book metadata and discovery capabilities.

## Public responsibilities

- Search books
- Return book details
- Persist locally relevant catalog items
- Normalize provider metadata into canonical books
- Provide admin-level catalog curation endpoints

### ID semantics (canonical vs ephemeral)

Lead rule: **Catalog owns the canonical Book ID; ephemeral external IDs exist only at the catalog public edge for discovery/JIT import.**

- **Canonical ID**: UUID generated/owned by catalog-service and persisted across services.
- **Ephemeral external ID**: `ext:ol:{sourceReference}` style identifier used only for provider-backed results that are not yet normalized.
- **Where ephemeral IDs are allowed**: `GET /api/v1/books` results and `GET /api/v1/books/{id}` input.
- **Where canonical IDs are required**: internal catalog APIs and all cross-service persistence references (for example, library-service `bookId`).

## API surface

### Search and details

#### `GET /api/v1/books?q={term}`

Searches books concurrently in local catalog storage and the configured Provider.

Notes:
- `q` is required (`@NotBlank`).
- Local books return canonical UUID IDs.
- Non-persisted external books return ephemeral IDs with format `ext:ol:{sourceReference}`.
- If local and external results match by `sourceReference`/ISBN-13, local result takes precedence.
- Search results expose canonical Books when already normalized and transient provider-backed results when they are not yet persisted.

**Response — 200 OK**

```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "title": "The Hobbit",
    "authorName": "J.R.R. Tolkien",
    "coverUrl": "https://covers.openlibrary.org/b/id/111-L.jpg"
  },
  {
    "id": "ext:ol:OL999W",
    "title": "Unfinished Tales",
    "authorName": "J.R.R. Tolkien",
    "coverUrl": "https://covers.openlibrary.org/b/id/222-L.jpg"
  }
]
```

#### `GET /api/v1/books/{id}`

Returns canonical book detail by either:
- local UUID (`123e4567-e89b-12d3-a456-426614174000`), or
- ephemeral ID (`ext:ol:OL999W`) that triggers JIT import/persistence.

Catalog remains responsible for the normalization step that turns provider metadata into canonical Book data.

**Response — 200 OK**

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "title": "Unfinished Tales",
  "authorName": "J.R.R. Tolkien",
  "isbn13": "9780261102163",
  "sourceReference": "OL999W",
  "coverUrl": "https://covers.openlibrary.org/b/id/222-L.jpg",
  "publishedYear": 1980
}
```

**Error — 400 Bad Request (`INVALID_BOOK_ID`)**

```json
{
  "timestamp": "2026-04-14T15:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "INVALID_BOOK_ID",
  "message": "Book ID format is invalid",
  "path": "/api/v1/books/invalid-id"
}
```

**Error — 404 Not Found (`BOOK_NOT_FOUND`)**

```json
{
  "timestamp": "2026-04-14T15:00:00Z",
  "status": 404,
  "error": "Not Found",
  "code": "BOOK_NOT_FOUND",
  "message": "Book not found",
  "path": "/api/v1/books/ext:ol:OL404W"
}
```

### Internal validation/read contract

#### `GET /api/v1/internal/books/{bookId}`

Returns minimal canonical book data for authenticated internal consumers such as library-service.

`{bookId}` must be a canonical catalog UUID (ephemeral IDs are invalid on internal routes).

**Authentication**: Requires a service JWT with `role=SERVICE` in the `Authorization: Bearer` header. Service tokens are issued by identity-service at `POST /api/v1/auth/service-token`.

**Error — 401 Unauthorized**: Returned when the request lacks a valid service token or the token cannot be authenticated.

**Error — 403 Forbidden**: Returned when the request is authenticated but the caller does not have the required service authority.

**Error — 404 Not Found**: Returned when the requested `bookId` is not a known canonical book UUID.

**Response — 200 OK**

```json
{
  "bookId": "123e4567-e89b-12d3-a456-426614174000",
  "title": "Clean Architecture",
  "coverUrl": "https://...",
  "pageCount": 432,
  "exists": true,
  "status": "ACTIVE"
}
```

### Admin

**Implemented (current)**

#### `POST /api/v1/admin/books/import`

Triggers or stores a normalized import from an external provider.

#### `PATCH /api/v1/admin/books/{bookId}`

Updates curated metadata fields.

#### `GET /api/v1/admin/books`

Lists catalog entries with moderation/import metadata.

## Data exposed to other services

catalog-service is allowed to expose lightweight book snapshots:

```json
{
  "bookId": "123e4567-e89b-12d3-a456-426614174000",
  "title": "Clean Architecture",
  "coverUrl": "https://...",
  "pageCount": 432,
  "status": "ACTIVE"
}
```

## Internal invariants

- Canonical `bookId` (UUID) must be stable.
- Duplicate books from external sources must be normalized.
- Ephemeral IDs (`ext:ol:*`) are API-edge identifiers only and must resolve to canonical UUIDs after JIT persistence.
- Imported metadata must be traceable to a source.
- Admin edits must not break referential consistency.

---

## 3. library-service contract

## Mission

Manage the user-facing reading lifecycle: user-book records, progress, goals, reviews, and in-app notifications.

## Public responsibilities

- Add Catalog Books to a personal library as UserBooks
- Update reading state
- Track reading progress
- Manage yearly reading goals
- Create and edit reviews
- List and update notifications
- Provide library dashboards for the current user

## API surface

### Personal library

#### `POST /api/v1/library/books`

Adds a Catalog Book to the authenticated user's library as a new UserBook.

**Request**

```json
{
  "bookId": "123e4567-e89b-12d3-a456-426614174000",
  "initialState": "WANT_TO_READ"
}
```

**Response — 201 Created**

```json
{
  "entryId": "ub_001",
  "userId": "usr_123",
  "bookId": "123e4567-e89b-12d3-a456-426614174000",
  "bookSnapshot": {
    "title": "Clean Code",
    "coverUrl": "https://covers.openlibrary.org/b/id/111-L.jpg",
    "pageCount": 464
  },
  "readingState": "WANT_TO_READ",
  "readingProgress": {
    "pagesRead": 0,
    "completionPercentage": 0
  }
}
```

#### `GET /api/v1/library/me`

Returns the authenticated user's library summary.

#### `GET /api/v1/library/me/books?state={state}`

Returns library entries filtered by reading state.

#### `GET /api/v1/library/books/{entryId}`

Returns a single UserBook owned by the authenticated user.

The response MUST use the persisted book snapshot (`title`, `coverUrl`, `pageCount`) to keep reads stable even when Catalog metadata changes later.

#### `PATCH /api/v1/library/books/{entryId}/state`

Updates the Reading State.

State transitions are intentionally flexible. The service reacts to transitions instead of hard-rejecting most of them.

Examples of expected behavior:

- manually setting `READ` auto-corrects progress to `100%`
- re-reading (`READ -> READING`) is valid and reopens the lifecycle for the current entry while preserving the path for future historical cycle tracking

**Request**

```json
{
  "state": "READ"
}
```

#### `PATCH /api/v1/library/books/{entryId}/progress`

Updates Reading Progress.

Reading Progress and Reading State are coupled.

- moving progress from `0` to a positive value transitions the entry to `READING`
- reaching `100%` transitions the entry to `READ`
- reducing progress after `READ` transitions the entry back to `READING`
- when canonical page count is unknown, progress is still allowed and `completionPercentage` is returned as `null`

**Request**

```json
{
  "pagesRead": 120
}
```

**Response — 200 OK**

```json
{
  "entryId": "ub_001",
  "pagesRead": 120,
  "completionPercentage": 28,
  "readingState": "READING"
}
```

Example when canonical `pageCount` is unknown:

```json
{
  "entryId": "ub_001",
  "pagesRead": 120,
  "completionPercentage": null,
  "readingState": "READING"
}
```

### Yearly goals

#### `PUT /api/v1/goals/yearly`

Creates or updates the current user's opt-in Yearly Goal for a specific calendar year.

**Request**

```json
{
  "year": 2026,
  "targetBooks": 24
}
```

#### `GET /api/v1/goals/yearly`

Returns the current user's yearly goal and progress.

Goal progress is measured only by completed `UserBook` records within the requested calendar year. Repeated completion of the same `Catalog Book` only counts again when it happens in a different calendar year.

### Reviews

#### `POST /api/v1/reviews`

Creates a Review for a Catalog Book.

**Request**

```json
{
  "bookId": "123e4567-e89b-12d3-a456-426614174000",
  "rating": 5,
  "content": "Excellent book about software architecture."
}
```

#### `PATCH /api/v1/reviews/{reviewId}`

Updates an existing review owned by the authenticated user.

#### `GET /api/v1/books/{bookId}/reviews`

Returns visible reviews for a given Catalog Book.

### Notifications

#### `GET /api/v1/notifications`

Returns the authenticated user's user-visible in-app notifications.

#### `PATCH /api/v1/notifications/{id}/read`

**Implemented (current)**

Marks a notification as read.

In V1, notifications only transition between `UNREAD` and `READ`.

### Admin

**Implemented (current)**

#### `GET /api/v1/admin/reviews`

Lists reviews for moderation.

#### `PATCH /api/v1/admin/reviews/{reviewId}/status`

Updates moderation status for a review.

#### `GET /api/v1/admin/metrics/library`

Returns initial library/review metrics.

## Data exposed to other services

V1 does not require library-service to act as a shared upstream service for core write operations. It mainly serves end-user and admin use cases through the gateway.

## Internal invariants

- A user cannot have duplicate active Library Entries for the same Catalog Book.
- Reading progress cannot be negative.
- Reading progress cannot exceed the known page count when page count is available.
- Reading progress is still allowed when page count is unknown, but completion percentage becomes `null`.
- Reading Progress and Reading State must stay synchronized through business rules.
- Re-reading is a valid lifecycle scenario for a UserBook.
- A review must belong to exactly one user and exactly one Catalog Book.
- A user may have at most one review per Catalog Book in V1.
- A user can only edit their own review.
- A user may have at most one Yearly Goal per calendar year.
- Yearly Goal progress is measured only by completed UserBooks in that calendar year.
- Changing a yearly target updates the same goal instead of creating a new one.
- A notification is always scoped to exactly one user.
- Notification status transitions must be valid.

---

## 4. Cross-service interaction rules

## identity-service → library-service

Interaction style:

- usually indirect through JWT claims and gateway security
- optional direct lookup for admin or profile enrichment cases

Allowed data shared:

- `userId`
- `role`
- `username`
- `displayName`

Not allowed:

- password hashes
- recovery tokens
- authentication internals

## catalog-service → library-service

Interaction style:

- direct synchronous validation/read call

Allowed use cases:

- validate `bookId`
- fetch canonical `pageCount`
- fetch title/cover snapshots

Error semantics expected by library-service:

- `404` means the referenced book does not exist for the requested operation
- `5xx`, timeouts, or transport failures must be treated as technical integration failures, not as business absence

Not allowed:

- writing library states inside catalog-service
- embedding review behavior in catalog-service

## library-service → catalog-service

Interaction style:

- direct authenticated HTTP call with a service JWT

Authentication:

- library-service acquires a service token from identity-service (`POST /api/v1/auth/service-token`) using its pre-shared credentials.
- library-service uses that token to authenticate calls to catalog internal APIs.
- Token lifecycle management is an implementation concern; the contract requirement is that internal calls must present a valid service token and must not fall back to user tokens.

Allowed use cases:

- verify that a book exists
- enrich read models with book snapshots
- persist a partial snapshot (`title`, `coverUrl`, `pageCount`) to optimize library reads while keeping catalog as the source of truth

library-service must not:

- mutate canonical book metadata directly
- import books by bypassing catalog-service rules

---

## 5. Common API conventions

## Error response format

All services should converge on a consistent error structure:

```json
{
  "timestamp": "2026-04-09T18:00:00Z",
  "status": 400,
  "error": "Validation Error",
  "code": "VALIDATION_ERROR",
  "message": "pagesRead must be greater than or equal to zero",
  "path": "/api/v1/library/books/ub_001/progress"
}
```

## Pagination response shape

```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

## Auth conventions

- Public endpoints must be explicitly marked public.
- User endpoints require `USER` or `ADMIN`.
- Admin endpoints require `ADMIN`.
- Internal endpoints (e.g., `/api/v1/internal/**`) require a service JWT with `role=SERVICE`, not a user token.
- Service tokens are issued by identity-service via `POST /api/v1/auth/service-token` using HTTP Basic with pre-shared credentials.
- Services that call internal endpoints on other services must present a valid service token and must not reuse end-user tokens for machine-to-machine calls.
- Services must trust gateway/security context, not user-supplied IDs in request bodies.

---

## 6. Critical V1 end-to-end flows

### Flow A — Register and log in

1. Client calls identity-service register endpoint.
2. Client logs in through identity-service.
3. Gateway forwards authenticated traffic using JWT.

### Flow B — Search and add a book to the library

1. Client searches books via catalog-service.
2. Client selects a book.
3. Client calls library-service to add the book.
4. Library validates the `bookId` with catalog-service.
5. Library stores the user-book entry.

### Flow C — Update reading progress

1. Client sends progress update to library-service.
2. Library loads the entry.
3. Library validates business rules.
4. Library recalculates percentage and possibly state.

### Flow D — Create a review

1. Client submits review to library-service.
2. Library validates user identity.
3. Library validates book identity with catalog-service.
4. Library persists the review.

### Flow E — Password recovery

1. Client requests password reset through identity-service.
2. Identity sends email.
3. Client submits token + new password.
4. Identity validates and updates credentials.

---

## 7. Deliberate exclusions from V1 contracts

These APIs are intentionally not part of V1 contracts:

- friendship endpoints
- social feed endpoints
- payment/subscription endpoints
- recommendation engine endpoints
- multi-channel notification delivery endpoints

---

## Final recommendation

The V1 contracts should stay intentionally narrow. If a contract looks convenient but crosses ownership boundaries, it is probably wrong. The goal is not to expose everything fast; the goal is to make service boundaries survivable.
