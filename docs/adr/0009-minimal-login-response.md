# ADR-0009 — Keep login response minimal and hide refresh token details

- **Status**: Accepted
- **Date**: 2026-04-09

## Context

The login slice needs to authenticate the user and return enough information for the client to proceed.

At this stage, the project has not finalized the refresh-token strategy yet. Possible future options include:

- refresh token in response body
- refresh token in secure HTTP-only cookie
- refresh token persistence and rotation rules

Exposing refresh token details too early would lock the API into a contract before the security design is complete.

## Decision

For the current login slice, return only:

- `accessToken`
- `expiresIn`
- `user`

Keep any refresh-token handling internal to the token issuing seam until the real JWT and refresh strategy is implemented.

## Consequences

### Positive

- Keeps the login contract small and client-focused.
- Avoids leaking security details before the refresh strategy is fully designed.
- Makes it easier to later move refresh token delivery to a more secure channel.

### Negative

- The public login contract may need to evolve later when refresh support is finalized.

## Rejected alternatives

### Expose refresh token in the response body now

Rejected because the client does not need it yet and the security approach is not finalized.

### Design the full JWT + refresh lifecycle before shipping login

Rejected for the current slice because it would slow down incremental delivery. A temporary seam is acceptable while the public response stays minimal.
