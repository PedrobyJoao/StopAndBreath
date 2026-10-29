# Stop And Breath

This Android app provides periodic mindful-breathing reminders.
Every `n` minutes, it displays a full-screen overlay containing a mindful message.
The overlay cannot be dismissed through its own controls until `s` seconds have
elapsed and should remain visible over ordinary apps.

## Requirements:

- User can NOT dismiss the popup until timer have elapsed.
- User can NOT see notifications (from notifications dropdown) until timer have elapsed.
- The tool shall NOT use root access

## Technology

- Kotlin with native Android APIs.
- Android Studio
- A foreground service with an ongoing notification for background operation.

## Development Guidelines

- Keep implementation simple and avoid unnecessary dependencies.
- Don't create unecessary tests that actually don't test anything new. Don't duplicate tests.
  I like simple and meaningful tests
- Max 70 chars per line when writing (including spaces), also for docs
