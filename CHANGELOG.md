# Changelog

All notable changes to the ResolveKit Android SDK will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.1] - 2026-04-30

### Fixed
- Event stream reconnection after app backgrounding
- Tool call result encoding for nullable return types
- Compose chat view rendering in fragment hosts

### Changed
-  now auto-starts runtime on composition
-  defaults to auto-generated UUID if null

### Added
-  for View-based app integration
-  KSP annotation for typed tool authoring
-  for custom JSON context injection
-  for per-session tool scoping
- GitHub Packages + Maven Central publish pipeline
- Sample app with working MainActivity + SampleFunctions
- Unit tests for core, networking, and ui modules

## [1.0.0] - 2026-04-01

### Added
- Kotlin-first runtime for ResolveKit sessions
- Jetpack Compose  component
-  for full-screen chat
- KSP-based  annotation for tool functions
- Multi-module architecture (sdk, core, networking, ui, authoring, ksp)
- HTTP/3-first session event stream
- Reconnect with in-flight turn replay

