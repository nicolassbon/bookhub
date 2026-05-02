# Identity

This context defines who the user is, how they authenticate, and how access is maintained securely in BookHub. It exists to own identity, credentials, and token lifecycles without leaking authentication concerns into the other contexts.

## Language

**Refresh Token**:
The long-lived credential used to obtain a new access token without re-entering the user's password.
_Avoid_: Session Token, session cookie, login token

**User**:
The basic account identity owned by Identity, including authentication-facing and access-control-facing attributes.
_Avoid_: full product profile, reader journey, social profile

**Password Reset Token**:
The time-bound credential used to authorize a password reset for a specific user.
_Avoid_: Recovery Token, account recovery token, reset secret

**Credential**:
The authentication secret and authentication-state record associated with a user.
_Avoid_: Password (when the broader credential state is meant), login secret, auth blob

**Access Token**:
The short-lived token that authorizes access to protected BookHub resources.
_Avoid_: JWT, auth token, session token

## Relationships

- A **User** owns authentication-facing identity in BookHub.
- An **Access Token** represents authenticated access for one user identity until expiry.
- A **Credential** belongs to exactly one user.
- A **Password Reset Token** belongs to exactly one user and one password-reset request.
- A **Refresh Token** belongs to exactly one authenticated user.

## Example dialogue

> **Dev:** "If the access token expires, do we ask the user to log in again immediately?"
> **Domain expert:** "No — if the user still has a valid **Refresh Token**, Identity can issue a new access token without forcing a new login."

## Flagged ambiguities

- `Session Token` is not the preferred term in Identity; use **Refresh Token** for the persisted renewal credential.
- `JWT` names the token format, not the business concept; use **Access Token** for the credential used to access protected resources.
- `User` in Identity means the basic account identity, not the full product profile across BookHub.
- `Recovery Token` is too broad in Identity; use **Password Reset Token** for the credential that authorizes a password change.
- `Password` is too narrow when lockout and authentication state are included; use **Credential** for the broader authentication record.
