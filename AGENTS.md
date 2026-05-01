# resolvekit-android-sdk

This file is the **table of contents** for coding agents. Keep it short, stable, and current.

## Working Contract

- Humans define goals and constraints.
- Agents implement code, tests, docs, and CI changes.
- Repository-local docs are the source of truth.
- If behavior changes, update docs in the same PR.

## First Read

1. [README.md](README.md) for module model and integration patterns.
2. [CHANGELOG.md](CHANGELOG.md) for architecture and release knowledge.
3. [CONTRIBUTING.md](CONTRIBUTING.md) for harness principles.

## Commands

## Source of Truth Layout

- [sdk/](sdk/) public SDK facade artifact.
- [core/](core/) shared types and contracts.
- [networking/](networking/) API + stream transport.
- [ui/](ui/) runtime and chat surfaces.
- [authoring/](authoring/) annotation API.
- [ksp/](ksp/) KSP processor.
- [sample/](sample/) reference app.
- [docs/INDEX.md](docs/INDEX.md) documentation entry point.
- [AGENTS.md](AGENTS.md) plan history and tech debt.

## Guardrails

- No secrets in repo ([.env.example](.env.example), signing keys, credentials).
- Maintain binary compatibility assumptions for public SDK APIs.
- Prefer small dependency upgrades; coordinated sweeps for major versions.
- Run `./gradlew test` when changing docs structure.
