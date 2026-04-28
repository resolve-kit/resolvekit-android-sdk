# ResolveKit Android SDK

Android SDK for embedding ResolveKit agent chat, tool calling, and host UI surfaces into Android apps.

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

- JDK 17
- Android Studio with Android SDK Platform 36
- Gradle wrapper included in the repo
- ResolveKit API key if you want to run the sample app against the live backend

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

For local setup, copy [`.env.example`](/Users/t0405/Developer/resolvekit-android-sdk/.env.example) to `.env` and fill in the values. `.env` is loaded by the publishing script and is gitignored.

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

GitHub Actions publishing is defined in [`.github/workflows/publish.yml`](/Users/t0405/Developer/resolvekit-android-sdk/.github/workflows/publish.yml). GitHub Packages uses `GITHUB_TOKEN`; Maven Central publishing runs only when these repository secrets are configured:

- `MAVEN_CENTRAL_USERNAME`
- `MAVEN_CENTRAL_PASSWORD`
- `SIGNING_KEY_BASE64`
- `SIGNING_PASSWORD`

## Repository Hygiene

This repo includes:

- `LICENSE`
- `CONTRIBUTING.md`
- `SECURITY.md`
- GitHub issue templates
- CI workflow for build, test, and local publication validation

## Development Notes

- `local.properties` is intentionally ignored.
- Do not commit API keys or publishing credentials.
- Public API changes should be reflected in this README.
