# Rally Club — native Android badminton

A complete Kotlin Android Studio project. The game uses Android Canvas and native touch input, with no WebView, game engine, runtime library dependencies, account, or network permission.

## Open and run

1. Extract the project ZIP, then open the **BadmintonRally** folder in Android Studio (the folder containing `settings.gradle.kts`).
2. Use **JDK 17 or 21** as the Gradle JDK in Settings → Build, Execution, Deployment → Build Tools → Gradle. If your Studio bundles a newer JDK, select/download JDK 21 here.
3. Allow Gradle sync and install **Android SDK Platform 35** and **Build Tools 35.0.0** if prompted. Initial sync needs internet access.
4. Select an Android 6.0+ emulator or USB-connected phone and press **Run**.

Build tools are pinned to Android Gradle Plugin 8.9.2, Gradle 8.11.1, and Kotlin 2.1.20. The Gradle wrapper is included. No machine-specific SDK path is included; Android Studio creates `local.properties` for your computer.

## Play

- Choose Easy, Club, or Pro, then tap **Let's play**.
- Tap **Serve** to start each rally.
- Drag anywhere on the lower court to move sideways. You can hold one finger to move and use another to hit.
- Follow the lime landing marker. When the shuttle is near you and its ring lights up, tap **Clear** for a high return or **Smash** for a fast return. You must be within racket reach.
- Shots aim crosscourt based on your position. Clears buy time; smashes give the AI less time to reach the shuttle.
- First to 11, win by two, with a cap at 15. This is an arcade singles game: the player serves every rally; scoring is intentionally simplified.
- Tap the upper-right pause button to pause. Leaving the app pauses the match; tap Resume after returning.
- The best rally persists locally. A match itself is held in memory and resets if Android destroys the process.

## Code and checks

- `RallyEngine.kt`: frame-independent movement, shot timing, opponent movement, scoring, and match state.
- `RallyView.kt`: responsive portrait court, Canvas graphics, multi-touch input, haptics, and best-rally storage.
- `MainActivity.kt`: Activity lifecycle.
- `RallyEngineTest.kt`: pause, timing, successful returns, and match/reset tests.

From a terminal with JDK 17/21 and Android SDK configured:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug
```

On macOS/Linux, run `chmod +x gradlew`, then `./gradlew testDebugUnitTest assembleDebug lintDebug`.

This prototype is optimized for portrait touch play. Canvas controls do not expose separate TalkBack actions. No sound assets are required.

## Validation in the creation environment

All Kotlin source files compiled successfully against Android Studio's bundled Android layout library. All four JUnit game-logic tests passed. This is a source-level check, not a substitute for an Android build or device test.

The Gradle project configured successfully, but APK assembly and Android lint could not run because the installed Android SDK returned filesystem access-denied errors. No APK is included and device gameplay has not been verified. Run the Gradle command above after opening the project in Android Studio with an accessible SDK.

Build compatibility reference: [Android Gradle Plugin 8.9 release notes](https://developer.android.com/build/releases/agp-8-9-0-release-notes).
