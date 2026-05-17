# BookHub V1 Closure Roadmap

This roadmap defines the shortest high-signal path to close BookHub V1 based on the current repository state and active docs. It separates what is already in place from what is still required for V1 closure and what should intentionally wait until after V1.

## 1) Implemented foundations (already in place)

| Area | Current baseline |
|---|---|
| Runtime shape | `api-gateway` + `identity-service` + `catalog-service` + `library-service` running as the V1 topology |
| Identity | Registration, login, refresh rotation, logout, password recovery/reset, `GET /api/v1/users/me` |
| Catalog | Public search/detail (`/api/v1/books/**`), local persistence, Open Library bootstrap/integration |
| Library core | Add/list/get library entries, state transitions, reading progress |
| Library extended V1 scope | Yearly goals, reviews (create/update/list by book), notifications (list/mark read) |
| Internal service auth | Catalog internal routes require service-to-service auth; identity issues service tokens; library forwards service tokens to catalog internal APIs |
| Platform baseline | Gateway routing, Flyway migrations, PostgreSQL per service boundary, Docker Compose local stack, ADR and contract docs |
| Auth rate limiting | Redis-backed distributed rate limiting on all auth endpoints, shared TTL counters across instances, trusted-proxy fingerprinting, fail-closed on store unavailability |

## 2) Must-have remaining work for V1 closure

These items are required to claim a production-ready V1 baseline, not only a feature-complete demo.

### ~~A. Make auth rate limiting production-grade~~ ✅ Done

> Delivered in commit `89d4f3e`. Redis-backed sliding-window counters replaced node-local `ConcurrentHashMap` state. Trusted-proxy fingerprinting preserved. Fail-closed on Redis unavailability (503). Contracts and docs updated.

### ~~B. Deliver minimum admin/moderation surfaces defined for V1~~ ✅ Done

**Outcome:** V1 admin scope is minimally usable instead of roadmap-only.

- Implemented initial admin endpoints for user visibility and review moderation workflows.
- Added role-gated authorization and tests for admin-only surfaces.
- Aligned exposed endpoints with `docs/service-contracts-v1.md` and updated contracts where implementation differs.

### ~~C. Close V1 alignment pass across docs and executable contracts~~ ✅ Done

**Outcome:** V1 can be reviewed without ambiguity between intent and running behavior.

- Reconciled PRD/contract expectations with implemented endpoints and role boundaries.
- Updated root, service, and infrastructure READMEs to reflect final V1 closure scope (including admin APIs and shared rate limiting).
- Documented recently mitigated security issues in the backlog.


## 3) Optional / post-V1 (do not block V1 closure)

- Full social feed, advanced reactions/comments, and friendship graph expansion.
- Real-time notification delivery.
- Payments/freemium capabilities.
- Event-driven decomposition beyond synchronous HTTP-first V1.
- Broader observability and performance hardening beyond the minimum viable baseline.

## V1 closure checklist

- [x] Catalog internal routes require authenticated service-to-service access.
- [x] Auth rate limiting is shared-state and multi-instance consistent.
- [x] Initial admin/moderation APIs are implemented and role-protected.
- [x] Contracts and operational docs match the final executable V1 state.
