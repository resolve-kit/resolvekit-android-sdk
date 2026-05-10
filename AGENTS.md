# resolvekit-android-sdk

ResolveKit Android SDK — native runtime, UI, and tool function integration for Android.

## Working Contract

- Humans define API surface and quality bars.
- Agents implement code, tests, and CI changes.
- SDK changes must maintain backward compatibility for public APIs.
- Breaking changes must include migration notes.

## Project Overview

ResolveKit Android SDK provides runtime orchestration, tool calling, and chat UI surfaces for Kotlin apps.

**Tech Stack**: Kotlin 1.9.22, Compose, KSP, OkHttp, kotlinx.serialization
**Min SDK**: 26 | **Compile SDK**: 36 | **JDK**: 17
**Packages**: `app.resolvekit:sdk`, `app.resolvekit:authoring`, `app.resolvekit:ksp`

## Agent Skills

This repo ships with integration skills in `.agents/skills/`. Load them when relevant:

- `resolvekit-android-integration` — How to integrate this SDK into an Android project. Covers Maven installation, KSP function authoring, runtime configuration, Compose/Activity/Fragment UI integration, ProGuard rules, theming, and troubleshooting.
- `resolvekit-agent-instructions` — How AI agents should approach ResolveKit integration. Covers project detection, function design patterns, integration order, and verification.

When a user asks to integrate ResolveKit into their Android project, load `resolvekit-android-integration` and follow its steps.

## First Read

1. `build.gradle.kts` for project structure and plugins.
2. `sdk/`, `core/`, `ui/`, `networking/`, `authoring/`, `ksp/` for module organization.
3. `sample/` for integration examples.
