# Contributing

## Development setup

1. Install JDK 17.
2. Install Android SDK Platform 36 and Build-Tools 36.x in Android Studio.
3. Clone the repository and open it in Android Studio, or use the Gradle wrapper from the terminal.
4. Add `local.properties` with your SDK path:

```properties
sdk.dir=/path/to/Android/sdk
```

5. If you want to run the sample app against the live backend, add your API key to `local.properties`:

```properties
resolvekit.apiKey=iaa_your_key_here
```

You can also export `RESOLVEKIT_API_KEY` instead of writing it to `local.properties`.

For local publishing credentials, copy [`.env.example`](/Users/t0405/Developer/resolvekit-android-sdk/.env.example) to `.env` and fill in the values you need. Use a base64-encoded private key in `SIGNING_KEY_BASE64`. Keep `.env` local only.

## Validation

Run the same checks that CI runs before opening a pull request:

```bash
./gradlew test :sample:assembleDebug publishToMavenLocal
```

## Project structure

- `core` contains shared types and tool registration.
- `networking` contains the backend client and event stream handling.
- `ui` contains the runtime and host UI surfaces.
- `authoring` contains the `@ResolveKit` annotation APIs.
- `ksp` contains the annotation processor.
- `sample` is the reference Android app.

## Pull requests

- Keep changes scoped.
- Update the README when public behavior changes.
- Do not commit secrets, `.env`, `local.properties`, or private credentials.
- Include verification notes in the pull request body.
