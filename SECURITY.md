# Security Policy

## Reporting a vulnerability

Do not open public GitHub issues for security problems.

Report vulnerabilities by emailing `hello@resolvekit.app` with:

- a short description of the issue
- affected module or file paths
- reproduction steps or proof of concept
- impact assessment if known

If you need encrypted communication, mention that in the initial email and we will coordinate a secure channel.

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
