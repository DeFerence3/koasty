# Koasty

> Lightweight, customizable toast-style notifications for Compose Multiplatform.

[![Kotlin](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/docs/multiplatform.html)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-4285F4?logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/compose-multiplatform/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

Koasty gives Compose Multiplatform applications a shared notification API across Android, iOS, desktop, and web. Show a short message from common code, let Koasty handle its lifetime and dismissal, and customize the visuals when your product needs a distinct style.

> [!NOTE]
> Koasty is in early development. The API may change before the first stable release. Feedback, bug reports, and contributions are welcome.

## Why Koasty?

- One Compose API for Android, iOS, JVM desktop, JavaScript, and Wasm.
- Timed, persistent, dismissible, and clickable notifications.
- Swipe-to-dismiss gestures and animated transitions.
- Custom colors, typography, shape, padding, elevation, and animation specs.
- Slots for a leading icon or a completely custom notification layout.
- Direct alert positioning with compact bottom-right defaults on desktop and web.
- No platform UI code required at the call site.

## Supported targets

| Platform | Target |
| --- | --- |
| Android | `android` |
| iOS devices | `iosArm64` |
| iOS Simulator on Apple Silicon | `iosSimulatorArm64` |
| Desktop | `jvm` |
| Web | `js` and `wasmJs` |

## Installation

Koasty is configured for publication under:

```kotlin
commonMain.dependencies {
    implementation("io.github.deference3:koasty:0.0.1")
}
```

If that version has not yet been published to Maven Central, include this repository as a project dependency while developing locally:

```kotlin
// settings.gradle.kts
include(":koasty")
```

```kotlin
// Your Compose Multiplatform module
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":koasty"))
        }
    }
}
```

## Quick start

Create and remember one `KoastManager`, then place `KoastyProvider` around your application content. The provider renders notifications above its content.

```kotlin
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.deference.koasty.KoastManager
import com.deference.koasty.KoastyProvider

@Composable
fun App() {
    val koastManager = remember { KoastManager() }

    KoastyProvider(koastManager = koastManager) {
        Button(
            onClick = {
                koastManager.show(
                    title = "Saved",
                    message = "Your changes are ready."
                )
            }
        ) {
            Text("Show koast")
        }
    }
}
```

## Notification lifetimes

The manager exposes focused methods for common notification behavior:

```kotlin
// Disappears automatically after 3.5 seconds by default.
koastManager.show("Uploaded", "Your image is now available.")

// Custom duration.
koastManager.show(
    title = "Connected",
    duration = 5.seconds //accepts kotlin Duration
)

// Remains until swiped away or dismissed in code.
koastManager.showUntilDismissed(
    title = "You're offline",
    message = "Some features may be unavailable."
)

// Can only be dismissed in code.
koastManager.showPersistent(
    title = "Uploading",
    message = "Please keep the app open."
)

koastManager.dismiss()
```

Clickable notifications are supported as well:

```kotlin
koastManager.showClickable(
    title = "Update available",
    message = "Tap to view the release notes.",
    onClick = { openReleaseNotes() }
)
```

## Customize the host

Use `KoastyHostConfig` to adapt the default notification to your design system:

```kotlin
KoastyProvider(
    koastManager = koastManager,
    config = KoastyHostConfig(
        containerColor = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 12.dp,
        horizontalSwipeEnabled = true,
        swipeUpEnabled = true
    )
) {
    AppContent()
}
```

You can also provide leading content while retaining Koasty's standard layout and behavior:

```kotlin
KoastyProvider(
    koastManager = koastManager,
    leadingContent = { koast ->
        Text(text = "●")
    }
) {
    AppContent()
}
```

For complete visual control, pass `koastContent` and render the current `KoastRequest` yourself.

## Run the sample applications

Clone the repository and use the included Gradle wrapper:

```shell
# Android
./gradlew :androidApp:assembleDebug

# Desktop
./gradlew :desktopApp:run

# Web (Wasm)
./gradlew :webApp:wasmJsBrowserDevelopmentRun

# Web (JavaScript)
./gradlew :webApp:jsBrowserDevelopmentRun
```

For iOS, open `iosApp/iosApp.xcodeproj` in Xcode and run the application from there.

On Windows, replace `./gradlew` with `gradlew.bat`.

## Build the library

```shell
./gradlew :koasty:build
```

Native Apple binaries must be built on macOS.

## Project structure

```text
koasty/      Compose Multiplatform library
shared/      Shared UI for the sample applications
androidApp/  Android sample entry point
desktopApp/  Desktop sample entry point
webApp/      JavaScript and Wasm sample entry points
iosApp/      iOS sample entry point
```

## Contributing

Contributions of all sizes are welcome.

1. Fork the repository and create a branch from `main`.
2. Keep changes focused and include tests when behavior changes.
3. Run the relevant Gradle build and tests locally.
4. Open a pull request describing the problem and your approach.

For larger API or behavior changes, open an issue first so the design can be discussed before implementation.

## License

Koasty is distributed under the Apache License 2.0. See the repository license for details.

---

Built with 💖 for [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) / [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) .
