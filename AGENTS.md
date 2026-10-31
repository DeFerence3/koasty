# Repository Guidelines

## Project Structure & Module Organization

Koasty is a Compose Multiplatform notification library.

- `koasty/`: public library; shared Kotlin code lives in `src/commonMain/kotlin/com/deference/koasty/`. Platform implementations use `androidMain`, `iosMain`, `jvmMain`, `jsMain`, and `wasmJsMain`.
- `shared/`: demo UI and themes; shared assets live in `src/commonMain/composeResources/`.
- `androidApp/`, `desktopApp/`, `webApp/`, `iosApp/`: platform demo entry points. Android resources use `src/main/res/`; iOS assets use asset catalogs.
- `gradle/libs.versions.toml`: dependency and plugin versions.

Keep reusable notification behavior in `koasty`; keep sample-specific presentation in `shared`.

## Build, Test, and Development Commands

Use JDK 21 and the Gradle wrapper from the repository root. Commands below use PowerShell; on macOS/Linux, replace `.\gradlew.bat` with `./gradlew`.

- `.\gradlew.bat :koasty:build` — assemble and check the library.
- `.\gradlew.bat :androidApp:assembleDebug` — build the Android demo APK.
- `.\gradlew.bat :desktopApp:run` — launch the desktop demo.
- `.\gradlew.bat :webApp:wasmJsBrowserDevelopmentRun` — launch the Wasm demo; use `:webApp:jsBrowserDevelopmentRun` for JavaScript.
- `.\gradlew.bat :koasty:testAndroidHostTest :koasty:jvmTest` — run Android host and JVM tests.
- `.\gradlew.bat :koasty:connectedAndroidDeviceTest` — run instrumentation tests on a connected device/emulator.
- `.\gradlew.bat :koasty:lint` — run configured Android/JVM lint checks.

Open `iosApp/iosApp.xcodeproj` in Xcode for iOS development. Apple binaries and simulator tests require macOS.

## Coding Style & Naming Conventions

Follow the configured official Kotlin style with four-space indentation. Use PascalCase for types and UI composables, camelCase for functions/properties, and UPPER_SNAKE_CASE for constants. Match filenames to their primary declarations; platform files follow patterns such as `Platform.android.kt`. Keep platform APIs in platform source sets. Use IDE Kotlin formatting; no ktlint or Detekt configuration exists.

## Testing Guidelines

Use `kotlin.test` for shared/host tests and JUnit 4 with AndroidX for device tests. Existing examples live under `koasty/src/androidHostTest/` and `koasty/src/androidDeviceTest/`; add portable behavior tests under `koasty/src/commonTest/kotlin/`. Name classes `*Test` and methods descriptively, such as `dismiss_ignoresStaleId`. Cover behavior changes, especially notification lifetime and dismissal. No coverage threshold is configured.

## Commit & Pull Request Guidelines

Follow the history's Conventional Commit style: `feat: expand demo app samples`, `feat(koast): dynamic positioning`, or `docs: ...`. Branch from `main` and keep changes focused. Describe the problem, approach, affected platforms, and validation; link relevant issues and include screenshots for UI changes. Discuss larger API changes in an issue first, and update README examples when public usage changes.
