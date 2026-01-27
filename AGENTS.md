# AGENTS.md - Project Context & Coding Guidelines

## 1. Project Overview
**BookHub** is a Social Reading Network with a freemium model.
- **Architecture:** Monolithic REST API (Layered Architecture).
- **Language:** Java 17.
- **Framework:** Spring Boot 3.5.x.
- **Build Tool:** Maven.
- **Database:** PostgreSQL.
- **Security:** Spring Security 6 (Stateless JWT).
- **Documentation:** OpenAPI (Swagger).

---

## 2. Architecture & Package Structure
We follow a strict **Layered Architecture**. Dependencies flow in one direction:
`Controller` -> `Service` -> `Repository` -> `Database`

```
src/main/java/com/tallerwebi/bookhub/
├── config/           # Configuration classes (Security, Swagger, CORS)
├── controller/       # REST Endpoints (@RestController)
├── dto/              # Data Transfer Objects
│   ├── request/      # Input DTOs (@Valid)
│   └── response/     # Output DTOs
├── model/            # JPA Entities (@Entity)
├── repository/       # Data Access Layer (Spring Data JPA)
├── service/          # Business Logic interfaces
│   └── impl/         # Service implementations
├── exception/        # Global Error Handling
└── security/         # JWT, Auth filters, UserDetails
```

---

## 3. Coding Standards & Best Practices

### 3.1. General
- **Language:** English for ALL code (variables, methods, comments, commits).
- **Injection:** Use **Constructor Injection** exclusively. `@Autowired` on fields is FORBIDDEN.
- **Lombok:** Use heavily to reduce boilerplate (`@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`).
- **Clean Code:** Methods should be short and do one thing. Variable names must be descriptive.

### 3.2. JPA Entities (`model` package)
- Use `@Entity` and `@Table(name = "snake_case_plural")`.
- **Do NOT** use `@Data` on entities (performance/hashCode issues). Use `@Getter`, `@Setter`, `@ToString`.
- Use `Jakarta` imports (`jakarta.persistence.*`) NOT `javax`.
- Implement `equals()` and `hashCode()` using **only the ID**.
- Relationships:
  - Always use `FetchType.LAZY` for `@OneToMany` and `@ManyToMany`.
  - Use `Set<>` instead of `List<>` for collections to avoid "MultipleBagFetchException".

### 3.3. DTOs (`dto` package)
- **NEVER** return Entities from Controllers. Always map to DTOs.
- Use **MapStruct** for Entity <-> DTO conversion.
- Use `record` for simple DTOs (Java 16+).
- Separate Request and Response DTOs.
- Validation: Use annotations (`@NotBlank`, `@Email`, `@Size`) in Request DTOs.

### 3.4. Repositories (`repository` package)
- Extend `JpaRepository<Entity, Long>`.
- Use **Derived Query Methods** (e.g., `findByEmail`) for simple queries.
- Use `@Query("SELECT ...")` with JPQL for complex queries.
- **No business logic** inside repositories.

### 3.5. Services (`service` package)
- Annotate implementation classes with `@Service` and `@Transactional(readOnly = true)` at the class level.
- Annotate modifying methods (create, update, delete) with `@Transactional`.
- Business rules and validations go here.
- Throw custom exceptions (e.g., `ResourceNotFoundException`) instead of returning null.

### 3.6. Controllers (`controller` package)
- Annotate with `@RestController` and `@RequestMapping("/api/v1/resource")`.
- Return `ResponseEntity<DTO>`.
- Use `@Valid` for Request Body validation.
- Document endpoints with `@Operation` and `@ApiResponse` (Swagger).
- Keep controllers "thin": they only handle HTTP protocol (status codes, headers), then delegate to Service.

### 3.7. Exception Handling
- Use a global `@ControllerAdvice`.
- Return a standardized `ErrorResponse` DTO (timestamp, status, message, path).
- Map internal exceptions to HTTP Status codes (e.g., `ResourceNotFound` -> 404).

---

## 4. Testing Guidelines
- **Frameworks:** JUnit 5, Mockito.
- **Unit Tests:** Focus on Service layer. Mock repositories.
- **Integration Tests:** Focus on Controllers (`@WebMvcTest` or `@SpringBootTest`).
- **Naming:** `ClassNameTest` (e.g., `UserServiceTest`).
- Use `@DisplayName` to describe the test scenario.

## 5. Security Guidelines
- Stateless session management (`SessionCreationPolicy.STATELESS`).
- Passwords must be hashed using `BCrypt`.
- Public endpoints must be explicitly defined in `SecurityConfig`.
- CORS must be configured to allow frontend origins.

## 6. Git Workflow
- **Commits:** Conventional Commits standard.
  - `feat: add user registration`
  - `fix: resolve login bug`
  - `refactor: optimize database query`
  - `docs: update swagger documentation`
  - `chore: update dependencies`

## 7. Common Commands
- **Run App:** `./mvnw spring-boot:run`
- **Run Tests:** `./mvnw test`
- **Build:** `./mvnw clean package -DskipTests`
