# Context Map

## Contexts

- [Identity](./services/identity-service/CONTEXT.md) — defines platform identity, authentication, and access control.
- [Catalog](./services/catalog-service/CONTEXT.md) — defines canonical book metadata and discovery.
- [Library](./services/library-service/CONTEXT.md) — defines the user's relationship with books, reading progress, and library-owned workflows.

## Relationships

- **Identity → Library**: Library relies on Identity for authenticated user identity but does not own credentials or authentication rules.
- **Identity → Catalog**: Catalog may trust identity-derived access context when security rules are introduced, but it does not own user identity.
- **Catalog → Library**: Catalog owns canonical book metadata; Library references catalog books and stores library-specific snapshots.

## Operational Boundaries (AGENTS.md Alignment)

To enforce the architecture rules from `AGENTS.md`, the following strict operational boundaries must be respected:

1. **Cross-Context Communication:**
   - **Rule:** Cross-context data enters only through explicit APIs (synchronous HTTP via Gateway/Feign/WebClient) or events.
   - **Why:** Enforces the "explicit APIs only" rule. No service is allowed to directly read another service's database.

2. **Catalog vs. Library Boundary:**
   - **Rule:** Library stores `catalogBookId` as a reference. It never owns or writes Catalog metadata.
   - **Rule:** If Library needs book details (e.g., title, author) for presentation, it either fetches them via API or stores an immutable snapshot.
   - **Why:** Protects the "Catalog owns canonical book metadata" principle.

3. **Identity vs. Library Boundary:**
   - **Rule:** Identity never owns or knows about reading states, shelves, or reviews.
   - **Rule:** Library never owns credentials or token logic; it blindly trusts the validated `userId` injected by the Gateway/Security Context.
   - **Why:** Maintains strict bounded contexts. Identity is auth-only; Library is domain-only.

4. **Persistence Isolation:**
   - **Rule:** Each context is strictly isolated to its own database schema (`bookhub_identity`, `bookhub_catalog`, `bookhub_library`).
   - **Why:** Enforces the "database per service" mandate.
