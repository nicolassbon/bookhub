# PRD — Library Service Foundation

## 1. Purpose

`library-service` is the product-centric core of BookHub V1.

Its purpose is to manage the user's relationship with books across the personal reading lifecycle, including:

- personal library entries
- reading state transitions
- reading progress
- yearly goals
- reviews
- in-app notifications

This PRD iteration focuses on building a solid, extensible foundation rather than a minimal CRUD implementation.

## 2. Product goals

The service must:

- support the main Goodreads-like user journey around reading
- preserve domain consistency while allowing realistic user behavior
- prepare the foundation for goals, reviews, and notifications
- avoid redesign when richer lifecycle behavior is introduced later

This service is intended to become one of the most important bounded contexts in the platform.

## 3. Scope of this foundation phase

### In scope now

- personal library entries
- reading states: `WANT_TO_READ`, `READING`, `READ`
- reading progress updates
- automatic synchronization between progress and reading state
- catalog validation for referenced books
- partial book snapshot storage for efficient reads
- domain foundation for future reading-cycle history
- contract alignment for future goals, reviews, and notifications

### Prepared but not fully implemented in this phase

- yearly goals
- reviews
- notifications
- richer lifecycle reactions triggered from domain events
- explicit reading-cycle history model

## 4. Core user outcomes

A user must be able to:

- add a book to their personal library
- mark books as want to read, reading, or read
- update reading progress even when the total page count is unknown
- see progress reflected consistently in the current library state
- re-read a previously finished book without losing prior completion meaning

## 5. Domain principles

### 5.1 Flexible state transitions

The service should avoid over-restricting state transitions.

The model should prefer reacting to transitions through domain behavior and future domain events instead of blocking most transitions up front.

Examples:

- moving to `READ` may trigger review/rating prompts and completion handling
- moving to `READING` may establish or continue a reading period
- moving from `READ` back to `READING` is a valid re-reading scenario

### 5.2 Re-reading is a first-class use case

Re-reading must be supported intentionally.

`READ -> READING` must not be treated as an invalid transition. It represents a new reading cycle instead of overwriting the previous completion semantics.

### 5.3 State and progress are coupled

Reading state and reading progress are not independent.

They must stay synchronized through business rules.

## 6. Functional rules

### 6.1 Library entry rules

- a user cannot have duplicate active library entries for the same book
- adding a missing book requires validating that the `bookId` exists in catalog-service
- all library operations are scoped to the authenticated owner of the entry

### 6.2 Progress rules

- `pagesRead` cannot be negative
- when canonical `pageCount` is known, `pagesRead` cannot exceed it
- progress updates are allowed even when `pageCount` is unknown
- when `pageCount` is unknown, `percentage` must be `null`

### 6.3 Automatic state transitions

- when progress changes from `0` to a positive value, the entry transitions to `READING`
- when progress reaches `100%`, the entry transitions to `READ`
- when a user manually marks an entry as `READ` with progress below `100%`, the system auto-corrects progress to `100%`
- if progress is reduced after an entry is in `READ`, the entry transitions back to `READING`

### 6.4 Completion semantics

- `READ` is the completed state for the current reading cycle
- completion does not mean the user-book relationship is closed forever
- future re-reading must remain possible without losing the meaning of previous completion

## 7. Temporal model guidance

The current model may start with the existing `UserBook` aggregate, but the foundation must be designed with future reading-cycle history in mind.

This means:

- timestamps must not be modeled as disposable technical fields only
- `startedAt`, `finishedAt`, and `lastProgressAt` should reflect meaningful lifecycle semantics
- the domain should be evolvable toward a richer `ReadingCycle` or equivalent historical model without breaking contracts

## 8. Catalog integration rules

`catalog-service` remains the source of truth for canonical book metadata.

### 8.1 Required validations

- validate that a referenced book exists before creating a new library entry
- use catalog metadata when available to enforce progress limits

### 8.2 Error semantics

- catalog `404` must be treated as a business-level "book not found" outcome
- catalog `5xx`, timeout, or other technical failures must be treated as technical integration errors and the operation must be rejected

### 8.3 Snapshot strategy

For efficiency and read decoupling, library-service should persist a partial book snapshot alongside the `bookId`.

The initial snapshot should include at least:

- `title`
- `coverUrl`
- `pageCount`

## 9. API foundation

The current contract baseline remains:

- `POST /api/v1/library/books`
- `GET /api/v1/library/me`
- `GET /api/v1/library/me/books?state={state}`
- `GET /api/v1/library/books/{entryId}`
- `PATCH /api/v1/library/books/{entryId}/state`
- `PATCH /api/v1/library/books/{entryId}/progress`

This phase must ensure the implemented behavior matches the contract semantics for state and progress.

## 10. Foundation for next modules

### 10.1 Yearly goals

Yearly goals are part of the intended library context, but this phase focuses on leaving the service ready for them rather than fully closing the feature.

### 10.2 Reviews

Finishing a book is expected to become a natural trigger for the review flow in a subsequent phase.

### 10.3 Notifications

Notifications should later consume meaningful business transitions from the reading lifecycle, but this phase only prepares the conceptual foundation.

## 11. Non-functional expectations

The library foundation must:

- preserve clear ownership boundaries
- remain compatible with synchronous HTTP integration in V1
- support deterministic tests for core domain behavior
- avoid coupling product rules directly to controllers or persistence details
- remain extensible without major redesign

## 12. Primary risks

### Risk 1 — treating lifecycle as a simple status field

If state changes are modeled as naive field updates, the service will become hard to evolve when re-reading, review triggers, and notification flows are added.

### Risk 2 — mixing business absence with technical integration failures

If catalog lookup errors are flattened into a single outcome, the service will produce incorrect business behavior and poor operational diagnostics.

### Risk 3 — overfitting the first schema to today's simplest flow

If the initial model ignores future reading cycles, the service may require a disruptive redesign later.

## 13. Success criteria for this phase

This foundation phase is successful if:

- library entries are implemented with strong ownership and duplication rules
- state and progress behave consistently under the defined automatic rules
- catalog integration distinguishes business absence from technical failure
- the service persists the minimal book snapshot needed for efficient reads
- the design leaves a credible path toward reading-cycle history, goals, reviews, and notifications
