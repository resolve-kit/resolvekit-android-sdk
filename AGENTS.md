# resolvekit-android-sdk

This file is the **table of contents** for coding agents. Keep it short, stable, and current.

## Working Contract

- Humans define goals and constraints.
- Agents implement code, tests, docs, and CI changes.
- Repository-local docs are the source of truth.
- If behavior changes, update docs in the same PR.

## First Read

1.  for module model and integration patterns.
2.  for architecture and release knowledge.
3.  for harness principles.

## Commands



## Source of Truth Layout

-  public SDK facade artifact.
-  shared types and contracts.
-  API + stream transport.
-  runtime and chat surfaces.
-  annotation API.
-  KSP processor.
-  reference app.
-  documentation entry point.
-  plan history and tech debt.

## Guardrails

- No secrets in repo (, signing keys, credentials).
- Maintain binary compatibility assumptions for public SDK APIs.
- Prefer small dependency upgrades; coordinated sweeps for major versions.
- Run  when changing docs structure.

