# BookHub V1 — Bounded Contexts and Service Responsibilities

## Purpose

This document defines the bounded contexts for BookHub V1 and explains how responsibilities should be split across the initial microservices. Its main goal is to prevent a distributed monolith by giving each service a clear business boundary, data ownership model, and interaction contract.

## Design principles

- Prefer business boundaries over technical layers.
- Each service owns its data and behavior.
- Avoid shared databases across services.
- Keep V1 intentionally small: 3 services maximum.
- Favor synchronous HTTP communication in V1.
- Treat cross-service calls as integration points, not shortcuts.
- Preserve room for future extraction without over-designing V1.

## Bounded contexts overview

BookHub V1 is split into three bounded contexts:

1. **Identity**
2. **Catalog**
3. **Library**

These contexts are intentionally aligned with the product scope defined for V1: authentication, book discovery, personal library management, reviews, yearly goals, and in-app notifications.

---

## 1. Identity Context

### Mission

Manage who the user is, how they authenticate, and what permissions they have.

### Core responsibilities

- User registration
- Authentication
- Authorization
- Role assignment
- Password recovery
- User profile base information
- Token issuance and validation

### Owned concepts

- User
- Credential
- Role
- Access Token
- Refresh Token
- Password Reset Token

### Owned data examples

- `users`
- `credentials`
- `roles`
- `recovery_tokens`
- `refresh_tokens` (if used)

### What this context should NOT own

- Book metadata
- Reading progress
- Reviews
- Notifications
- Reading goals

### Public API examples

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/forgot-password`
- `POST /auth/reset-password`
- `GET /users/{id}`
- `PATCH /users/{id}`

### Outbound dependencies

- Email provider for password recovery

### Notes

Identity should be the source of truth for basic user identity and access control, but not for every user-related business concept. Other services may store `userId` references, but not duplicate authentication logic.

---

## 2. Catalog Context

### Mission

Manage book discovery and canonical book metadata used across the platform.

### Core responsibilities

- Book search
- Book detail retrieval
- Author information
- Book import/normalization from external providers
- Local persistence of actively used catalog entries
- Admin-level catalog review and curation

### Owned concepts

- Book
- Author
- Provider
- Normalization metadata
- Catalog moderation status

### Owned data examples

- `books`
- `authors`
- `book_sources`
- `catalog_imports`
- `catalog_audit_log`

### What this context should NOT own

- User credentials
- Shelves
- Reading states
- Reviews as a business capability
- Notifications

### Public API examples

- `GET /books?query=`
- `GET /books/{id}`
- `POST /books/import`
- `GET /authors/{id}`
- `PATCH /admin/books/{id}`

### Outbound dependencies

- Provider API (recommended for V1 bootstrap)

### Notes

Catalog is the source of truth for canonical book metadata. Other services may reference books by `bookId` and optionally keep small read models if needed, but ownership of canonical metadata remains here.

---

## 3. Library Context

### Mission

Manage the user's relationship with Catalog Books: organization, progress, goals, reviews, and user-visible notifications.

### Core responsibilities

- Personal library management
- Shelf organization
- Reading state transitions
- Reading progress tracking
- Opt-in yearly reading goals
- User reviews
- Basic user-visible in-app notifications
- User-facing library metrics

### Owned concepts

- UserBook
- Reading Cycle (future-facing)
- Reading State
- Reading Progress
- Book Snapshot
- Yearly Goal
- Review
- Notification
- Notification status

### Owned data examples

- `user_books`
- `reading_progress`
- `yearly_goals`
- `reviews`
- `notifications`
- `notification_receipts`

### What this context should NOT own

- Authentication
- Password recovery
- Canonical book metadata lifecycle
- Payment/subscription logic
- Friendship/feed/social graph in V1

### Public API examples

- `POST /library/books/{bookId}`
- `PATCH /library/books/{bookId}/status`
- `PATCH /library/books/{bookId}/progress`
- `GET /library/me`
- `PUT /goals/yearly`
- `GET /goals/yearly`
- `POST /reviews`
- `PATCH /reviews/{id}`
- `GET /books/{bookId}/reviews`
- `GET /notifications`
- `PATCH /notifications/{id}/read`

### Outbound dependencies

- Identity service for user validation/authorization context
- Catalog service for canonical book validation/details

### Notes

Library is the most product-centric context in V1. It owns the user's journey around reading, but it should not become a catch-all service. Social graph, payments, and recommendation engines should stay out until later phases.

---

## Context map

### Upstream/downstream relationships

- **Identity** is upstream for authentication and user identity.
- **Catalog** is upstream for canonical book metadata.
- **Library** depends on Identity and Catalog to execute its use cases.

### High-level flow examples

#### Add book to library

1. User authenticates through Identity.
2. Library receives `userId` from the gateway/security layer.
3. Library validates that `bookId` exists in Catalog.
4. Library creates or updates the UserBook.

#### Create review

1. User authenticates through Identity.
2. Library validates the referenced `bookId` with Catalog.
3. Library stores the review.
4. Library may create a user-visible notification later in future phases.

#### Password recovery

1. User requests password reset.
2. Identity creates a recovery token.
3. Identity sends email.
4. Identity validates token and updates credentials.

---

## Ownership rules

### Identity owns

- who the user is
- how the user logs in
- which role the user has
- password lifecycle

### Catalog owns

- what a book is
- what metadata defines a book
- what author data is canonical
- how external book data is normalized

### Library owns

- how a user organizes Catalog Books in a personal library
- what a user is reading
- how progress is measured
- what review a user wrote about a Catalog Book
- what user-visible in-app notifications the user has
- what opt-in yearly goal the user is tracking

---

## Anti-corruption rules

To avoid service leakage:

- Library must not write directly to Catalog tables.
- Catalog must not know about reading states or yearly goals.
- Identity must not store library-specific metrics.
- No service may bypass another service's business rules through direct database access.
- Shared IDs are acceptable; shared mutable business state is not.

---

## Cross-service data strategy

### Identity references

Other services may store:

- `userId`
- optionally a display snapshot such as `username` for read purposes

But the source of truth remains Identity.

### Catalog references

Library may store:

- `bookId`
- optional book snapshot fields such as `title`, `coverUrl`, or `pageCount` for read stability and performance

But Catalog remains the source of truth for canonical metadata.

### Why light denormalization is acceptable

In distributed systems, duplication for read performance is normal. The rule is simple: duplicate for read convenience if needed, but keep ownership explicit.

---

## Why Reviews belong to Library in V1

Reviews could eventually become their own context, but keeping them inside Library in V1 is the right tradeoff.

### Reasons

- Reviews are tightly connected to the user's reading journey, but they remain attached to Catalog Books rather than owned by UserBooks.
- Splitting them now would introduce extra complexity with low architectural payoff.
- V1 needs delivery speed without losing good boundaries.

### Future extraction signal

Reviews should become a separate context/service later if:

- review moderation becomes complex
- comments/reactions/social engagement expand significantly
- feed generation depends heavily on review activity

---

## Why Notifications belong to Library in V1

Notifications are kept inside Library only for the V1 scope of user-facing in-app notifications related to library/review/goal events.

### Reasons

- V1 notifications are simple, in-app only, and scoped to one user.
- A dedicated notification service would be premature.
- The current scope does not justify multi-channel delivery yet.

### Future extraction signal

Notifications should become their own context/service later if:

- email/push/websocket channels are introduced
- notification templates and delivery policies grow
- multiple domains start publishing events to a central notification system

---

## Deliberately excluded contexts for V1

The following contexts are intentionally postponed:

### Social Context

- friendships
- social feed
- post comments
- social graph

### Subscription Context

- freemium plans
- payment lifecycle
- billing rules

### Recommendation Context

- recommendation strategies
- personalization engine
- ranking logic

These are valid future contexts, but they should not dilute the V1 delivery.

---

## Recommended package-level mental model

Even before full implementation, each service should internally reflect clean boundaries:

- `domain`
- `application`
- `infrastructure`
- `interfaces` or `web`

The main point is not folder aesthetics. The point is protecting business rules from transport and persistence concerns.

---

## Open questions to validate during implementation

- How much user profile data should remain in Identity versus a future Profile context?
- Should admin metrics live inside each service or be aggregated separately?
- Is a refresh-token flow necessary for V1, or is a simpler JWT strategy enough?
- Which exact fields from Catalog should be denormalized into Library read models?

---

## Final recommendation

For BookHub V1, the right boundary model is:

- **Identity** for access and user identity
- **Catalog** for canonical book metadata
- **Library** for the user's reading lifecycle

This split is small enough to ship, strong enough to demonstrate real architecture judgment, and flexible enough to evolve into additional contexts later without rebuilding the system from scratch.
