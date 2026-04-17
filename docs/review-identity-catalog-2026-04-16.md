# Security & Quality Review — Identity Service & Catalog Service

**Date:** 2026-04-16
**Reviewed by:** AI Engineering Review
**Services:** `identity-service`, `catalog-service`

---

## Table of Contents

- [Identity Service Review](#identity-service-review)
  - [1. JWT Security — NimbusJwtTokenIssuer](#1-jwt-security--nimbusjtwtokenissuer)
  - [2. Refresh Tokens — RefreshTokenRepository](#2-refresh-tokens--refreshtokenrepository)
  - [3. Logout — LogoutUserService](#3-logout--logoutuserservice)
  - [4. Password Reset Token — PasswordResetToken](#4-password-reset-token--passwordresettoken)
  - [5. Forgot Password — ForgotPasswordService](#5-forgot-password--forgotpasswordservice)
  - [6. Reset Password — ResetPasswordService](#6-reset-password--resetpasswordservice)
  - [7. Security Config — SecurityConfig](#7-security-config--securityconfig)
  - [8. Global Exception Handler — GlobalExceptionHandler](#8-global-exception-handler--globalexceptionhandler)
  - [9. Rate Limiting](#9-rate-limiting)
  - [10. Password in Infrastructure — UserJpaRepository](#10-password-in-infrastructure--userjparepository)
  - [11. Register — RegisterUserService](#11-register--registeruserservice)
  - [12. Domain Validation — User Aggregate](#12-domain-validation--user-aggregate)
  - [13. Immutability — RefreshToken & PasswordResetToken](#13-immutability--refreshtoken--passwordresettoken)
- [Catalog Service Review](#catalog-service-review)
  - [1. External Provider Resilience — OpenLibraryClient](#1-external-provider-resilience--openliblaryclient)
  - [2. Graceful Degradation — ExternalProviderException](#2-graceful-degradation--externalprovidexception)
  - [3. Merge Logic — SearchResultMerger](#3-merge-logic--searchresultmerger)
  - [4. Normalization Edge Cases — BookNormalization](#4-normalization-edge-cases--booknormalization)
  - [5. Search Performance — CatalogSearchConfig](#5-search-performance--catalogsearchconfig)
  - [6. Caching](#6-caching)
  - [7. Input Validation — BookController](#7-input-validation--bookcontroller)
  - [8. ID Validation — InvalidBookIdException](#8-id-validation--invalidbookidexception)
  - [9. Entity Mapper — BookEntityMapper](#9-entity-mapper--bookentitymapper)
  - [10. Web Mapper / DTOs — BookWebMapper](#10-web-mapper--dtos--bookwebmapper)
  - [11. DB Schema — V1__init_catalog_schema.sql](#11-db-schema--v1__init_catalog_schemasql)
  - [Bonus: Silent Test Omission](#bonus-silent-test-omission)
- [Executive Summary by Priority](#executive-summary-by-priority)

---

## Identity Service Review

> File references are relative to
> `services/identity-service/src/main/java/com/bookhub/identity/`
> unless noted otherwise.

---

### 1. JWT Security — `NimbusJwtTokenIssuer`

**Files:** `infrastructure/security/NimbusJwtTokenIssuer.java`, `config/SecurityConfig.java`, `resources/application.yml`

#### Algorithm — HS256 (HMAC-SHA256) ⚠️

Uses a **symmetric** algorithm: the same secret both signs and verifies tokens. In a
microservices architecture this means every service that needs to verify tokens must possess
the signing secret. If any one of those services is compromised, the attacker can forge tokens
for the entire platform.

**RS256 or ES256 (asymmetric)** would let other services verify tokens with only the public
key, keeping the signing key confined to the identity service.

```java
// infrastructure/security/NimbusJwtTokenIssuer.java
final JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
```

#### Key strength — no startup enforcement ⚠️

The key is accepted as any arbitrary string with no minimum-length validation. NIST SP 800-107
requires ≥ 256 bits (32 bytes) for HS256. A misconfigured `JWT_SECRET=mysecret` (7 bytes) is
silently accepted at startup.

```java
// config/SecurityConfig.java
private SecretKey secretKey(final String jwtSecret) {
    return new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
}
```

**Recommendation:** Add an `@PostConstruct` assertion in `NimbusJwtTokenIssuer` that throws a
hard startup failure if the key is shorter than 32 bytes.

#### Expiration — acceptable ✅

Default 3600 s (1 hour), configurable via `${JWT_EXPIRATION:3600}`. Reasonable.

#### Missing `iss` / `aud` claims ⚠️

No `issuer()` and no `audience()` are set in the claims. Another service configured with the
same HS256 secret would accept these tokens unconditionally. Always set
`.issuer("identity-service")` and `.audience(List.of("bookhub-api"))` and validate them in
the resource server configuration.

#### PII in payload ⚠️

`email` is embedded as a plain JWT claim. JWTs are only Base64-encoded (not encrypted), so
anyone holding the token can read the user's email. Consider omitting `email` from the access
token if downstream services do not strictly need it there — it is already available via
`/api/v1/users/me`.

---

### 2. Refresh Tokens — `RefreshTokenRepository`

**Files:** `domain/auth/RefreshToken.java`, `application/auth/RefreshSessionService.java`,
`infrastructure/persistence/RefreshTokenRepositoryAdapter.java`

#### Single-use with rotation ✅

Every call to `/refresh` atomically revokes the old token and issues a new one:

```java
// application/auth/RefreshSessionService.java
existingToken.revoke(now);
refreshTokenRepository.save(existingToken);

final RefreshToken newToken = RefreshToken.issue(newTokenValue, user, now.plusSeconds(...));
refreshTokenRepository.save(newToken);
```

#### No refresh-token family / reuse detection ⚠️

If a token is stolen and the attacker uses it *after* the legitimate user has already rotated
it, the stolen token is simply expired or revoked — no alarm is raised, and the attacker's new
session chain continues unchallenged.

[RFC 9700 §4.14](https://datatracker.ietf.org/doc/html/rfc9700) recommends that if a revoked
token is presented again, the **entire token family** for that user should be invalidated. This
requires storing a `family_id` column and implementing `revokeAllByFamily()`.

#### Active-token lookup ✅

The repository query checks `revoked = FALSE` **and** `expires_at > now`, and the DB index
`idx_refresh_tokens_revoked_expires_at` covers it correctly.

---

### 3. Logout — `LogoutUserService`

**File:** `application/auth/LogoutUserService.java`

#### Single-session only ⚠️

Only the one refresh token from the cookie is revoked. A user logged in on three devices will
still have two active sessions after logout. There is no `revokeAllByUserId()` in
`RefreshTokenRepository`. This should be offered as an explicit "log out everywhere" operation.

#### Access token remains valid for up to 1 hour ⚠️

The JWT is stateless and will continue to be accepted by every service until it expires.
There is no token blocklist. For sensitive actions (account compromise, explicit logout) this
window is too wide.

**Mitigations:**
- Shorten JWT TTL to 15 minutes, or
- Introduce a per-token revocation cache: `SET jti 1 EX 3600` in Redis.

#### Silent no-op on invalid cookie ⚠️

If the cookie value is blank or not a valid UUID, `logout()` simply returns without revoking
anything on the server side. The cookie is cleared client-side, but the server did nothing.

---

### 4. Password Reset Token — `PasswordResetToken`

**Files:** `domain/auth/PasswordResetToken.java`, `application/auth/ResetPasswordService.java`,
`application/auth/ForgotPasswordService.java`, `resources/application.yml`

| Criterion | Result |
|---|---|
| Short expiration | ✅ 900 s (15 min) default |
| Deleted after first use | ✅ `delete()` + `deleteByUserId()` in `ResetPasswordService` |
| One-per-user enforced | ✅ `deleteByUserId()` before issuing in `ForgotPasswordService` |
| Token entropy | ✅ `UUID.randomUUID()` = 122 bits |

#### Token stored as plaintext in the database ⚠️ HIGH

The raw reset token (a UUID string) is persisted as-is in the `password_reset_tokens` table.
If the table is read-accessible to an attacker (SQL injection, DB dump, DBA over-privilege),
they can extract any unexpired token and reset the victim's password within the 15-minute
window.

**Recommendation:** Hash the token before storing it. SHA-256 is sufficient since the value
already has high entropy. Send the plaintext once in the email link, store only the hash, and
verify on lookup.

```java
// Example: store SHA-256 of the token
String tokenHash = DigestUtils.sha256Hex(rawToken);
passwordResetTokenRepository.save(PasswordResetToken.issue(tokenHash, userId, expiresAt));
```

---

### 5. Forgot Password — `ForgotPasswordService`

**Files:** `application/auth/ForgotPasswordService.java`, `web/auth/AuthController.java`

#### HTTP response is identical ✅

`200 OK` with no body is returned regardless of whether the email is registered. This
correctly prevents user enumeration attacks.

#### Timing oracle ⚠️

When the email is **not found**, the method returns after a single fast DB read. When it **is
found**, the method executes: one DB read + one delete + one insert + one SMTP call. The SMTP
call alone can add 50–500 ms. An attacker measuring response times can still enumerate
registered emails.

**Mitigation:** Always dispatch the email asynchronously (e.g., via `@Async` +
`ApplicationEventPublisher`) so the HTTP response is returned before the email is sent.

#### Mail failure swallowed silently ⚠️

```java
// application/auth/ForgotPasswordService.java
} catch (RuntimeException exception) {
    LOGGER.warn("Password reset mail delivery failed for {}", normalizedEmail, exception);
}
```

The token is saved to the DB, mail delivery fails, the user receives `200 OK` but never gets
the email. The token silently expires after 15 minutes with the user having no feedback. A
retry mechanism or a distinct `503` response would improve this.

---

### 6. Reset Password — `ResetPasswordService`

**Files:** `web/auth/ResetPasswordRequest.java`, `web/auth/RegisterRequest.java`

#### No complexity validation ⚠️

Only **length** (8–72 chars) is enforced. The upper bound of 72 is correct (BCrypt silently
truncates beyond 72 bytes), but passwords like `12345678`, `aaaaaaaa`, or `password` are
accepted.

```java
// web/auth/ResetPasswordRequest.java
@NotBlank(message = "newPassword is required")
@Size(min = 8, max = 72, message = "newPassword length must be between 8 and 72")
String newPassword
```

The same pattern applies to `RegisterRequest.password`.

**Recommendation:** Add a `@Pattern` constraint or a custom `@ValidPassword` annotation
enforcing at least one uppercase letter, one digit, and one special character — or adopt
NIST SP 800-63B and check against a breached-password list (e.g., `zxcvbn` score ≥ 2).

---

### 7. Security Config — `SecurityConfig`

**File:** `config/SecurityConfig.java`

#### Open endpoints — minimal and correct ✅

```java
.requestMatchers(
    "/api/v1/auth/register",
    "/api/v1/auth/login",
    "/api/v1/auth/refresh",
    "/api/v1/auth/logout",
    "/api/v1/auth/forgot-password",
    "/api/v1/auth/reset-password")
.permitAll()
.anyRequest().authenticated()
```

The permit list is minimal and correct. Everything else requires authentication.

#### CSRF — acceptable ✅

CSRF is disabled. The `/refresh` and `/logout` endpoints rely on an `HttpOnly` cookie, but
`SameSite=Strict` effectively prevents cross-site cookie submission in all modern browsers.
Acceptable.

#### CORS — not configured ⚠️

`http.cors()` is never called and no `CorsConfigurationSource` bean is defined. Spring
Security 6 rejects pre-flight requests by default. If CORS is deliberately handled upstream
in the API gateway, this must be **explicitly documented**. If not, a
`CorsConfigurationSource` bean must be added.

#### Actuator not in `permitAll()` ⚠️

`anyRequest().authenticated()` covers `/actuator/**` too. With liveness/readiness probes
enabled (`management.endpoint.health.probes.enabled: true`), Kubernetes health-check calls
to `/actuator/health` will receive `401 Unauthorized` and the pod will be marked unhealthy
in a loop.

**Fix:** Add `/actuator/health/**` and `/actuator/info` to the `permitAll()` matcher.

---

### 8. Global Exception Handler — `GlobalExceptionHandler`

**File:** `web/error/GlobalExceptionHandler.java`

#### No stack traces or internal info ✅

All domain exceptions are mapped to structured `ErrorResponse` bodies. The
`DataIntegrityViolationException` handler deliberately uses a hardcoded generic message
instead of `exception.getMessage()`, which would leak SQL constraint names. The
`InvalidCredentialsException` message is `"Invalid email or password"` — does not reveal
whether the email exists.

#### No catch-all handler ⚠️

There is no `@ExceptionHandler(Exception.class)` fallback. Unhandled exceptions
(e.g., `DataAccessException`, `HttpMessageNotReadableException` for malformed JSON) fall
through to Spring Boot's default `BasicErrorController`. While stack traces are not exposed
by default, the response shape will differ from `ErrorResponse`, breaking client consistency.

**Fix:** Add a generic 500 handler.

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleUnexpected(final Exception ex,
        final HttpServletRequest request) {
    LOGGER.error("Unexpected error at {}", request.getRequestURI(), ex);
    return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
            "INTERNAL_ERROR", "An unexpected error occurred.", request);
}
```

---

### 9. Rate Limiting

**Files:** `pom.xml` (no rate-limiting dependency present), all auth controllers.

#### Completely absent ❌

There is no rate limiting on any endpoint. No Bucket4j, Resilience4j, or Spring Security
throttling dependency is present.

**Concrete attack surfaces:**

| Endpoint | Attack |
|---|---|
| `/api/v1/auth/login` | Unlimited credential-stuffing / brute-force |
| `/api/v1/auth/register` | Unlimited account creation / spam |
| `/api/v1/auth/forgot-password` | Unlimited email flooding for any target address |

**Recommendation:** Add [Bucket4j](https://github.com/bucket4j/bucket4j) with a
`HandlerInterceptor` or `OncePerRequestFilter`:

- `/login`: max 5 attempts per IP per minute, 15-minute lockout after 10 failures.
- `/register`: max 3 accounts per IP per hour.
- `/forgot-password`: max 3 requests per email per hour.

If rate limiting is handled at the API gateway layer (nginx, Traefik, Kong), document it
explicitly with a comment in `SecurityConfig`.

---

### 10. Password in Infrastructure — `UserJpaRepository`

**Files:** `infrastructure/persistence/UserJpaRepository.java`,
`web/user/UserProfileResponse.java`, `web/auth/RegisterResponse.java`,
`web/auth/LoginResponse.java`

#### Password never in response DTOs ✅

None of the outbound records (`UserProfileResponse`, `RegisterResponse`, `LoginResponse`,
`LoginUserResponse`) contain a `passwordHash` field.

#### Password not in logs ✅

`show-sql: true` is only active in the `local` profile. Hibernate does not log bound
parameter values under standard `show-sql` configuration. Password hash will not appear in
logs.

---

### 11. Register — `RegisterUserService`

**Files:** `application/auth/RegisterUserService.java`, `config/SecurityBeansConfig.java`

#### BCrypt with auto-salt ✅

```java
// config/SecurityBeansConfig.java
@Bean
public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}
```

`PasswordEncoderFactories.createDelegatingPasswordEncoder()` defaults to BCrypt with
**strength 10** (≈ 100 ms per encode). BCrypt generates a 128-bit random salt automatically
per invocation.

#### 72-byte truncation guard ✅

`@Size(max = 72)` on both `RegisterRequest.password` and `ResetPasswordRequest.newPassword`
ensures the server rejects longer inputs rather than BCrypt silently weakening them.

#### Ready for future migration to Argon2 ✅

The `{bcrypt}` prefix in the delegating encoder allows transparent re-encoding on next login
without invalidating existing hashes.

---

### 12. Domain Validation — `User` Aggregate

**File:** `domain/user/User.java`

#### No domain invariants ⚠️

```java
// domain/user/User.java
public static User create(
        final String username,
        final String email,
        final String passwordHash,
        final String displayName,
        final UserRole role) {
    return new User(null, username, email, passwordHash, displayName, role, null, null);
}
```

The factory method accepts any string — `null`, empty, malformed email, blank display name.
All real validation lives exclusively in the web layer DTOs. A future internal service call,
migration script, or admin tool that bypasses the controller can create `User` objects in an
invalid state.

Similarly, `User.updatePassword()` accepts any string, including unencoded plaintext, without
complaint.

**Recommendation:** Add guard clauses inside `User.create()` and the private constructor to
throw a domain-specific `InvalidUserException` for null/blank username, invalid email format,
null role, etc. The aggregate should be self-protecting regardless of caller.

---

### 13. Immutability — `RefreshToken` & `PasswordResetToken`

#### `RefreshToken` — controlled mutability ✅

Mutation surface is minimal (only `revoke()`), guarded against double-revocation. No public
setters. The controlled mutability is appropriate and intentional.

#### `PasswordResetToken` — effectively immutable ✅

No mutation methods, no public setters. After `@PrePersist` assigns `id` and `createdAt`,
the object is safely immutable. `equals()` correctly guards against null `id` before first
persist.

---

## Catalog Service Review

> File references are relative to
> `services/catalog-service/src/main/java/com/bookhub/catalog/`
> unless noted otherwise.

---

### 1. External Provider Resilience — `OpenLibraryClient`

**Files:** `infrastructure/provider/openlibrary/OpenLibraryClient.java`,
`config/CatalogSearchConfig.java`, `infrastructure/provider/openlibrary/OpenLibraryProperties.java`,
`resources/application.yml`

#### Socket-level timeouts configured ✅

Both `connectTimeout` and `readTimeout` are set on `SimpleClientHttpRequestFactory`, with a
2000 ms default configurable via `${CATALOG_OPENLIBRARY_TIMEOUT_MS:2000}`.

#### Async timeout with fallback metric ✅

`searchAsync()` applies an additional `.orTimeout()` layer and records a Micrometer counter
on fallback (`catalog.provider.openlibrary.search.fallbacks`).

#### No circuit breaker ❌

There is no Resilience4j or Spring Cloud Circuit Breaker in `pom.xml`. When OpenLibrary is
consistently unreachable, **every single search request** burns a virtual thread for up to
2 seconds before timing out. Under load this creates a latency spike even though the overall
fallback eventually returns local results. A half-open circuit state would stop even
attempting calls after N consecutive failures.

#### No retry ❌

Transient 5xx errors or brief network blips from OpenLibrary are never retried. The
`onStatus` handler converts HTTP errors into `RestClientException`, which is then silently
swallowed. A single transient failure causes immediate degradation to local-only results.

#### Detail endpoint has no targeted resilience ❌

`fetchDetail()` throws `ExternalProviderException` on any failure. No retry, no fallback.
A user requesting an external book by `ext:ol:...` ID when OpenLibrary is down always gets
a 502.

#### Double-timeout redundancy ⚠️

`OpenLibraryClient.searchAsync()` applies `.orTimeout(timeoutMs)`, then `SearchBooksService`
wraps that future with another `.completeOnTimeout(timeoutMs, ...)` at the same value. The
second timeout can never fire before the first. This creates confusion without causing harm.

---

### 2. Graceful Degradation — `ExternalProviderException`

**Files:** `domain/ExternalProviderException.java`, `application/SearchBooksService.java`,
`application/GetBookDetailService.java`, `web/GlobalExceptionHandler.java`

#### Search degrades perfectly ✅

Three independent safety nets ensure a search never fails due to OpenLibrary being down:

1. `OpenLibraryClient.search()` catches `RestClientException` → returns `List.of()`
2. `searchAsync().exceptionally()` → returns `List.of()` with metric and WARN log
3. `SearchBooksService` adds a redundant `.completeOnTimeout(List.of(), ...)` and
   `.exceptionally(ignored -> List.of())`

#### Detail endpoint fails hard ❌

`GetBookDetailService.fetchAndPersist()` explicitly re-throws `ExternalProviderException`,
which `GlobalExceptionHandler` maps to **502 Bad Gateway**. If a user clicks a search result
whose `id` is `ext:ol:OL262758W`, the book is not yet in the local DB, and OpenLibrary is
down, they receive a 502 with no partial degradation available.

---

### 3. Merge Logic — `SearchResultMerger`

**File:** `application/support/SearchResultMerger.java`

#### Local-first precedence correctly implemented ✅

A `LinkedHashMap` preserves insertion order. Local items are inserted first; external items
use `putIfAbsent()`, so a local result always wins over an external duplicate with the same
key.

#### Title-based deduplication is too aggressive ⚠️

When both `sourceReference` and `isbn13` are absent, the merger falls back to
`"title:" + title.toLowerCase()`. Many distinct books share titles ("It", "Dune" paperback
vs. hardback, "The Thing", etc.). Any two items with the same lowercase title — regardless
of author, edition, or publisher — will be collapsed into one, silently dropping one of them.

#### `"unknown"` key collision ⚠️

If an OpenLibrary document has a null `key`, null `isbn`, and null `title`, it hashes to the
single key `"unknown"`. Multiple such documents would all collide, and only the first would
survive the merge. Degenerate OL responses do occur in practice.

---

### 4. Normalization Edge Cases — `BookNormalization`

**Files:** `application/support/BookNormalization.java`,
`infrastructure/provider/openlibrary/OpenLibraryClient.java`,
`application/GetBookDetailService.java`

#### Null title flows to a `NOT NULL` column ❌

If OpenLibrary returns a work with `"title": null` (valid JSON, occurs for orphaned work
records), the null flows unchecked into the domain object and then into `entity.setTitle(null)`.
The column is `NOT NULL`, so a `DataIntegrityViolationException` is thrown.

`GetBookDetailService.fetchAndPersist()` catches `DataIntegrityViolationException` assuming a
concurrent duplicate-insert race condition, then calls `findBySourceReference()`, which returns
`Optional.empty()` (nothing was persisted), and throws
`BookNotFoundException("Book not found after concurrent import")` — a completely misleading
error message for a null-title condition.

**Fix:** Add a null guard before building the `Book` domain object from the OL response:

```java
final String title = response.title() != null ? response.title() : "Unknown Title";
```

#### ISBN-13 validation is incomplete ⚠️

`normalizeIsbn13` strips separators but does not validate that the result:
- is exactly 13 characters,
- consists only of digits, or
- passes the ISO 2108 check digit.

`"978-X-261-INVALID-4"` would normalize to `"978X261INVALID4"` and be stored without error.

#### No character-class filtering ⚠️

`normalizeIsbn13` does not filter non-digit, non-separator characters. A string like
`"978 0 X13 4"` normalizes to `"9780X134"` and passes through with no validation.

---

### 5. Search Performance — `CatalogSearchConfig` / `SearchBooksService`

**Files:** `config/CatalogSearchConfig.java`, `application/SearchBooksService.java`

#### Local and external searches run in parallel ✅

Both the local DB query and the OL HTTP call are submitted as `CompletableFuture` to the
same executor *before* either is joined. The executor uses Java 21 virtual threads
(`Executors.newVirtualThreadPerTaskExecutor()`), and `open-in-view: false` is set correctly.

#### `limit` is validated but never propagated — functional bug ❌

`BookController.search()` declares and validates a `limit` parameter (`@Max(100)`) but calls
`searchBooksService.search(query)` without passing it:

```java
// web/BookController.java
return searchBooksService.search(query)   // ← limit silently ignored
        .stream()
        .skip(offset)
        .limit(limit)
        ...
```

`SearchBooksService` hardcodes `DEFAULT_LIMIT = 20` for both local and external queries. The
maximum possible merged result set is ~40 items regardless of what the client requested. A
client requesting `limit=80` will silently receive at most ~40 results with no indication of
the effective cap.

---

### 6. Caching

**Files:** `pom.xml`, `application/SearchBooksService.java`, `application/GetBookDetailService.java`

#### No cache for search queries ❌

There is no `spring-boot-starter-cache`, Caffeine, or Redis dependency. No `@Cacheable`
annotation exists anywhere in the service. Every call to `GET /api/v1/books?q=hobbit` issues
a DB full-scan query and an HTTP call to OpenLibrary. Repeated high-frequency queries (same
top-10 search terms) hit both systems on every request.

#### Implicit detail "cache" via DB persistence ✅

`GetBookDetailService.getOrImportExternal()` checks `findBySourceReference()` before calling
OL. Once a book is fetched and persisted, subsequent detail requests are served from the local
DB with no external call.

#### No TTL or refresh mechanism for imported data ⚠️

Imported book records are never updated from OpenLibrary. If OL corrects an author name,
updates a cover image, or adds an ISBN, the local copy remains stale indefinitely. There is
no scheduled re-import or TTL.

---

### 7. Input Validation — `BookController`

**File:** `web/BookController.java`

#### Query params `q`, `limit`, `offset` are validated ✅

```java
@RequestParam("q") @NotBlank @Size(min = 2, max = 200) final String query,
@RequestParam(value = "limit", defaultValue = "20") @Min(1) @Max(MAX_LIMIT) final int limit,
@RequestParam(value = "offset", defaultValue = "0") @Min(0) final int offset
```

`@Validated` on the class activates Bean Validation for method parameters.
`ConstraintViolationException` is caught by `GlobalExceptionHandler` and returns a
structured 400.

#### `limit` is validated but functionally ignored ❌

See [Section 5](#5-search-performance--catalogsearchconfig--searchbooksservice).

#### No `@Max` on `offset` ⚠️

`offset=2147483647` is accepted. A reasonable upper bound (e.g., `@Max(10_000)`) would catch
obvious misuse.

#### `id` path variable has no web-layer constraint ⚠️

`getDetail(@PathVariable final String id)` has no annotation. Format validation only happens
inside `BookIdentifier.parse()` in the application layer. Functional, but the web layer
should sanitize before dispatching.

---

### 8. ID Validation — `InvalidBookIdException` / `BookIdentifier`

**Files:** `domain/BookIdentifier.java`, `domain/InvalidBookIdException.java`,
`application/GetBookDetailService.java`, `web/GlobalExceptionHandler.java`

#### Validation is thorough and happens before any I/O ✅

`BookIdentifier.parse()` validates all three invalid formats — blank, empty external ref, bad
UUID — before any DB or OL call. `GlobalExceptionHandler` maps `InvalidBookIdException` to a
structured 400.

#### `ext:ol:` reference content not further validated ⚠️

`ext:ol:!@#$%^&*` passes the parse check and proceeds to OpenLibrary. A simple alphanumeric
format guard would eliminate unnecessary outbound requests with obviously invalid IDs.

---

### 9. Entity Mapper — `BookEntityMapper`

**Files:** `infrastructure/persistence/BookEntityMapper.java`,
`infrastructure/persistence/BookEntity.java`, `domain/Book.java`

#### No information loss in the round-trip ✅

All seven domain fields are mapped in both directions. `toEntity()` correctly delegates ISBN
and source reference normalization to `BookNormalization`. `BookEntityMapperTest` verifies
normalization fires on the way to the entity.

#### No null guard on `title` in `toEntity()` ⚠️

`toEntity()` calls `entity.setTitle(book.getTitle())` without a null check. If the title is
null (see [Section 4](#4-normalization-edge-cases--booknormalization)), the resulting
`DataIntegrityViolationException` is misleading. The guard should exist in the normalization
step, before the mapper is even called.

#### Secondary convenience constructor in `SearchBooksService` ⚠️

A secondary constructor creates `new SearchResultMerger()` directly, bypassing Spring's
container. This works today because `SearchResultMerger` has no injected dependencies. The
moment a collaborator is added to `SearchResultMerger`, every caller using this constructor
will silently get a broken instance. This constructor should be removed.

#### Audit fields invisible to the domain ⚠️

`createdAt` / `updatedAt` are managed by `@PrePersist` / `@PreUpdate` but have no
representation in `Book`. If audit timestamps are ever needed in API responses or business
logic, the domain model cannot provide them without a schema change.

---

### 10. Web Mapper / DTOs — `BookWebMapper`

**Files:** `web/BookWebMapper.java`, `web/BookDetailResponse.java`,
`web/BookSearchResponse.java`

#### `BookSearchResponse` — clean encapsulation ✅

Only `id`, `title`, `authorName`, and `coverUrl` are exposed. `isbn13`, `sourceReference`,
and `publishedYear` are correctly withheld from list results.

#### `sourceReference` leaks an implementation detail in `BookDetailResponse` ⚠️

```java
// web/BookDetailResponse.java
public record BookDetailResponse(
        String id,
        String title,
        String authorName,
        String isbn13,
        String sourceReference,   // ← raw OL work key, e.g. "OL262758W"
        String coverUrl,
        Integer publishedYear) {}
```

`sourceReference` is the raw OpenLibrary work key. Exposing it couples the public API
contract to the internal provider implementation. If the catalog ever ingests books from a
second provider (e.g., Google Books), this field becomes ambiguous. The `id` field already
carries this routing information as `ext:ol:OL262758W` — `sourceReference` is redundant and
leaky.

#### Null-unsafe MapStruct expression ⚠️

```java
// web/BookWebMapper.java
@Mapping(target = "id", expression = "java(book.getId().toString())")
BookDetailResponse toDetailResponse(Book book);
```

If `book.getId()` is null, this expression throws a `NullPointerException` at runtime. In
the current flow this is unlikely, but the contract is unguarded.

---

### 11. DB Schema — `V1__init_catalog_schema.sql`

**Files:** `resources/db/migration/V1__init_catalog_schema.sql`,
`infrastructure/persistence/JpaBookRepository.java`

#### Indexes exist for the right columns ✅

```sql
CREATE INDEX idx_books_title_lower      ON books ((LOWER(title)));
CREATE INDEX idx_books_author_name_lower ON books ((LOWER(author_name)));
CREATE INDEX idx_books_isbn13           ON books (isbn13);
```

Plus the implicit unique index from `uk_books_source_reference`. Column types and sizes are
appropriate.

#### The indexes are bypassed by the actual search query ❌

The JPQL query uses **`LIKE '%query%'`** — a substring search with a leading wildcard:

```java
// infrastructure/persistence/JpaBookRepository.java
WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
   OR LOWER(COALESCE(b.authorName, '')) LIKE LOWER(CONCAT('%', :query, '%'))
   OR COALESCE(b.isbn13, '') LIKE CONCAT('%', :query, '%')
```

PostgreSQL's B-Tree indexes (including functional indexes) **can only support prefix matches**
(`LIKE 'query%'`). A leading `%` forces a **sequential full table scan** regardless of the
index. The three indexes defined in the migration are effectively unused for searches.

**At scale:** imperceptible at 1 000 rows; latency problem at 100 000; showstopper at
1 000 000.

**Correct solution:** PostgreSQL's `pg_trgm` extension with GIN indexes:

```sql
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_books_title_trgm  ON books USING GIN (LOWER(title) gin_trgm_ops);
CREATE INDEX idx_books_author_trgm ON books USING GIN (LOWER(author_name) gin_trgm_ops);
```

#### No check constraint on `isbn13` ⚠️

`isbn13 VARCHAR(13)` accepts any string up to 13 characters. A constraint would act as a
last-resort data quality gate:

```sql
ALTER TABLE books ADD CONSTRAINT chk_books_isbn13_digits
    CHECK (isbn13 ~ '^\d{13}$' OR isbn13 IS NULL);
```

#### `author_name` has no DB-level default ⚠️

The application normalizes a missing author to `"Unknown"`, but the column has no
`DEFAULT 'Unknown'`. A row inserted by a migration script or DBA tool bypassing the
application would store a NULL author.

---

### Bonus: Silent Test Omission

**File:** `src/test/java/com/bookhub/catalog/application/SearchBooksServiceTest.java`

The most important test in `SearchBooksServiceTest` — the one verifying that local results
take precedence over external duplicates — **is missing `@Test`**. JUnit 5 never executes it.
The core merge-and-deduplicate behavior has **zero actual test coverage** at the service level,
despite the test body being fully written.

```java
// SearchBooksServiceTest.java — missing @Test annotation!
void shouldMergeLocalAndExternalResultsConcurrently() {
    // fully written test body that never runs
}
```

---

## Executive Summary by Priority

### ✅ Resolved Since This Review

| # | Service | Problem | Resolution status |
|---|---|---|---|
| 1 | Identity | No rate limiting on `/login`, `/register`, `/forgot-password` | Resolved via auth rate limiting interceptor and 429 handling |
| 2 | Identity | Password reset token stored in plaintext in DB | Resolved via hashed token persistence + migration |
| 3 | Catalog | Leading-wildcard `LIKE '%q%'` bypasses all search indexes | Resolved via trigram-backed search strategy + migration |
| 4 | Catalog | Null title from OL flows to `NOT NULL` column producing a misleading error | Resolved via explicit invalid-provider-payload handling |
| 5 | Catalog | `limit` is validated but never propagated to the service or query | Resolved via end-to-end `limit/offset` propagation |
| 15 | Catalog | `shouldMergeLocalAndExternalResultsConcurrently` test missing `@Test` | Resolved; test is now active |

### ⚠️ Next Sprint — Recommended Focus

| # | Service | Problem | File |
|---|---|---|---|
| 6 | Identity | No `iss` / `aud` claims in JWT | `NimbusJwtTokenIssuer.java`, `SecurityConfig.java` |
| 7 | Identity | No minimum key length enforced at startup | `NimbusJwtTokenIssuer.java`, `SecurityConfig.java` |
| 12 | Identity | `/actuator/health` requires auth → Kubernetes health probes receive 401 | `SecurityConfig.java` |
| 18 | Identity | No catch-all `Exception` handler in `GlobalExceptionHandler` | `web/error/GlobalExceptionHandler.java` |
| 13 | Catalog | No circuit breaker — sustained OL downtime causes latency spike under load | `pom.xml`, `OpenLibraryClient.java` |
| 14 | Catalog | Detail endpoint fails with 502 if OL is down and book is not in local DB | `GetBookDetailService.java` |

### 🔵 Backlog — Important but Not Immediate

| # | Service | Problem | File |
|---|---|---|---|
| 8 | Identity | `logout()` only invalidates current session — no "log out everywhere" | `LogoutUserService.java`, `RefreshTokenRepository.java` |
| 9 | Identity | JWT access token remains valid up to 1h after logout | `SecurityConfig.java` |
| 10 | Identity | Consider migrating HS256 → RS256/ES256 for multi-service architecture | `NimbusJwtTokenIssuer.java` |
| 11 | Identity | CORS not configured | `SecurityConfig.java` |
| 17 | Identity | `User.create()` has no domain invariants — aggregate is not self-protecting | `domain/user/User.java` |
| 19 | Identity | Timing oracle in `/forgot-password` — dispatch email asynchronously | `ForgotPasswordService.java` |
| 20 | Catalog | `sourceReference` exposed in `BookDetailResponse` — couples contract to OL | `BookDetailResponse.java`, `BookWebMapper.java` |
| 21 | Catalog | No cache for repeated search queries | `SearchBooksService.java` |
| 22 | Catalog | ISBN normalization strips separators but does not validate digit-only content | `BookNormalization.java` |
| 23 | Catalog | Title-based deduplication in `SearchResultMerger` is too broad — collapses distinct books | `SearchResultMerger.java` |
| 24 | Catalog | No retry on transient OL errors | `OpenLibraryClient.java` |
| 26 | Catalog | No `CHECK` constraint on `isbn13` column | `V1__init_catalog_schema.sql` |

### ℹ️ Lower Priority / Re-evaluate Before Acting

| # | Service | Problem | Note |
|---|---|---|---|
| 16 | Identity | Only length validated for passwords, no complexity rules | Re-evaluate against current NIST guidance before adding regex-style complexity rules |
| 25 | Catalog | No `DEFAULT 'Unknown'` on `author_name` column | Application path already normalizes most cases; DB default is defense-in-depth only |
