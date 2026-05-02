# Library

This context defines the user's relationship with books inside BookHub. It exists to model personal reading workflows without taking ownership of identity or canonical catalog metadata.

## Language

**UserBook**:
The user's V1-level relationship record for a specific Catalog Book in their personal library.
_Avoid_: Library Entry, book row, shelf row

**Reading Cycle**:
A distinct reading attempt of the same Catalog Book by the same user, treated as a future concept beyond the current V1 entry model.
_Avoid_: current UserBook, reopened UserBook

**Catalog Book**:
The canonical book owned by the Catalog context and referenced by a UserBook.
_Avoid_: Book (when the distinction matters), library book, owned book

**Book Snapshot**:
The subset of Catalog Book data copied into a UserBook for library workflows.
_Avoid_: Catalog Snapshot, embedded book, cached book

**Reading State**:
The reader-facing lifecycle of a UserBook: `WANT_TO_READ`, `READING`, or `READ`.
_Avoid_: Reading Status, Library State, entry status

**Reading Progress**:
The measured advancement of a UserBook through its Catalog Book, expressed through pages read and percentage when known.
_Avoid_: Progress Update, reading metric, completion metric

**Completion Percentage**:
The optional derived percentage of a UserBook's Reading Progress when the Catalog Book page count is known.
_Avoid_: progress percentage, progress status, completion status

**Re-reading**:
The scenario where a reader starts reading again after a UserBook had already reached `READ`.
_Avoid_: Entry Reopened, Reading Cycle Restart

**Review**:
A user-owned opinion attached to a Catalog Book, expressed through a mandatory rating and optional text, whose visibility depends on moderation state.
_Avoid_: review entry, book comment, library review row

**Yearly Goal**:
An opt-in user-owned yearly target measured only by the number of completed UserBooks in a calendar year, not by raw Catalog Books; repeated completion only counts when it occurs in a different calendar year, and target changes adjust the same goal rather than creating a new one.
_Avoid_: generic goal, reading target type, yearly challenge instance

**Notification**:
A user-visible in-app message owned by exactly one user, not a raw internal event; it may communicate something that already happened or prompt the user toward a library-related action.
_Avoid_: domain event, system event, global announcement

## Relationships

- A **UserBook** belongs to exactly one user identity.
- A **UserBook** references exactly one **Catalog Book**.
- A **UserBook** may persist one **Book Snapshot** derived from its **Catalog Book**.
- A **UserBook** always has one **Reading State**.
- A **UserBook** may have **Reading Progress** even when percentage is unknown.
- **Completion Percentage** belongs to **Reading Progress** and may be unknown.
- **Re-reading** moves a **UserBook** from `READ` back to `READING` without changing the fact that the reader had completed it before.
- In V1, one **UserBook** represents the user's total relationship with a **Catalog Book**, not a single **Reading Cycle**.
- A **Review** belongs to exactly one user and exactly one **Catalog Book**.
- In V1, a user may have at most one **Review** per **Catalog Book**.
- A **Review** is not owned by a **UserBook**, even if the reading journey triggers it.
- A **Yearly Goal** belongs to exactly one user and one calendar year.
- A user may have at most one **Yearly Goal** per calendar year.
- **Yearly Goal** progress is measured only by completed **UserBooks**.
- Repeated completion of the same **Catalog Book** only counts again when it happens in a different calendar year.
- A **Notification** belongs to exactly one user.
- In V1, a **Notification** only transitions between `UNREAD` and `READ`.

## Example dialogue

> **Dev:** "If a reader changes progress, are we editing the catalog book?"
> **Domain expert:** "No — progress belongs to the **UserBook**. The **Catalog Book** stays canonical, and the record only keeps a **Book Snapshot** for library workflows."

> **Dev:** "If a completed entry goes from `READ` back to `READING`, did we lose the completion?"
> **Domain expert:** "No — that's **Re-reading**. The entry is active again, but we still treat prior completion as part of its meaning."

## Flagged ambiguities

- `Library Entry` is not the preferred canonical term; use **UserBook**.
- `Book` is too ambiguous inside the Library context; use **Catalog Book** when referring to the upstream canonical book.
- `Catalog Snapshot` is not the preferred term in Library; use **Book Snapshot**.
- A **UserBook** is not the same thing as a **Reading Cycle**; explicit per-cycle modeling is future-facing, not current V1 behavior.

## Pending glossary candidates
