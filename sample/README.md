# ResolveKit Sample App

A complete reference Android application demonstrating all integration patterns
and features of the ResolveKit Android SDK.

## Quick Start

### 1. Prerequisites

| Requirement | Value |
|---|---|
| Android Studio | Ladybug+ (2024.2) |
| JDK | 17 |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 36 |
| Gradle | 8.9+ (wrapper included) |

### 2. Configure API Credentials

The sample app requires a ResolveKit API key and backend URL. Set these in
one of two ways:

#### Option A: `local.properties` (recommended for local dev)

Create or edit `local.properties` in the repository root:

```properties
sdk.dir=/path/to/Android/sdk
resolvekit.apiKey=rk_your_api_key_here
resolvekit.baseUrl=https://agent.example.com
```

#### Option B: Environment variables

```bash
export RESOLVEKIT_API_KEY="rk_your_api_key_here"
export RESOLVEKIT_BASE_URL="https://agent.example.com"
```

> **No API key?** The app will still build and launch, but the chat session
> will fail to connect. The UI itself renders fine for visual testing.

### 3. Build & Run

```bash
# Build the debug APK
./gradlew :sample:assembleDebug

# Install on a connected device or emulator
./gradlew :sample:installDebug

# Or simply open the project in Android Studio and press ▶ on `sample`
```

## Demo Screens

When you launch the sample app, you'll see a **Dashboard** with four
integration demos:

### 1. Compose Chat

**Pattern:** Embed `ResolveKitChatView` directly in a Compose UI.

This is the recommended approach for Compose-first apps. The runtime is
created at the Activity level and the chat view is embedded inside a
`Scaffold` with a custom `TopAppBar` showing live connection state.

**Key files:**
- `ComposeChatActivity.kt` — Activity setup
- Demonstrates `collectAsState()` for runtime StateFlows

### 2. Full-Screen Activity

**Pattern:** Launch `ResolveKitChatActivity` as a standalone screen.

Ideal for apps that want a drop-in chat screen without building their own
UI. The SDK handles the entire lifecycle:

```kotlin
startActivity(
    ResolveKitChatActivity.createIntent(
        context = this,
        configuration = myConfiguration
    )
)
```

**Key files:**
- No custom Activity needed — the SDK provides `ResolveKitChatActivity`
- Uses `ResolveKitChatHostRegistry` internally for safe runtime handoff

### 3. Fragment Embedding

**Pattern:** Embed `ResolveKitChatFragment` inside a View-based layout.

For apps using traditional View XML layouts or Fragment navigation:

```kotlin
supportFragmentManager.commit {
    replace(
        R.id.fragment_container,
        ResolveKitChatFragment.newInstance(configuration)
    )
}
```

**Key files:**
- `FragmentHostActivity.kt` — Host Activity with CoordinatorLayout + Toolbar
- `res/layout/activity_fragment_host.xml` — XML layout with fragment container

### 4. Approval Flow Demo

**Pattern:** Observe and display tool-call approval UI.

Demonstrates functions with `requiresApproval = true`. When the agent
invokes such a function, the SDK shows an approval prompt. This screen
also observes the tool call checklist StateFlow to display live status.

**Key files:**
- `ApprovalDemoActivity.kt` — Observes `toolCallBatchState` and `toolCallChecklist`
- `DeleteData` function in `SampleFunctions.kt` — has `requiresApproval = true`

## Architecture

```
sample/
├── src/main/
│   ├── kotlin/app/resolvekit/sample/
│   │   ├── MainActivity.kt           # Launcher → redirects to Dashboard
│   │   ├── DashboardActivity.kt      # Entry point with integration cards
│   │   ├── ComposeChatActivity.kt    # Compose-first demo
│   │   ├── FragmentHostActivity.kt   # Fragment embedding demo
│   │   ├── ApprovalDemoActivity.kt   # Approval flow demo
│   │   ├── SampleApplication.kt      # Application-level config
│   │   └── SampleFunctions.kt        # Tool function definitions
│   ├── res/
│   │   └── layout/
│   │       └── activity_fragment_host.xml  # Fragment host layout
│   └── AndroidManifest.xml
└── build.gradle.kts
```

## Tool Functions

The sample includes several tool functions demonstrating different patterns:

| Function | Pattern | Approval | Parameters |
|---|---|---|---|
| `EchoMessage` | `@ResolveKit` annotation (KSP) | No | `message: String` |
| `GetCurrentTime` | Manual `AnyResolveKitFunction` | No | None |
| `AddNumbers` | Manual with numeric coercion | No | `a: number`, `b: number` |
| `DeleteData` | Manual with approval | **Yes** | `item_id: string` |
| `GetWeather` | Manual with simulated API | No | `city: string` |
| `SearchNotes` | Manual with optional param | No | `query: string`, `limit: int?` |

### Try These Prompts

Once connected, try asking the agent:

- _"What time is it?"_ → triggers `GetCurrentTime`
- _"Add 15 and 27"_ → triggers `AddNumbers`
- _"What's the weather in Tokyo?"_ → triggers `GetWeather`
- _"Search notes for 'meeting'"_ → triggers `SearchNotes`
- _"Delete item test-123"_ → triggers `DeleteData` with **approval prompt**

## Configuration

### Shared Application Config

`SampleApplication` creates a single `ResolveKitConfiguration` shared across
all screens. This mirrors a real app's DI setup (Hilt/Koin):

```kotlin
class SampleApplication : Application() {
    val configuration: ResolveKitConfiguration by lazy {
        ResolveKitConfiguration(
            baseUrl = BuildConfig.RESOLVEKIT_BASE_URL,
            apiKeyProvider = { BuildConfig.RESOLVEKIT_API_KEY.takeIf { it.isNotBlank() } },
            functions = listOf(GetCurrentTime, AddNumbers, DeleteData, GetWeather, SearchNotes),
            functionPacks = listOf(SampleFunctionPack)
        )
    }
}
```

### Function Packs

`SampleFunctionPack` demonstrates grouping related tools:

```kotlin
object SampleFunctionPack : ResolveKitFunctionPack {
    override val id = "sample_utilities"
    override val functions = listOf(EchoMessageAdapterHolder)
}
```

## Build Variants

| Variant | Description |
|---|---|
| `debug` | Default — uses local.properties / env vars |
| `release` | ProGuard/R8 enabled — see root README for rules |

## Common Issues

### App crashes on launch with "No API key"
The API key provider returns null/empty. Check `local.properties` or env
vars. The chat view still renders but cannot connect.

### KSP adapter not generated
Run `./gradlew :sample:build` to trigger the KSP processor. The generated
`*ResolveKitAdapter` classes appear in `sample/build/generated/ksp/`.

### Fragment Host Activity crashes
Ensure `appcompat` and `material` dependencies are in `build.gradle.kts`.
The FragmentHostActivity uses `AppCompatActivity` and Material components.

### Connection shows BLOCKED
Your API key is invalid or the backend URL is unreachable. Check:
- Key starts with `rk_`
- `baseUrl` is correct
- Backend is running and accessible from your device/emulator
