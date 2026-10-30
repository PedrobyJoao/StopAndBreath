# Stop&Breathe — Android prototype

A Kotlin Android app with automatic full-screen breathing reminders.
Uses an accessibility overlay and Android's built-in views, without
additional UI libraries or root access.

## Android support

Requires **Android 8.0 (API 26) or newer**; targets Android 16 (API 36).
This is the build's compatibility range, not a device-certification
claim. Overlay priority, system gestures, notification-shade blocking,
and automatic service recovery vary by Android version and manufacturer.
These behaviors still require testing on real devices.

## Run unit tests first

From your development shell (no Android Studio or emulator needed):

```sh
./gradlew :app:testDebugUnitTest --console=plain
```

The JVM tests exercise active-time accumulation, pause/resume, interval
boundaries, countdown rounding, guarded and explicit dismissal, delayed
window display, duplicate display, and fresh-session resets. They use
simulated monotonic timestamps rather than waiting for real timers.
Gradle requires an installed SDK and may download dependencies.

The HTML report is under `app/build/reports/tests/testDebugUnitTest/`.
Unit tests do not verify Android windows, permissions, or reboot
reconnection; those require the device checks below.

## Build and install

From this directory:

```sh
nix develop
./gradlew testDebugUnitTest lintDebug assembleDebug
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.stopandbreathe/.MainActivity
```

Alternatively, open this directory in Android Studio from the
development shell, sync Gradle, select your device, and click Run.
Initial builds need network access to download dependencies.

## Try it

1. Open **Stop&Breathe**, read the accessibility disclosure, and tap
   **Open accessibility settings**. Enable the Stop&Breathe service.
   Android 13+ may require allowing restricted settings in App info for
   sideloaded apps before accessibility can be enabled.
2. Reminders start automatically. There are no Start, Stop, or Test
   controls. No notification or display-over-other-apps permission is
   needed. Keep the phone on and unlocked for one minute.
3. The reminder shows a randomly selected message, a smaller countdown,
   and a visible but disabled **Dismiss** button. Try Back, ordinary
   app switching, notification-shade swipes, and heads-up notifications
   with both gesture and button navigation. Blocking is best-effort;
   record device-specific escapes rather than assuming kiosk behavior.
4. Turn the screen off partway through the countdown. After unlocking,
   the same message and remaining countdown should resume. Locked
   screen-on time must not advance either timer.
5. After ten active seconds, Dismiss becomes clickable. The reminder
   stays until you dismiss it, then a fresh one-minute interval begins.
6. Between reminders, turn the screen off or leave it on the lock
   screen. Neither period should count toward the next reminder.
7. Reboot and unlock. Android should reconnect the enabled service
   automatically, without opening the app. The interval starts fresh.
   Process termination also loses progress; system reconnection can
   take time. Force-stop is a separate system escape, not a supported
   restart mechanism.
8. Disable accessibility access in Android Settings to opt out. Confirm
   that any overlay disappears. Re-enabling starts a fresh interval.

All editable reminder defaults live in
`app/src/main/java/com/stopandbreathe/ReminderConfig.kt`:

- `INTERVAL_MINUTES = 1`
- `COUNTDOWN_SECONDS = 10`
- `MESSAGES`: randomly selected hardcoded entries. Identical entries
  are intentional placeholders for later editing.
- Timing-update intervals and message/countdown text sizes.

Edit this file and rebuild to change defaults. Nothing is persisted:
reboot, process termination, or service reconnection resets progress.

### Intentional limitations

- This is an accessibility overlay, not kiosk mode. It blocks ordinary
  interaction best-effort, but cannot guarantee preventing notification
  access, system navigation, emergency controls, or permission changes.
- Both timers count only screen-on, unlocked time. The overlay is
  removed while off or locked, then restored with its paused countdown.
  The app never wakes or unlocks the device.
- Android manages the accessibility service and normally reconnects it
  after reboot/unlock or process death while enabled. No boot receiver
  or foreground service is needed. Force-stop, revoked access, and OEM
  restrictions can prevent or delay recovery.
- The service does not subscribe to accessibility events, retrieve
  window content, request key filtering or gesture injection, or use
  network/storage permissions. Timing and messages remain in memory.
- Google Play distribution requires accessibility-policy review and
  appropriate disclosure/consent. This is not declared an accessibility
  tool for users with disabilities. Local installation is the current
  development workflow.

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
