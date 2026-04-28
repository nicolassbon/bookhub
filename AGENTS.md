# AGENTS.md - BookHub Project Context & Coding Guidelines

## 1. Project Overview

**BookHub** is a social reading platform inspired by Goodreads.

- **Architecture:** Microservices-first backend with clear bounded contexts.
- **Runtime apps:** `api-gateway`, `identity-service`, `catalog-service`, `library-service`.
- **Language:** Java 21.
- **Framework:** Spring Boot 3.x.
- **Build Tool:** Maven.
- **Database:** PostgreSQL, database per service.
- **Security:** Spring Security 6, stateless authentication.
- **Migrations:** Flyway.
- **Documentation:** OpenAPI and English-only project docs.
- **Local platform:** Docker Compose + observability demo.

---

## 2. Architecture and Repository Structure

BookHub uses a **monorepo** with independent services.

```text
bookhub/
├── docs/
├── infrastructure/
│   ├── docker/
│   └── observability/
├── services/
│   ├── api-gateway/
│   ├── identity-service/
│   ├── catalog-service/
│   └── library-service/
└── frontend/
    └── web-app/
```

### 2.1 Service internal structure

Each backend service should follow this package layout:

```text
src/main/java/com/bookhub/{service}/
├── application/
├── domain/
├── infrastructure/
├── web/
└── config/
```

### 2.2 Layer responsibilities

- **domain/**: business concepts, aggregates, value objects, domain exceptions, repository ports.
- **application/**: use-case orchestration and transactional workflows.
- **infrastructure/**: JPA adapters, HTTP clients, mail adapters, persistence implementations.
- **web/**: controllers, request/response DTOs, API mappers, HTTP concerns.
- **config/**: Spring configuration only.

### 2.3 Architecture rules

- Boundaries are business-first, not framework-first.
- Each service owns its data and behavior.
- Cross-service communication must happen through explicit APIs only.
- Do **NOT** create a `shared`, `common`, or `core-lib` module by default.
- Do **NOT** access another service database directly.

---

## 3. Coding Standards

### 3.1 General

- **Language:** English for code, docs, comments, commits, and technical artifacts.
- **Injection:** Use constructor injection only. Field injection is forbidden.
- **Clean code:** Methods should be short and intentional. Names must reveal purpose.
- **Immutability:** Prefer immutable objects by default.
- **No unnecessary cleverness:** Clarity beats abstraction.

### 3.2 Object creation

- Prefer **Lombok builders** for rich object construction to improve readability.
- Avoid direct constructor calls with many parameters in application/web code.
- Use `record` for simple request/response DTOs and result models.
- Acceptable exceptions to the builder preference:
  - framework-required constructors
  - compact and obvious value objects
  - JPA protected no-arg constructors
  - tests where direct construction is clearer

### 3.3 Lombok usage

- Prefer `@Builder`, `@Getter`, `@RequiredArgsConstructor` where they improve readability.
- Do **NOT** use `@Data` on entities.
- Be explicit when Lombok would hide important intent.

### 3.4 Java conventions

- Use PascalCase for classes and records.
- Use camelCase for fields and methods.
- Use UPPER_SNAKE_CASE for constants.
- Prefer domain-specific exceptions over generic `RuntimeException`.
- Avoid raw types, long parameter lists, deep nesting, and silent catch blocks.

---

## 4. Persistence and Entities

- Use `@Entity` and explicit table names.
- Use `jakarta.persistence.*`, never `javax.*`.
- Implement `equals()` and `hashCode()` using only the identifier.
- Use `FetchType.LAZY` where relationships are introduced.
- Repositories should stay free of business logic.
- Use derived queries first; use JPQL/native queries only when justified.
- Use Flyway for schema evolution; never rely on ad-hoc schema drift.

---

## 5. API and Web Layer

- Controllers must remain thin.
- Never expose entities directly from controllers.
- Use request/response DTOs for every API boundary.
- Validate request payloads with Bean Validation.
- Return structured error responses through a global `@ControllerAdvice`.
- Keep endpoint naming consistent with the contracts in `docs/service-contracts-v1.md`.

### Error response baseline

Every service should converge on an error shape like:

```json
{
  "timestamp": "2026-04-09T18:00:00Z",
  "status": 400,
  "error": "Validation Error",
  "code": "VALIDATION_ERROR",
  "message": "displayName must not be blank",
  "path": "/api/v1/auth/register"
}
```

---

## 6. Application Service Naming

Avoid generic names that lose the use-case meaning.

### Preferred naming

- `RegisterUserService`
- `LoginUserService`
- `CreateReviewService`
- `UpdateReadingProgressService`

### Avoid when the class represents a single use case

- `UserService`
- `AuthService`
- `BookService`

### Rule

If a class orchestrates **one explicit use case**, name it after the use case.
If a class truly groups a cohesive set of related operations, a broader name can be acceptable.

The goal is to make the application layer read like the product behavior, not like a grab bag of utilities.

---

## 7. Testing Guidelines

- Follow **TDD** by default: red → green → refactor.
- Unit tests first for business rules.
- Use MockMvc for web layer tests.
- Use integration tests for critical flows.
- Prefer Testcontainers for realistic persistence-backed tests when the slice justifies it.
- Keep tests deterministic and readable.
- Test behavior, not implementation trivia.

### 7.1 Functional vs non-functional test ownership

- **Java tests are the functional source of truth.** Use Java tests to verify correctness, HTTP contracts, validation, error responses, business rules, persistence effects, and token lifecycle behavior.
- **WebMvc tests own boundary behavior.** Use `@WebMvcTest` suites for request validation, malformed input, structured error responses, and functional rate-limit assertions.
- **PostgreSQL-backed integration tests own stateful flows.** Use `PostgreSqlIntegrationTest`-based suites for register/login/refresh/logout/password-recovery flows, token rotation/revocation, and persistence-backed behavior.
- **k6 is non-functional only.** Use k6 for load, pressure, stress, concurrency, latency, and throughput characterization.
- **Do not use k6 as the functional source of truth.** Semantic assertions such as expected 400/401/429 responses, domain error codes, or business-flow correctness must live in Java tests.

---

## 8. Security Guidelines

- Stateless authentication only.
- Public endpoints must be explicitly declared.
- Deny by default for everything else.
- Hash passwords with BCrypt or Argon2 through `PasswordEncoder`.
- Never hardcode secrets in source or YAML.
- Use environment variables for credentials and sensitive configuration.
- Configure CORS centrally in security config.
- Enforce authorization on the server side, never in the frontend only.

---

## 9. Documentation and Decisions

- All project documentation must be written in English.
- Architectural decisions belong in `docs/adr/`.
- Service contracts, domain model, and bounded contexts in `docs/` are source-of-truth artifacts.
- If implementation diverges from the docs, update the docs deliberately.

---

## 10. SDD Usage Policy

BookHub uses **Spec-Driven Development (SDD)** as the default workflow for meaningful feature work.

### 10.1 When to use the full SDD flow

Use the full SDD flow for features that introduce meaningful business behavior, architectural impact, or non-trivial implementation scope.

Examples:

- user registration
- login
- JWT and refresh-token support
- password recovery
- new business workflows
- cross-service integrations
- significant domain logic changes
- new public API capabilities

Expected SDD coverage for these features:

- proposal
- spec
- design
- tasks
- apply
- verify
- archive

The exact depth may vary, but the feature must be documented and traceable through SDD artifacts.

### 10.2 When to use only part of SDD

For small, low-risk, or clearly bounded changes, use only the SDD phase(s) that match the complexity of the work.

Examples:

- minor refactors
- mapper cleanup
- naming improvements
- focused bug fixes
- small configuration adjustments
- test-only improvements

Typical smaller paths may include:

- `apply` only
- `apply + verify`
- a lightweight `spec + apply` when clarification is still useful

### 10.3 Rule of proportionality

Use **full SDD for real feature work** and **proportional SDD for small changes**.

The goal is to avoid both:

- implementing important features without traceability
- creating unnecessary process overhead for small changes

---

## 11. Git Workflow

- Use Conventional Commits.
- Keep `legacy/spring-mvc` intact as the legacy reference branch.
- Microservices work belongs on `main`-based migration branches.
- Do not commit `.env`, local agent artifacts, or IDE metadata.

Examples:

- `feat(identity): add user login endpoint`
- `fix(library): prevent duplicate user-book entries`
- `docs(adr): document database-per-service decision`
- `chore(infra): add local compose observability stack`

---

## 12. Common Commands

- **Run tests:** `./mvnw test`
- **Run one service:** `./mvnw spring-boot:run`
- **Package without tests:** `./mvnw clean package -DskipTests`

Do not rely on manual DB changes. Prefer Flyway migrations.
