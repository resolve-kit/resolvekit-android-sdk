# resolvekit-android-sdk

This file is the **table of contents** for coding agents. Keep it short, stable, and current.

## Working Contract

- Humans define goals and constraints.
- Agents implement code, tests, docs, and CI changes.
- Repository-local docs are the source of truth.
- If behavior changes, update docs in the same PR.

## First Read

1. `README.md` for module model and integration patterns.
2. `docs/INDEX.md` for architecture and release knowledge.
3. `docs/agent-first/README.md` for harness principles.

## Commands

```bash
./gradlew test :sample:assembleDebug publishToMavenLocal
```

## Source of Truth Layout

- `sdk/` public SDK facade artifact.
- `core/` shared types and contracts.
- `networking/` API + stream transport.
- `ui/` runtime and chat surfaces.
- `authoring/` annotation API.
- `ksp/` KSP processor.
- `sample/` reference app.
- `docs/INDEX.md` documentation entry point.
- `docs/exec-plans/` plan history and tech debt.

## Guardrails

- No secrets in repo (`.env`, signing keys, credentials).
- Maintain binary compatibility assumptions for public SDK APIs.
- Prefer small dependency upgrades; coordinated sweeps for major versions.
- Run `bash scripts/check_agent_docs.sh` when changing docs structure.
