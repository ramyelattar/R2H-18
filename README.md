# R2H+18

R2H+18 is a native Android app built with Kotlin and Jetpack Compose.  
It focuses on private, on-device couple experiences with local-first storage and no backend dependency for core usage.

## Current Status

- Android app module: `app`
- Product/design docs: `docs/superpowers`
- Build system: Gradle Kotlin DSL
- Min SDK: 28
- Target/Compile SDK: 35
- Java/Kotlin target: 17

## Tech Stack

- Kotlin + Jetpack Compose + Material 3
- Room + SQLCipher
- DataStore
- BiometricPrompt
- ExoPlayer (Media3)
- Health Connect
- WorkManager
- Bluetooth LE + Wi-Fi Direct

## Project Structure

```text
R2H-18/
  app/
    src/main/java/com/igniteai/app/
      core/
      data/
      feature/
      ui/
    src/test/
  docs/superpowers/
    specs/
    plans/
```

## Local Development

### Prerequisites

- Android Studio with Android SDK 35
- JDK 17
- Gradle 8.7+ available on PATH (wrapper scripts are not currently committed)

### Build Debug APK

```bash
gradle :app:assembleDebug
```

### Run Unit Tests

```bash
gradle :app:testDebugUnitTest
```

### Run Instrumented Tests (Emulator/Device)

```bash
gradle :app:connectedDebugAndroidTest
```

## Documentation

- Design spec: `docs/superpowers/specs/2026-03-25-igniteai-v1-design.md`
- Implementation plan: `docs/superpowers/plans/2026-03-25-igniteai-v1-implementation.md`

## Plugin Workflow (Codex)

### Canva Plugin

Use the Canva plugin to create and iterate on:

- launch visuals and social creatives
- in-app flow mockups for onboarding/session screens
- branded presentation decks for stakeholder updates

### Test Android Apps Plugin

Use the Test Android Apps plugin to run emulator QA loops:

- launch app and validate navigation flows
- capture screenshots for regressions
- inspect UI state and reproduce bugs with logs

## Notes

- The repository currently does not include `gradlew`/`gradlew.bat`.
- If you want fully reproducible CI/local builds, add Gradle wrapper files in a follow-up change.
