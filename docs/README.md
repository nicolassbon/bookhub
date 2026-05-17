# BookHub Documentation Index

This folder contains active documentation used to guide implementation and operate the repository.

## Normative / Source of Truth

These documents define product intent, architecture, and contracts. They should not be rewritten just to match temporary implementation gaps.

- `adr/` — Architecture Decision Records.
- `git-workflow.md` — Git and branch workflow conventions.
- `legacy-feature-inventory.md` — Legacy feature inventory reference.
- `bounded-contexts-v1.md` — Domain boundaries.
- `domain-model-v1.md` — Core domain model (needs future refresh).
- `prd-bookhub-v1.md` — Product requirements baseline.
- `repository-structure-v1.md` — Monorepo/service structure reference.
- `service-contracts-v1.md` — API contract baseline.

## Operational / Tracking Docs

These documents must reflect the current executable state of the repository, local setup, and active remediation work.

- `security-hardening-backlog.md` — Future security hardening opportunities beyond active defects.

## Service and Infrastructure READMEs

The following files live outside `docs/`, but they are operational documentation and must stay aligned with the running code and configuration:

- `README.md`
- `infrastructure/docker/README.md`
- `services/api-gateway/README.md`
- `services/identity-service/README.md`
- `services/catalog-service/README.md`
- `services/library-service/README.md`
