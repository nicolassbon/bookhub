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
- Issue and validate tokens
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

Authenticates a user and returns access credentials.

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
  "refreshToken": "refresh-token-if-enabled",
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

Refreshes an access token if refresh tokens are enabled.

#### `POST /api/v1/auth/forgot-password`

Starts the password recovery flow.

**Request**

```json
{
  "email": "nico@example.com"
}
```

#### `POST /api/v1/auth/reset-password`

Completes the password reset flow.

**Request**

```json
{
  "token": "recovery-token",
  "newPassword": "NewStrongPassword123!"
}
```

### Users

#### `GET /api/v1/users/me`

Returns the authenticated user's profile.

#### `GET /api/v1/users/{userId}`

Returns public/basic user profile data.

#### `PATCH /api/v1/users/{userId}`

Updates editable base profile fields.

**Request**

```json
{
  "displayName": "Nico",
  "bio": "Backend developer and reader",
  "avatarUrl": "https://..."
}
```

### Admin

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
- Normalize external book data
- Provide admin-level catalog curation endpoints

## API surface

### Search and details

#### `GET /api/v1/books?query={term}&page={n}&size={n}`

Searches books by title and/or author.

**Response — 200 OK**

```json
{
  "items": [
    {
      "bookId": "bk_001",
      "title": "Clean Architecture",
      "authors": ["Robert C. Martin"],
      "coverUrl": "https://...",
      "publishedYear": 2017,
      "source": "OPEN_LIBRARY"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

#### `GET /api/v1/books/{bookId}`

Returns the canonical book detail.

**Response — 200 OK**

```json
{
  "bookId": "bk_001",
  "title": "Clean Architecture",
  "subtitle": null,
  "description": "A guide to software structure and design.",
  "authors": [
    {
      "authorId": "au_001",
      "name": "Robert C. Martin"
    }
  ],
  "isbn13": "9780134494166",
  "pageCount": 432,
  "language": "en",
  "coverUrl": "https://...",
  "publishedYear": 2017,
  "categories": ["Software", "Architecture"]
}
```

### Internal validation/read contract

#### `GET /api/v1/internal/books/{bookId}`

Returns minimal canonical book data for trusted internal consumers such as library-service.

**Response — 200 OK**

```json
{
  "bookId": "bk_001",
  "title": "Clean Architecture",
  "coverUrl": "https://...",
  "pageCount": 432,
  "exists": true,
  "status": "ACTIVE"
}
```

### Admin

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
  "bookId": "bk_001",
  "title": "Clean Architecture",
  "coverUrl": "https://...",
  "pageCount": 432,
  "status": "ACTIVE"
}
```

## Internal invariants

- Canonical `bookId` must be stable.
- Duplicate books from external sources must be normalized.
- Imported metadata must be traceable to a source.
- Admin edits must not break referential consistency.

---

## 3. library-service contract

## Mission

Manage the user-facing reading lifecycle: library, shelves, progress, goals, reviews, and in-app notifications.

## Public responsibilities

- Add books to a personal library
- Update reading state
- Track progress
- Manage yearly reading goals
- Create and edit reviews
- List and update notifications
- Provide library dashboards for the current user

## API surface

### Personal library

#### `POST /api/v1/library/books`

Adds a book to the authenticated user's library.

**Request**

```json
{
  "bookId": "bk_001",
  "initialState": "WANT_TO_READ"
}
```

**Response — 201 Created**

```json
{
  "entryId": "ub_001",
  "userId": "usr_123",
  "bookId": "bk_001",
  "state": "WANT_TO_READ",
  "progress": {
    "pagesRead": 0,
    "percentage": 0
  }
}
```

#### `GET /api/v1/library/me`

Returns the authenticated user's library summary.

#### `GET /api/v1/library/me/books?state={state}`

Returns library entries filtered by reading state.

#### `PATCH /api/v1/library/books/{entryId}/state`

Updates the reading state.

**Request**

```json
{
  "state": "READ"
}
```

#### `PATCH /api/v1/library/books/{entryId}/progress`

Updates reading progress.

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
  "percentage": 28,
  "state": "READING"
}
```

### Yearly goals

#### `PUT /api/v1/goals/yearly`

Creates or updates the current user's yearly goal.

**Request**

```json
{
  "year": 2026,
  "targetBooks": 24
}
```

#### `GET /api/v1/goals/yearly`

Returns the current user's yearly goal and progress.

### Reviews

#### `POST /api/v1/reviews`

Creates a review for a book.

**Request**

```json
{
  "bookId": "bk_001",
  "rating": 5,
  "content": "Excellent book about software architecture."
}
```

#### `PATCH /api/v1/reviews/{reviewId}`

Updates an existing review owned by the authenticated user.

#### `GET /api/v1/books/{bookId}/reviews`

Returns reviews for a given book.

### Notifications

#### `GET /api/v1/notifications`

Returns the authenticated user's in-app notifications.

#### `PATCH /api/v1/notifications/{notificationId}/read`

Marks a notification as read.

### Admin

#### `GET /api/v1/admin/reviews`

Lists reviews for moderation.

#### `PATCH /api/v1/admin/reviews/{reviewId}/status`

Updates moderation status for a review.

#### `GET /api/v1/admin/metrics/library`

Returns initial library/review metrics.

## Data exposed to other services

V1 does not require library-service to act as a shared upstream service for core write operations. It mainly serves end-user and admin use cases through the gateway.

## Internal invariants

- A user cannot have duplicate active library entries for the same book.
- Reading progress cannot be negative.
- Reading progress cannot exceed the known page count when page count is available.
- A review must belong to exactly one user and one book.
- A user can only edit their own review.
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

Not allowed:

- writing library states inside catalog-service
- embedding review behavior in catalog-service

## library-service → catalog-service

library-service may:

- verify that a book exists
- enrich read models with book snapshots

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
