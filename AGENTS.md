# Project Context

This Android app provides periodic mindful-breathing reminders.
Every `n` minutes, it displays a full-screen overlay containing a mindful message.
The overlay cannot be dismissed through its own controls until `s` seconds have
elapsed and should remain visible over ordinary apps. Android’s system-level
escape routes remain available; this is not a device-locking or kiosk app.

## Technology

- Kotlin with native Android APIs.
- Android Studio
- A foreground service with an ongoing notification for background operation.
- Android overlay permission for displaying reminders over other apps.

## Development Guidelines

- Keep implementation simple and avoid unnecessary dependencies.
- Don't create unecessary tests that actually don't test anything new. Don't duplicate tests.
  I like simple and meaningful tests
