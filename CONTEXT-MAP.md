# Context Map

## Contexts

- [Identity](./services/identity-service/CONTEXT.md) — defines platform identity, authentication, and access control.
- [Catalog](./services/catalog-service/CONTEXT.md) — defines canonical book metadata and discovery.
- [Library](./services/library-service/CONTEXT.md) — defines the user's relationship with books, reading progress, and library-owned workflows.

## Relationships

- **Identity → Library**: Library relies on Identity for authenticated user identity but does not own credentials or authentication rules.
- **Identity → Catalog**: Catalog may trust identity-derived access context when security rules are introduced, but it does not own user identity.
- **Catalog → Library**: Catalog owns canonical book metadata; Library references catalog books and stores library-specific snapshots.
