# ResolveKit Android SDK

[![CI](https://github.com/resolve-kit/resolvekit-android-sdk/actions/workflows/android.yml/badge.svg)](https://github.com/resolve-kit/resolvekit-android-sdk/actions/workflows/android.yml)
[![Maven Central](https://img.shields.io/maven-central/v/app.resolvekit/sdk.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/app.resolvekit/sdk)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Min SDK](https://img.shields.io/badge/minSDK-24-blue.svg)](https://developer.android.com/studio/releases/platforms)

Android SDK for embedding ResolveKit agent chat, tool calling, and host UI surfaces into Android apps.

**Support is moving into the product. ResolveKit is where it lands.**

The repository contains:

- a Kotlin-first runtime for ResolveKit sessions
- a Jetpack Compose chat UI
- Activity and Fragment host surfaces for non-Compose apps
- a KSP-based authoring path for defining tools with `@ResolveKit`
- a sample Android app

## Modules

| Module | Purpose |
| --- | --- |
| `sdk` | Public facade artifact that pulls in the default ResolveKit runtime and chat UI stack |
| `core` | Shared JSON types, tool definitions, registry, and errors |
| `networking` | ResolveKit API and event stream clients |
| `ui` | Runtime, Compose chat view, `ResolveKitChatActivity`, and `ResolveKitChatFragment` |
| `authoring` | `@ResolveKit` annotation and authoring interfaces |
| `ksp` | KSP processor that generates tool adapters |
| `sample` | Reference Android application |

## Requirements

| Requirement | Value |
| --- | --- |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |
| JDK | 17 |
| Android Studio | Ladybug+ |
| Gradle | 8.9+ (wrapper included) |
| Compose | BOM 2024.12+ |
| Kotlin | 2.0+ |

## Installation

ResolveKit is set up to publish these coordinates:

```text
app.resolvekit:sdk:<version>
app.resolvekit:authoring:<version>
app.resolvekit:ksp:<version>
```

`core`, `networking`, and `ui` remain published for advanced/internal use, but `sdk` is the default dependency for app consumers.

### Maven Central

For most apps, depend on `sdk`. It brings in the default ResolveKit runtime and UI stack.

```kotlin
dependencies {
    implementation("app.resolvekit:sdk:1.0.1")
}
```

If you want the annotation-based tool authoring path:

```kotlin
plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    implementation("app.resolvekit:sdk:1.0.1")
    implementation("app.resolvekit:authoring:1.0.1")
    ksp("app.resolvekit:ksp:1.0.1")
}
```

### GitHub Packages

Use this when you need preview artifacts before a Maven Central release.

```kotlin
repositories {
    google()
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/resolve-kit/resolvekit-android-sdk")
        credentials {
            username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
            password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
```

Then use the same coordinates shown above.

### Local Maven for Android Studio testing

If you are working on the SDK locally and want to consume it from another app without publishing externally:

```bash
./gradlew publishToMavenLocal
```

Then add `mavenLocal()` to the consuming app:

```kotlin
repositories {
    mavenLocal()
    google()
    mavenCentral()
}
```

### Composite build from another app

For active SDK development, composite builds are usually the best Android Studio workflow.

In the host app `settings.gradle.kts`:

```kotlin
includeBuild("../resolvekit-android-sdk")
```

Then depend on the project coordinates normally:

```kotlin
dependencies {
    implementation("app.resolvekit:sdk:1.0.1")
    implementation("app.resolvekit:authoring:1.0.1")
    ksp("app.resolvekit:ksp:1.0.1")
}
```

Gradle will substitute the local modules automatically.

## Quick Start

### Compose-first integration

```kotlin
val runtime = ResolveKitRuntime(
    configuration = ResolveKitConfiguration(
        apiKeyProvider = { "iaa_your_key_here" },
        functions = listOf(GetCurrentTime)
    ),
    context = applicationContext
)

setContent {
    MaterialTheme {
        ResolveKitChatView(runtime = runtime)
    }
}
```

### Launch as a screen from a View-based app

```kotlin
startActivity(
    ResolveKitChatActivity.createIntent(
        context = this,
        configuration = ResolveKitConfiguration(
            apiKeyProvider = { "iaa_your_key_here" },
            functions = listOf(GetCurrentTime)
        )
    )
)
```

### Embed as a Fragment

```kotlin
supportFragmentManager.beginTransaction()
    .replace(
        R.id.container,
        ResolveKitChatFragment.newInstance(
            ResolveKitConfiguration(
                apiKeyProvider = { "iaa_your_key_here" },
                functions = listOf(GetCurrentTime)
            )
        )
    )
    .commit()
```

## Configuration Reference

`ResolveKitConfiguration` is passed to `ResolveKitRuntime` at initialization and is immutable after that point.

```kotlin
ResolveKitConfiguration(
    baseURL: URL = URL("https://agent.example.com"),
    apiKeyProvider: () -> String?,
    deviceIDProvider: () -> String? = { null },
    llmContextProvider: () -> JSONObject = { emptyMap() },
    availableFunctionNamesProvider: (() -> List<String>)? = null,
    localeProvider: (() -> String)? = null,
    functions: List<AnyResolveKitFunction> = emptyList(),
    context: Context
)
```

### `baseURL`

**Type:** `URL` | **Required:** No | **Default:** `https://agent.example.com`

Base URL of the ResolveKit backend. Override only when self-hosting:

```kotlin
baseURL = URL("https://your-backend.example.com")
```

### `apiKeyProvider`

**Type:** `() -> String?` | **Required:** Yes

Called at the start of each session. Return `null` or an empty string to block connection.

```kotlin
apiKeyProvider = { SecureConfig.getApiKey() }
```

### `deviceIDProvider`

**Type:** `() -> String?` | **Required:** No | **Default:** `{ null }`

Stable device or user identifier used to correlate sessions across app launches. If `null` is returned, the SDK generates and persists a UUID automatically.

```kotlin
deviceIDProvider = {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    prefs.getString("device_id", null) ?: run {
        val id = UUID.randomUUID().toString()
        prefs.edit().putString("device_id", id).apply()
        id
    }
}
```

### `llmContextProvider`

**Type:** `() -> JSONObject` | **Required:** No | **Default:** `{ emptyMap() }`

Custom JSON context sent as `llm_context` during session creation. Use it to pass user preferences, location, app state, or any signal the agent needs at routing time.

### `localeProvider`

**Type:** `(() -> String)?` | **Required:** No | **Default:** `null`

Provides the preferred locale for the chat session as a BCP 47 language tag (e.g. `"en"`, `"lt"`, `"fr-CA"`). If `null`, the SDK resolves locale from system settings.

### `functions`

**Type:** `List<AnyResolveKitFunction>` | **Required:** No | **Default:** `emptyList()`

List of tool functions available to the agent.

## Runtime API

`ResolveKitRuntime` exposes the following state via `StateFlow`:

| Property | Type | Description |
| --- | --- | --- |
| `messages` | `StateFlow<List<ResolveKitChatMessage>>` | Chat transcript in chronological order |
| `connectionState` | `StateFlow<ResolveKitConnectionState>` | Current session-stream connection phase |
| `isTurnInProgress` | `StateFlow<Boolean>` | True while the agent is processing a turn |
| `pendingToolCall` | `StateFlow<ResolveKitPendingToolCall?>` | Current active tool call awaiting approval |
| `toolCallChecklist` | `StateFlow<List<ToolCallChecklistItem>>` | Live checklist of tool calls in current batch |
| `toolCallBatchState` | `StateFlow<ResolveKitToolCallBatchState>` | Aggregate approval state of current batch |
| `executionLog` | `StateFlow<List<String>>` | Debug log of runtime lifecycle events |

### Connection States

| State | Description |
| --- | --- |
| `Disconnected` | Not connected (initial state) |
| `Connecting` | Establishing WebSocket connection |
| `Connected` | Session established, ready for chat |
| `Reconnecting` | Connection lost, attempting reconnect |
| `Error` | Connection failed with error |

## Defining Tools

### Manual tool registration

```kotlin
object GetCurrentTime : AnyResolveKitFunction {
    override val resolveKitName = "get_current_time"
    override val resolveKitDescription = "Returns the current local time"
    override val resolveKitParametersSchema = mapOf(
        "type" to JSONValue.String("object"),
        "properties" to JSONValue.Object(emptyMap())
    )
    override val resolveKitTimeoutSeconds = 5
    override val resolveKitRequiresApproval = false

    override suspend fun invoke(
        arguments: JSONObject,
        context: ResolveKitFunctionContext
    ): JSONValue = JSONValue.String("12:00:00 UTC")
}
```

### KSP authoring with `@ResolveKit`

```kotlin
@ResolveKit(
    name = "echo_message",
    description = "Echoes back the provided message",
    requiresApproval = false
)
class EchoMessage(private val message: String) : ResolveKitFunction {
    override suspend fun perform(): Any = message
}
```

The processor generates `EchoMessageResolveKitAdapter`, which can be passed into `ResolveKitConfiguration.functions`.

### Supported Parameter Types

| Kotlin type | JSON Schema type | LLM coercion |
| --- | --- | --- |
| `String` | `"string"` | Tolerates numbers/bools |
| `Boolean` | `"boolean"` | Tolerates `1`/`0`/`"true"`/`"false"` |
| `Int`, `Long` | `"integer"` | Truncates `3.0 → 3` |
| `Float`, `Double` | `"number"` | — |
| `T?` (any of above) | Same as `T`, not in `required` | `null` if key absent |
| `List<T>` | `"array"` with `"items"` schema | — |
| `Map<String, V>` | `"object"` | — |
| Nested `data class` | `"object"` | — |

## ProGuard / R8 Rules

Add these rules to your app's `proguard-rules.pro` to prevent obfuscation of ResolveKit classes:

```proguard
# ResolveKit
-keep class app.resolvekit.** { *; }
-keep class app.resolvekit.core.** { *; }
-keep class app.resolvekit.ui.** { *; }
-keep class app.resolvekit.networking.** { *; }

# KSP-generated adapters
-keep class **ResolveKitAdapter { *; }
-keep class **_ResolveKitAdapter { *; }

# JSON serialization
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep function names for tool dispatch
-keepnames class app.resolvekit.authoring.** { *; }
```

## Android Studio Workflows

### Work on the SDK itself

1. Open this repository in Android Studio.
2. Ensure `local.properties` points at your Android SDK:

```properties
sdk.dir=/path/to/Android/sdk
```

3. If you want to run the sample app, also add:

```properties
resolvekit.apiKey=iaa_your_key_here
```

4. Select the `sample` run configuration or run:

```bash
./gradlew :sample:installDebug
```

### Use the SDK source from another Android Studio project

Recommended options:

1. `includeBuild("../resolvekit-android-sdk")` for day-to-day local development.
2. `publishToMavenLocal` when you want artifact-style consumption without an external registry.

Avoid manually copying AARs between projects unless you specifically need a binary handoff.

## Building and Testing

```bash
./gradlew test
./gradlew :sample:assembleDebug
./gradlew publishToMavenLocal
```

## Publishing

Shared publishing logic lives in [`gradle/publish.gradle.kts`](gradle/publish.gradle.kts).

Configured targets:

- Maven Central
- GitHub Packages
- local Maven via `publishToMavenLocal`

Expected release credentials are read from Gradle properties or environment variables:

- `MAVEN_CENTRAL_USERNAME` / `MAVEN_CENTRAL_PASSWORD`
- `GITHUB_PACKAGES_USERNAME` / `GITHUB_PACKAGES_PASSWORD`
- `SIGNING_KEY_BASE64` / `SIGNING_PASSWORD`

Legacy `OSSRH_USERNAME` and `OSSRH_PASSWORD` are also accepted for compatibility.

For local setup, copy `.env.example` to `.env` and fill in the values. `.env` is loaded by the publishing script and is gitignored.

For Maven Central releases with Gradle's built-in `maven-publish`, the artifact upload and the Portal release handoff are separate steps. The GitHub Actions workflow performs both automatically. For a local release, run:

```bash
./gradlew publishAllPublicationsToMavenCentralRepository

auth="$(printf '%s:%s' "$MAVEN_CENTRAL_USERNAME" "$MAVEN_CENTRAL_PASSWORD" | base64 | tr -d '\n')"
curl --fail --silent --show-error \
  -X POST \
  -H "Authorization: Bearer $auth" \
  "https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/app.resolvekit?publishing_type=automatic"
```

After the handoff call succeeds, Sonatype may keep the deployment in `PUBLISHING` state for several minutes before the modules appear in Maven Central search.

GitHub Actions publishing is defined in `.github/workflows/publish.yml`. GitHub Packages uses `GITHUB_TOKEN`; Maven Central publishing runs only when these repository secrets are configured:

- `MAVEN_CENTRAL_USERNAME`
- `MAVEN_CENTRAL_PASSWORD`
- `SIGNING_KEY_BASE64`
- `SIGNING_PASSWORD`

## Troubleshooting

### Connection fails with 401
- Verify your API key starts with `iaa_`
- Check that `baseURL` points to a running ResolveKit backend
- Ensure the backend has an app configured for your API key

### Tool calls not appearing in chat
- Verify functions are registered in `ResolveKitConfiguration.functions`
- Check that the function name matches exactly (snake_case)
- Ensure the KSP processor ran: `./gradlew build` should generate `*ResolveKitAdapter` classes

### Compose UI not rendering
- Ensure `ResolveKitRuntime` is created with a valid `Context`
- Check that `MaterialTheme` wraps `ResolveKitChatView`
- Verify Compose BOM version compatibility

### ProGuard crashes in release builds
- Add the ProGuard rules from the [ProGuard / R8 Rules](#proguard--r8-rules) section above
- Run `./gradlew :sample:assembleRelease` to test with minification enabled

## Repository Hygiene

This repo includes:

- `LICENSE` (MIT)
- `CONTRIBUTING.md`
- `SECURITY.md`
- GitHub issue templates
- CI workflow for build, test, and local publication validation

## Development Notes

- `local.properties` is intentionally ignored.
- Do not commit API keys or publishing credentials.
- Public API changes should be reflected in this README.
