# Skill Registry

**Delegator use only.** Any agent that launches sub-agents reads this registry to resolve compact rules, then injects them directly into sub-agent prompts. Sub-agents do NOT read this registry or individual SKILL.md files.

See `_shared/skill-resolver.md` for the full resolution protocol.

## User Skills

| Trigger | Skill | Path |
|---------|-------|------|
| setup/API/framework questions where up-to-date docs matter | documentation-lookup | /home/nico/.config/opencode/skills/documentation-lookup/SKILL.md |
| Docker/Compose local orchestration, Dockerfile hardening | docker-patterns | /home/nico/.config/opencode/skills/docker-patterns/SKILL.md |
| Java 21 records/sealed types/virtual threads usage | java-21 | /home/nico/.config/opencode/skills/java-21/SKILL.md |
| TypeScript typing/interfaces/generics work | typescript | /home/nico/.config/opencode/skills/typescript/SKILL.md |
| Angular project structure and placement decisions | angular-architecture | /home/nico/.config/opencode/skills/angular/architecture/SKILL.md |
| Angular components/signals/inject/control-flow | angular-core | /home/nico/.config/opencode/skills/angular/core/SKILL.md |
| Angular forms and validation workflows | angular-forms | /home/nico/.config/opencode/skills/angular/forms/SKILL.md |
| Angular performance optimization | angular-performance | /home/nico/.config/opencode/skills/angular/performance/SKILL.md |
| security reviews, threat modeling, secure-by-design checks | security-review | /home/nico/.config/opencode/skills/security-review/SKILL.md |
| adversarial dual-review requests (“judgment day”, “doble review”) | judgment-day | /home/nico/.config/opencode/skills/judgment-day/SKILL.md |
| creating GitHub issues with issue-first workflow | issue-creation | /home/nico/.config/opencode/skills/issue-creation/SKILL.md |
| creating pull requests under issue-first workflow | branch-pr | /home/nico/.config/opencode/skills/branch-pr/SKILL.md |
| Go tests/Bubbletea testing patterns | go-testing | /home/nico/.config/opencode/skills/go-testing/SKILL.md |
| creating new AI agent skills | skill-creator | /home/nico/.config/opencode/skills/skill-creator/SKILL.md |
| Java coding standards for Spring Boot services | java-coding-standards | /home/nico/proyectos/backend/bookhub/.agents/skills/java-coding-standards/SKILL.md |
| Spring Boot backend architecture and API patterns | springboot-patterns | /home/nico/proyectos/backend/bookhub/.agents/skills/springboot-patterns/SKILL.md |
| Spring Security/authz/input/secrets best practices | springboot-security | /home/nico/proyectos/backend/bookhub/.agents/skills/springboot-security/SKILL.md |
| Spring Boot TDD workflow and testing stack | springboot-tdd | /home/nico/proyectos/backend/bookhub/.agents/skills/springboot-tdd/SKILL.md |
| Spring Boot verification loop pre-PR/release | springboot-verification | /home/nico/proyectos/backend/bookhub/.agents/skills/springboot-verification/SKILL.md |

## Compact Rules

Pre-digested rules per skill. Delegators copy matching blocks into sub-agent prompts as `## Project Standards (auto-resolved)`.

### documentation-lookup
- Use Context7 for library/framework/API answers instead of model memory.
- Resolve library ID first, then query docs; do not skip resolution.
- Prefer exact package matches with better reputation/benchmark/version fit.
- Keep documentation queries specific to the user’s exact task.
- Limit Context7 calls to 3 per question; then answer with explicit uncertainty.
- Never send secrets/credentials in tool queries.

### docker-patterns
- Use Docker Compose service-name networking and healthchecks for local multi-service stacks.
- Prefer multi-stage Dockerfiles (dev/build/prod) and minimal runtime images.
- Run production containers as non-root whenever possible.
- Expose only required ports; keep internal services private to Docker network.
- Use named volumes for persistent data and bind mounts for local iteration.
- Split dev/prod behavior with override files instead of one overloaded compose file.

### java-21
- Use records for immutable DTOs/value objects and validate in compact constructors.
- Use sealed hierarchies with switch pattern matching for exhaustive branching.
- Use virtual-thread executors for blocking I/O fan-out.
- Avoid mutable public data carriers.
- Never spawn raw platform threads per request path.

### typescript
- Use const-object-first enums (`as const`) and derive unions from values.
- Keep interfaces flat; extract nested object types into named interfaces.
- Never use `any`; use `unknown`, generics, and type guards.
- Prefer utility types (`Pick`, `Omit`, `Partial`, etc.) over duplicated types.
- Use `import type` for type-only imports.

### angular-architecture
- Apply the Scope Rule: location depends on usage scope, not personal preference.
- Keep feature-local pieces in `features/<feature>/...`.
- Move reusable (2+ features) pieces to `features/shared/...` only.
- Keep app-wide singletons in `core/...`.
- Avoid redundant filename suffixes (`.component`, `.service`, `.model`).
- Prefer one concept per file and readable, intentional member ordering.

### angular-core
- Use standalone components and avoid decorator-era I/O APIs.
- Prefer `input/output/model` function APIs over `@Input/@Output`.
- Model component state with `signal/computed/effect`.
- Prefer `inject()` over constructor injection.
- Use native template control flow (`@if/@for/@switch`).
- Favor zoneless + OnPush patterns where configured.

### angular-forms
- Prefer Reactive Forms for production-stable form handling.
- Use Signal Forms only when explicitly opting into experimental APIs.
- Build forms with `fb.nonNullable.group()` for strict typing.
- Read values with `getRawValue()`.
- Model nested collections explicitly with `FormGroup/FormArray`.

### angular-performance
- Use `NgOptimizedImage` + `ngSrc` for all images.
- Always provide `width/height` or `fill` to prevent layout shifts.
- Mark the LCP image with `priority`.
- Use `@defer` for below-the-fold/heavy UI.
- Lazy-load routes with `loadComponent/loadChildren`.
- Choose SSR only when SEO/time-to-content justifies it.

### security-review
- Start from assets, actors, entry points, and trust boundaries.
- Trace sensitive data flows end-to-end across validation/authz/storage/exposure.
- Review by failure modes: authn/authz, input, exposure, secrets, config, availability, supply chain.
- Reject “security by convention” unless enforced server-side.
- Prioritize realistic exploitability and business impact.
- Recommend the smallest effective control per finding.

### judgment-day
- Resolve/inject matching project standards before launching judges.
- Run two blind parallel reviews with identical scope.
- Synthesize verdict into confirmed/suspect/contradiction buckets.
- Ask user before applying confirmed fixes after first verdict.
- Re-judge only for confirmed CRITICAL blockers; avoid infinite minor loops.
- Escalate after two fix iterations unless user requests continuation.

### issue-creation
- Search duplicates before opening a new issue.
- Use mandatory issue templates; blank issues are disabled.
- New issues start as `status:needs-review`.
- PRs require maintainer-added `status:approved` first.
- Send questions to Discussions, not Issues.

### branch-pr
- Every PR must link an approved issue and include exactly one `type:*` label.
- Branch names must follow `type/description` with lowercase `a-z0-9._-`.
- Use Conventional Commits only; no AI attribution trailers.
- Follow PR template and contributor checklist strictly.
- Ensure required CI checks pass before merge.
- Run shellcheck when shell scripts are touched.

### go-testing
- Prefer table-driven tests for Go business logic.
- Test Bubbletea model transitions directly via `Update()`.
- Use `teatest` for interactive TUI flow tests.
- Use golden files for stable rendered-output assertions.
- Use `t.TempDir()` and interfaces to isolate side effects.

### skill-creator
- Create skills only for recurring/reusable patterns.
- Follow canonical skill layout (`SKILL.md`, optional `assets/`, `references/`).
- Include full frontmatter and explicit Trigger text.
- Keep critical rules concise and examples minimal.
- Use local references (not web URLs) in `references/`.
- Update agent docs/registry after adding a skill.

### java-coding-standards
- Prefer clarity over cleverness and keep objects immutable by default.
- Enforce naming conventions for classes/methods/constants consistently.
- Return `Optional` from find-style operations; avoid `Optional.get()` chains.
- Keep stream pipelines short and readable; use loops when clearer.
- Use domain-specific exceptions and avoid broad catch-all handling.
- Avoid raw types, magic numbers, deep nesting, and silent catches.

### springboot-patterns
- Keep layer boundaries explicit: web → application → infrastructure/domain.
- Keep controllers thin; validate DTOs at API boundaries.
- Keep transactions in application services; no business logic in repositories.
- Centralize error mapping via `@ControllerAdvice`.
- Prefer derived/parameterized data access and explicit pagination.
- Add caching/async/logging intentionally with observability.

### springboot-security
- Prefer stateless auth and deny-by-default authorization.
- Enforce server-side authz with role/ownership checks.
- Validate request DTOs with Bean Validation before side effects.
- Never hardcode secrets; use env vars/secret managers.
- Configure CORS centrally with restricted origins.
- Hash passwords via `PasswordEncoder` (BCrypt/Argon2); avoid unsafe query construction.

### springboot-tdd
- Follow red → green → refactor for features and bug fixes.
- Start with unit tests, then add MockMvc/integration where needed.
- Use JUnit 5 + Mockito + AssertJ with deterministic tests.
- Use Testcontainers for production-like persistence tests.
- Keep coverage as a quality gate (not just a report).

### springboot-verification
- Before PR/release run build, static analysis, tests+coverage, security scan, and diff review.
- Stop immediately on build failures; fix before continuing.
- Prefer infra-backed tests for critical flows.
- Scan dependencies/secrets and common insecure patterns.
- Validate final diff for logging, validation, transactionality, and config drift.

## Project Conventions

| File | Path | Notes |
|------|------|-------|
| AGENTS.md | /home/nico/proyectos/backend/bookhub/AGENTS.md | Index — references files/paths below |
| service contracts | /home/nico/proyectos/backend/bookhub/docs/service-contracts-v1.md | Referenced by AGENTS.md |
| ADR directory | /home/nico/proyectos/backend/bookhub/docs/adr/ | Referenced by AGENTS.md |
| runtime services root | /home/nico/proyectos/backend/bookhub/services/ | Referenced by AGENTS.md |
| infrastructure root | /home/nico/proyectos/backend/bookhub/infrastructure/ | Referenced by AGENTS.md |
| frontend root | /home/nico/proyectos/backend/bookhub/frontend/ | Referenced by AGENTS.md |
| legacy branch reference | /home/nico/proyectos/backend/bookhub/.git/refs/heads/legacy/spring-mvc | Referenced by AGENTS.md |

Read the convention files listed above for project-specific patterns and rules. All referenced paths have been extracted — no need to re-read index files to discover more.
