# Stop&Breathe — initial Android prototype

A Kotlin Android app that displays a full-screen breathing reminder over ordinary
apps. The initial UI uses Android's built-in views without additional UI libraries.
Requires Android 8.0 (API 26) or newer.

## Run unit tests first

From your existing development shell (no Android Studio or emulator needed):

```sh
./gradlew :app:testDebugUnitTest --console=plain
```

The 11 JVM tests exercise the actual Kotlin timing logic: interval boundaries,
countdown rounding, early-dismissal prevention, stop protection, duplicate starts
and test requests, explicit dismissal, delayed reminders, and restart behavior.
They use simulated monotonic timestamps, so they do not wait for real timers.
The Android Gradle plugin still requires an installed SDK for project setup.
Gradle may download build and test dependencies on the first run.

The HTML report is at `app/build/reports/tests/testDebugUnitTest/index.html`.
These unit tests do not verify Android overlay rendering or system permissions;
those require the device checks below.

## Build and install

From this directory:

```sh
nix develop
./gradlew testDebugUnitTest lintDebug assembleDebug
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.stopandbreathe/.MainActivity
```

Alternatively, open this directory in Android Studio from the development shell,
let Gradle sync, select your device, and click Run. Initial builds need network
access to download the pinned Gradle distribution and Maven dependencies.

## Try it

1. Open **Stop&Breathe** and tap **Allow display over other apps**. Enable access for
   Stop&Breathe in Android Settings, then return. Some devices show an app list first.
2. Allow notifications when prompted through the app's notification button
   (recommended on Android 13+). Denying notifications does not grant the app an
   exception from Android's foreground-service requirements.
3. Tap **Test now** for an immediate reminder, or **Start reminders** to wait for
   the first interval. Test now also starts periodic reminders.
4. Try tapping Dismiss before the countdown ends: it is disabled. Press Home or
   switch to an ordinary app: the overlay should remain visible.
5. After 10 seconds, tap **Dismiss**. The next one-minute interval starts then.
6. Stop between reminders using the app or **Stop reminders** in its notification.

The test defaults live in
`app/src/main/java/com/stopandbreathe/ReminderConfig.kt`:

- `INTERVAL_MINUTES = 1`
- `COUNTDOWN_SECONDS = 10`

Change these constants and rebuild to adjust timing. Unit tests cover the
countdown, dismissal guard, repeat scheduling, and start/stop transitions.

### Intentional limitations

- This is an overlay, not kiosk mode. Android's system controls, permission
  revocation, force-stop, and reboot remain available, including during reminders.
- The app's own Stop controls cannot dismiss an active reminder. After the
  countdown, the overlay remains until you explicitly tap Dismiss.
- A user-started foreground service keeps reminders active with an ongoing
  notification. There is no automatic restart after reboot or process termination.
- Timing is approximate. There are no exact alarms or wake locks. Sleep/Doze and
  manufacturer battery restrictions can delay reminders; overdue reminders wait
  for the screen to be awake and unlocked rather than waking or unlocking it.
- Some sensitive apps and system screens deliberately hide overlays. Behavior
  must be checked on your actual phone, not just an emulator.
- The service declares Android's `specialUse` foreground-service type. Publishing
  on Google Play would require review of this use case and applicable policies;
  this prototype is intended for local installation and testing.

## Android development on NixOS

This flake provides Android Studio, JDK 21, Android API 36, build tools
36.0.0, SDK sources, adb, and an x86_64 Google APIs emulator image.
Kotlin support is included in Android Studio; native C/C++ tooling is omitted.

## Start

With Nix flakes enabled, run from this directory:

```sh
nix develop
android-studio
```

The first invocation generates `flake.lock`. Keep it in version control to pin
the environment. If you initialize a Git repository, stage `flake.nix` before
using the flake, since Nix ignores untracked files in Git repositories.

The flake explicitly permits unfree packages and accepts the Android SDK license.
Review these settings before evaluating it.

## Studio and project settings

- Use the existing SDK at the path printed by `echo "$ANDROID_HOME"` inside
  the development shell. Skip downloading another SDK in Studio's setup wizard.
- Set the project's Gradle JDK to the path printed by `echo "$JAVA_HOME"`
  (or select `JAVA_HOME` if Studio offers it). Studio itself uses its bundled runtime.
- Use the project's Gradle wrapper (`./gradlew`), not a global Gradle install.
  The project's Kotlin and Android Gradle plugin versions belong in its Gradle files.
- Choose `compileSdk = 36`. If the project explicitly specifies a build-tools
  version, use `36.0.0`, or add its required version to the flake.
- Create a virtual device in Device Manager using the installed API 36 Google
  APIs x86_64 image. Virtual device data stays in your writable home directory.

The SDK is read-only in the Nix store. Add SDK platforms, build tools, or images
in `flake.nix`, then re-enter `nix develop`; do not install them through Studio's
SDK Manager. Existing projects may need their local `sdk.dir` updated to
`ANDROID_HOME`. Do not commit machine-specific `local.properties` files.

Always launch Studio from the development shell so it inherits the environment.
The shell also configures the Nix-packaged `aapt2` for command-line Gradle builds:

```sh
./gradlew assembleDebug
adb devices
```

USB device permissions, KVM access for emulator acceleration, and Android app
permissions are left to you. A project-local flake cannot grant host permissions.

## Updates

```sh
nix flake update
nix develop
```

Review and commit the updated `flake.lock`. Dependency downloads made by Gradle
are separate from the Nix lock file.
