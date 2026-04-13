# Build Quality Hardening — Verification

## Commands

Run from repository root:

```bash
mvn clean compile
mvn clean test
```

## What to check

1. `mvn clean compile`
   - The warning `testAnnotationProcessorPaths` is no longer printed.

2. `mvn clean test`
   - The JDK 21 dynamic-agent warning block is absent.
   - JaCoCo reports are generated for modules that execute tests.

3. JaCoCo report location
   - Per module, open `target/site/jacoco/index.html`.
   - Expected locations after test run:
     - `services/api-gateway/target/site/jacoco/index.html`
     - `services/identity-service/target/site/jacoco/index.html`
     - `services/catalog-service/target/site/jacoco/index.html`
     - `services/library-service/target/site/jacoco/index.html`

## Final evidence

- Run date: 2026-04-12
- `mvn clean compile`: PASS
- `mvn clean test`: PASS
- `testAnnotationProcessorPaths` warning removed: YES
- JDK21 dynamic-agent warning removed: YES
- JaCoCo HTML generated in modules with tests: YES

### Scenario mapping (spec)

| Requirement scenario | Result | Evidence |
|---|---|---|
| Compiler warning elimination (`testAnnotationProcessorPaths`) | PASS | `mvn clean compile` output no longer prints unknown parameter warnings for compiler plugin. |
| JDK21 Mockito dynamic agent warning mitigation | PASS | `mvn clean test` output no longer prints `WARNING: A Java agent has been loaded dynamically` nor `Mockito is currently self-attaching...`. |
| JaCoCo report for module with tests | PASS | `target/site/jacoco/index.html` exists for all service modules (`api-gateway`, `identity-service`, `catalog-service`, `library-service`). |
| Module without tests should not fail build | PASS | Root parent module logs `Skipping JaCoCo execution due to missing execution data file.` and build continues successfully. |
