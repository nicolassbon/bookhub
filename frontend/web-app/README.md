# BookHub Web Application

Standalone, zoneless Angular application for the BookHub social reading platform.

## Technology Stack

- **Framework:** Angular (Zoneless Change Detection, Signals)
- **Language:** TypeScript
- **Styling:** Vanilla SCSS with curated CSS Custom Properties design system (`src/_tokens.scss`)
- **Testing:** Vitest & `@testing-library/angular`
- **Package Manager:** pnpm

## Architecture & File Naming Conventions

This application follows the official **Angular 2025/2026 Style Guide**:

- **Concise Naming:** Redundant suffixes like `.component` are omitted. Component files use `[name].ts`, `[name].html`, `[name].scss`, and `[name].spec.ts` (e.g., `login.ts`, `login.html`, `login.scss`, `login.spec.ts`).
- **Standalone Components:** All components, pipes, and directives are standalone.
- **Signal-based State:** Application state and session tokens use Angular Signals (`SessionStore`).
- **Lazy Loading:** All feature routes (`/login`, `/register`, `/catalog`, `/catalog/books/:id`, `/library`, `/profile`, `/notifications`) are lazy-loaded via standalone `loadComponent()`.

## Features

- **Auth & Session Management:** Form validation, JWT in-memory store, single-flight refresh token interceptor, and route guards (`authGuard`, `guestGuard`).
- **Catalog & Discovery:** Debounced search, pagination, book detail views, and community reviews.
- **Reading Library:** Tabbed shelf filtering (`TODOS`, `WANT_TO_READ`, `READING`, `READ`), percentage progress indicators, and page progress updates.
- **Engagement & Profile:** Yearly reading goals widget, public & authenticated reviews submission, profile display name updates, and notifications center.
- **Accessibility:** Keyboard skip link (`#main-content`), ARIA landmarks, status/alert live regions, and screen-reader utilities (`.sr-only`).

## Available Scripts

Run from `frontend/web-app`:

```bash
pnpm install
pnpm start     # Runs dev server with proxy to API Gateway at http://localhost:8080
pnpm test      # Runs Vitest unit test suite (40/40 tests)
pnpm build     # Generates production CSR bundle in dist/web-app
```
