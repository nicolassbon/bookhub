# PRD — BookHub V1 Modernized

## 1. Product vision

BookHub V1 aims to modernize the legacy social reading application into an API-first solution with a microservices-based backend, strong engineering practices, solid testing, and a strong foundation for future versions.

The project must serve at the same time as:

- a real Goodreads-inspired product
- a strong backend portfolio project
- a demonstration of architecture and system design judgment

## 2. Success definition

The project will be considered successful if it:

- covers the prioritized core features from the legacy system
- exposes a modern and maintainable architecture
- can evolve into new features without requiring a major rewrite
- demonstrates strong Java/Spring, testing, CI/CD, observability, and distributed design skills

## 3. Objectives

### Business objectives

- Modernize BookHub.
- Build a foundation for a real product.
- Increase portfolio value for job opportunities.

### Technical objectives

- Migrate from a monolithic Spring MVC application to a well-bounded 4-service runtime behind an API gateway (`api-gateway`, `identity-service`, `catalog-service`, `library-service`).
- Design consistent and well-documented APIs.
- Work with TDD, strong unit tests, and integration tests for important flows.
- Include Docker Compose, CI/CD, and observability demo support.

## 4. Target user

### Primary user

A Goodreads-style social reader who wants to:

- organize their library
- discover books
- write reviews
- track yearly reading progress

### User types

- Regular user
- Admin

## 5. V1 scope

### In scope

- Registration, login, and password recovery
- Basic user profile
- Book search and book detail
- Personal library
- Reading states: Want to Read, Reading, Read
- Reading progress by pages and percentage
- Basic shelf/library organization
- Yearly reading goal
- Create and view reviews
- Basic in-app notifications
- Initial admin endpoints
- API Gateway
- Database per microservice
- Docker Compose
- Architecture and API documentation
- Observability demo
- CI/CD

### Out of scope for V1

- Full social feed
- Advanced social comments and reactions
- Full friendship system
- Freemium model
- Payments
- Real-time notifications
- AI features
- A complete frontend as a release blocker

## 6. Prioritized features

### 6.1 Authentication and accounts

- User registration
- JWT-based login
- Refresh token support or an equivalent secure strategy
- Password recovery by email
- Roles: USER and ADMIN

### 6.2 Catalog

- Search books by title/author
- View book details
- Persist locally the books actually used by users

### 6.3 Personal library

- Add a book to the library
- Change reading state
- Update reading progress
- View user shelves
- View yearly goal and progress

### 6.4 Reviews

- Create own review
- Edit own review
- View book reviews
- Rating and text content

### 6.5 Notifications

- List user notifications
- Mark notifications as read
- Trigger basic internal business events

### 6.6 Admin

- View users
- View and moderate reviews
- Review imported/manual catalog entries
- Access initial metrics

## 7. Non-functional requirements

- Maintainable and scalable architecture
- API-first design
- JWT-based security
- TDD as the main working strategy
- Strong test coverage on core business logic
- Continuous integration
- Reproducible local containerized setup
- Minimum viable observability:
  - structured logs
  - metrics
  - basic tracing

## 8. Initial architecture proposal

## V1 microservices

### 1. identity-service

Responsibilities:

- authentication
- users
- roles
- password recovery

Owned data:

- users
- credentials
- recovery tokens / security tokens

### 2. catalog-service

Responsibilities:

- book search
- book details
- local persistence of the active catalog
- initial catalog administration

Owned data:

- books
- authors
- import metadata

### 3. library-service

Responsibilities:

- personal library
- reading states
- progress tracking
- yearly goals
- reviews
- in-app notifications
- basic functional metrics

Owned data:

- user_books
- reading_progress
- yearly_goals
- reviews
- notifications

## Cross-cutting components

- API Gateway
- Externalized configuration per environment
- OpenAPI per service
- Observability demo
- CI/CD

## 9. Proposed architecture decisions

### 9.1 Database per service

This is adopted from V1 to enforce clear service boundaries. In local development, it can be implemented with a single PostgreSQL container using separate databases or schemas.

### 9.2 Service-to-service communication

**Recommended decision for V1:** prioritize synchronous HTTP communication between services.

**Why:**

- reduces initial complexity
- makes a solid V1 achievable within 8 weeks
- leaves room for introducing events later

**Tradeoff:**

- less decoupling than an event-driven solution
- part of the distributed architecture evolution is postponed to a later iteration

### 9.3 Book catalog strategy

**Recommended decision:** use an external API to bootstrap search and persist locally the books that users actually interact with.

**Advantages:**

- speeds up delivery
- avoids manually loading a full catalog
- keeps control over relevant domain data

**Risk:**

- requires a normalization/import strategy

### 9.4 Frontend strategy

The system is designed backend-first. During implementation, validation will primarily happen through Swagger/Postman. Angular is intentionally decoupled from backend progress.

## 10. Recommended stack

- Java 21
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Docker + Docker Compose
- Spring Cloud Gateway
- OpenFeign or WebClient for synchronous communication
- Micrometer + Prometheus
- Grafana
- Loki or simple structured logging depending on time
- JUnit 5 + Mockito + Testcontainers for important integration tests
- GitHub Actions for CI/CD

## 11. Roadmap

### Phase 0 — Discovery and design

- Legacy feature inventory
- PRD
- bounded context definition
- initial API contracts

### Phase 1 — Platform foundation

- repository structure
- gateway
- environment-based configuration
- minimum observability
- initial CI/CD

### Phase 2 — identity-service

- authentication
- users
- password recovery
- roles

### Phase 3 — catalog-service

- external search integration
- local persistence
- book details
- catalog admin endpoints

### Phase 4 — library-service

- personal library
- progress tracking
- yearly goals
- reviews
- in-app notifications

### Phase 5 — Hardening

- critical integration tests
- final documentation
- demo dashboards
- deployment/delivery

## 12. Main risks

### Risk 1 — over-architecture

Trying to include too many additional services or too much infrastructure beyond the accepted V1 runtime shape.

**Mitigation:** keep V1 limited to the accepted runtime shape: `api-gateway` + `identity-service` + `catalog-service` + `library-service`.

### Risk 2 — poor understanding of the legacy behavior

Part of the original behavior is not fully remembered.

**Mitigation:** keep the legacy inventory alive and validate parity through concrete use cases.

### Risk 3 — operational complexity

Gateway, observability, and CI/CD can consume significant time.

**Mitigation:** implement a solid demo-level version, not a full enterprise-grade platform.

### Risk 4 — introducing async messaging too early

Adding a broker in V1 may compromise delivery.

**Mitigation:** leave async as a future extension unless a critical case proves otherwise.

## 13. Scope reduction priority if time gets tight

1. Admin UI
2. Advanced observability
3. Non-critical admin endpoints
4. Extra catalog refinements

Must not be cut:

- authentication
- personal library
- search
- reviews
- yearly goals
- microservice foundation

## 14. Testing strategy

- TDD as the implementation mode
- strong unit tests in domain and application layers
- integration tests for critical flows
- basic security tests
- contract testing is optional, not required for V1

## 15. Success metrics

- Accepted runtime shape running end-to-end: `api-gateway` + `identity-service` + `catalog-service` + `library-service`
- core business flows working end-to-end
- clear technical documentation
- reproducible local environment with Docker Compose
- CI/CD pipeline running tests
- visible observability demo

## 16. Tradeoffs and rejected decisions

### Rejected option: migrate to a Spring Boot monolith

Rejected for this iteration because the project goal is also to demonstrate system design and microservices architecture for portfolio purposes.

### Rejected option: split V1 into 5-6 microservices

Rejected because it increases fragmentation risk and reduces the probability of delivering a strong V1 within 8 weeks.

### Rejected option: introduce AI in V1

Explicitly rejected because it does not add value to the core product at this stage.

### Rejected option: event-driven architecture from day one

Postponed to avoid excessive complexity and prioritize a finished V1.

## 17. Next steps

1. Validate this PRD.
2. Design bounded contexts and per-service contracts.
3. Create a V1 feature parity checklist against the legacy version.
4. Break the work down into technical milestones.
