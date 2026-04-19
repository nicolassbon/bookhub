# Git Workflow for BookHub

## Goal

Keep `main` stable while preserving a simple workflow suitable for solo development.

## Recommendation

- Use `main` as the stable integration branch.
- Develop all meaningful changes in short-lived branches.
- Merge back into `main` only when the change is coherent, reviewed, and testable.

## Why this workflow still matters for a solo project

Even when working alone, branches provide:

- isolation for unfinished work
- cleaner diffs and commits
- easier rollback
- less risk of breaking `main`
- freedom to pause one change and start another without mixing concerns

## Branch strategy

Use lightweight branch names based on intent:

- `feat/...` for new features
- `fix/...` for bug fixes
- `chore/...` for maintenance and tooling
- `docs/...` for documentation-only changes

Examples:

- `feat/identity-rs256`
- `feat/catalog-circuit-breaker`
- `fix/identity-jwt-validation`
- `docs/git-workflow`

## Rules for `main`

- `main` should stay in a releasable or at least integrable state.
- Do not use `main` as a scratchpad once the project has a real base.
- Merge to `main` only after the branch has a complete technical purpose.

## When direct work on `main` is acceptable

Working directly on `main` is only acceptable during the earliest bootstrap stage, when the repository is still being shaped and changes are highly disposable.

Once the project has real architecture, services, contracts, tests, or security-sensitive code, stop working directly on `main`.

## Practical merge criteria

Before merging a branch into `main`, confirm that:

- the branch solves one coherent problem
- the important tests pass for that scope
- the diff is understandable
- configuration changes are intentional
- documentation is updated when needed

## Commit style

Use Conventional Commits.

Examples:

- `feat(identity): migrate JWT signing to RS256`
- `feat(catalog): add circuit breaker for OpenLibrary`
- `fix(identity): validate RSA key material at startup`
- `docs: document solo git workflow`

## Suggested solo workflow

1. Start from updated `main`.
2. Create a focused branch.
3. Implement one coherent change.
4. Review the diff.
5. Run the relevant tests.
6. Merge into `main`.
7. Delete the branch if no longer needed.

## BookHub recommendation

For this project, use branches from now on.

BookHub already has:

- multiple services
- explicit contracts
- security-sensitive flows
- SDD artifacts
- growing architectural decisions

That means the project is past the disposable bootstrap stage. Keeping `main` clean will make the next iterations safer and easier.
