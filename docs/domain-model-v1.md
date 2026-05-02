# BookHub V1 — Domain Model

## Purpose

This document defines the initial domain model for BookHub V1 by service. It focuses on business concepts, aggregate boundaries, and invariants rather than persistence details.

## Modeling principles

- Model behavior before tables.
- Aggregates protect business invariants.
- Value objects should capture concepts with rules, not just grouped fields.
- Keep aggregates small enough for transactional consistency.
- V1 models should optimize for clarity and correct behavior over abstraction.

---

## 1. identity-service domain model

## Main aggregate: User

### Purpose

Represents an authenticated platform user and the base account identity.

### Core attributes

- `userId`
- `username`
- `email`
- `displayName`
- `bio`
- `avatarUrl`
- `role`
- `status`
- `createdAt`
- `updatedAt`

### Behaviors

- register
- update profile
- change role
- activate/deactivate account

### Invariants

- username is unique
- email is unique
- role must be valid
- inactive users cannot authenticate

## Supporting aggregate/entity: Credential

### Purpose

Stores authentication material linked to a user.

### Core attributes

- `userId`
- `passwordHash`
- `passwordUpdatedAt`
- `failedAttempts`
- `lockedUntil`

### Behaviors

- verify password
- change password
- register failed login attempt
- clear login failures

### Invariants

- password is stored only as a hash
- password changes invalidate recovery state when applicable

## Supporting entity: RecoveryToken

### Purpose

Represents a password recovery request.

### Core attributes

- `tokenId`
- `userId`
- `tokenHash` or secure token representation
- `expiresAt`
- `usedAt`

### Invariants

- token must expire
- used token cannot be reused

## Value objects

- `EmailAddress`
- `Username`
- `PasswordHash`
- `Role`
- `UserStatus`

---

## 2. catalog-service domain model

## Main aggregate: Book

### Purpose

Represents the canonical definition of a book in the platform.

### Core attributes

- `bookId`
- `title`
- `subtitle`
- `description`
- `isbn10`
- `isbn13`
- `pageCount`
- `language`
- `coverUrl`
- `publishedYear`
- `status`
- `sourceType`
- `sourceReference`

### Behaviors

- create from normalized import
- update curated metadata
- activate/deactivate catalog visibility
- attach author references

### Invariants

- canonical book identity must be stable
- page count cannot be negative
- a book must have at least a title

## Supporting aggregate/entity: Author

### Purpose

Represents canonical author metadata.

### Core attributes

- `authorId`
- `name`
- `bio`
- `birthYear`
- `deathYear`

### Invariants

- author name cannot be blank

## Supporting aggregate/entity: CatalogImport

### Purpose

Tracks external metadata ingestion.

### Core attributes

- `importId`
- `provider`
- `providerReference`
- `rawPayloadReference`
- `importedAt`
- `normalizationStatus`

### Behaviors

- register import
- mark import as normalized
- mark import as rejected

## Value objects

- `BookTitle`
- `Isbn`
- `LanguageCode`
- `CatalogStatus`
- `SourceReference`

---

## 3. library-service domain model

## Main aggregate: UserBook

### Purpose

Represents the relationship between a user and a book in the personal library.

### Core attributes

- `entryId`
- `userId`
- `bookId`
- `bookSnapshot` (`title`, `coverUrl`, `pageCount`)
- `state`
- `pagesRead`
- `percentage`
- `startedAt`
- `finishedAt`
- `addedAt`
- `lastProgressAt`

### Behaviors

- add to library
- change reading state
- update progress
- mark as finished
- recalculate percentage
- react to re-reading transitions without losing completion meaning
- prepare future reading-cycle history

### Invariants

- one active entry per user-book pair
- pages read cannot be negative
- percentage must stay between 0 and 100 when known
- percentage may be `null` when canonical page count is unknown
- finished state implies 100% progress when page count is known
- progress greater than zero implies `READING` or `READ`
- reducing progress after completion reopens the entry as `READING`

### Modeling note

V1 starts with `UserBook` as the main aggregate, but the model must remain evolvable toward explicit reading-cycle history. Re-reading (`READ -> READING`) is a valid scenario and should be interpreted as a new cycle rather than as destructive overwriting of previous completion semantics.

## Supporting aggregate: YearlyGoal

### Purpose

Represents the user's reading goal for a specific year.

### Core attributes

- `goalId`
- `userId`
- `year`
- `targetBooks`
- `completedBooks`
- `status`

### Behaviors

- create goal
- update target
- increment progress
- mark achieved

### Invariants

- one goal per user per year
- target books must be greater than zero
- completed books cannot be negative

## Supporting aggregate: Review

### Purpose

Represents a user's opinion about a book.

### Core attributes

- `reviewId`
- `userId`
- `bookId`
- `rating`
- `content`
- `status`
- `createdAt`
- `updatedAt`

### Behaviors

- create review
- edit content
- change moderation status

### Invariants

- rating must be within the accepted range
- review must belong to one user and one book
- only the owner can edit the review in normal user flows

## Supporting aggregate: Notification

### Purpose

Represents an in-app notification visible to a user.

### Core attributes

- `notificationId`
- `userId`
- `type`
- `title`
- `message`
- `payload`
- `status`
- `createdAt`
- `readAt`

### Behaviors

- create notification
- mark as read

### Invariants

- notification must belong to one user
- read notification must have a `readAt` timestamp

## Value objects

- `ReadingState`
- `ReadingProgress`
- `Rating`
- `GoalStatus`
- `NotificationType`
- `NotificationStatus`

---

## Aggregate boundaries and transactional rules

## identity-service

- `User` and `Credential` may live in the same transaction boundary depending on implementation.
- `RecoveryToken` should remain isolated enough to avoid accidental reuse or race conditions.

## catalog-service

- `Book` is the main consistency boundary.
- `Author` can be managed independently, with references from books.
- `CatalogImport` should not force `Book` to expose raw provider concerns.

## library-service

- `UserBook` should own reading state transitions and progress rules.
- `YearlyGoal` should not be overloaded with library entry behavior.
- `Review` moderation state should stay independent from user-book lifecycle.
- `Notification` should remain lightweight and not become a workflow engine.

---

## Core business rules by service

## identity-service

- A user registers with unique email and username.
- Password recovery must be time-bound.
- Account status controls access.

## catalog-service

- A book is canonical once normalized and stored.
- External provider duplication must be reconciled.
- Catalog curation must preserve stable identity.

## library-service

- A user manages their own reading lifecycle.
- Progress updates are constrained by reading rules.
- Reviews are user-owned content attached to books.
- Yearly goals track reading outcome, not intent.
- Notifications are informational and user-scoped.

---

## Suggested enums

## identity-service

- `Role`: `USER`, `ADMIN`
- `UserStatus`: `ACTIVE`, `INACTIVE`, `SUSPENDED`

## catalog-service

- `CatalogStatus`: `ACTIVE`, `HIDDEN`, `REJECTED`
- `SourceType`: `OPEN_LIBRARY`, `MANUAL`, `OTHER`
- `NormalizationStatus`: `PENDING`, `NORMALIZED`, `REJECTED`

## library-service

- `ReadingState`: `WANT_TO_READ`, `READING`, `READ`
- `GoalStatus`: `IN_PROGRESS`, `ACHIEVED`, `MISSED`
- `ReviewStatus`: `VISIBLE`, `HIDDEN`, `FLAGGED`
- `NotificationStatus`: `UNREAD`, `READ`
- `NotificationType`: `GOAL_PROGRESS`, `GOAL_ACHIEVED`, `REVIEW_CREATED`, `SYSTEM`

---

## Important modeling tradeoffs

## Why `UserBook` is the main aggregate in Library

Because the personal library is the core user-book relationship. If this concept is weak, everything else becomes scattered. Reading state and progress belong together.

## Why `Review` is separate from `UserBook`

Although related, review lifecycle and moderation are different concerns from reading progress. Keeping `Review` separate makes future extraction easier.

## Why `Notification` stays simple

V1 only needs in-app notifications. The model should not assume email, push, or websocket complexity yet.

---

## Future extraction signals

The following signs would justify new aggregates or even new services later:

- friendship and feed logic becoming central
- review comments/reactions/moderation growing significantly
- recommendation logic requiring its own ranking model
- notification delivery expanding to multiple channels
- subscription rules affecting multiple user actions again

---

## Final recommendation

Keep the V1 domain model small, behavior-focused, and explicit. The model should tell the story of the product:

- Identity answers **who the user is**
- Catalog answers **what a book is**
- Library answers **what the user is doing with the book**

If the model preserves that separation, implementation will be dramatically cleaner.
