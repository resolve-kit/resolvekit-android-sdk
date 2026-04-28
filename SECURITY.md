# Security Policy

## Reporting a vulnerability

Do not open public GitHub issues for security problems.

Use GitHub private vulnerability reporting for this repository:

- https://github.com/resolve-kit/resolvekit-android-sdk/security/advisories/new

Please include:

- affected version or commit
- affected module or file paths
- reproduction steps or proof of concept
- impact assessment if known
- any known mitigations

## Scope

This repository contains:

- Android client runtime and UI code
- API integration code for ResolveKit services
- sample application code
- Gradle publishing and packaging logic

## Secrets and credentials

- Never commit API keys, signing keys, or Sonatype or GitHub package credentials.
- Use environment variables or local-only Gradle properties for release credentials.
- Keep `local.properties` out of version control.
