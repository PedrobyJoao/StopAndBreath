# StopAndBreathe

This Android app provides periodic mindful-breathing reminders.

Every `n` minutes, it displays a full-screen popup window containing a mindful message.
The window cannot be dismissed through its own controls until `s` seconds have
elapsed and should remain visible over ordinary apps.

## Docs

- Software requirements: `docs/requirements.md`
- Roadmap: `docs/roadmap.md`

## Main code files

Under `app/src/main/java/com/stopandbreathe/`:

- `MainActivity.kt`: accessibility setup and reminder status.
- `ReminderService.kt`: automatic accessibility service and overlays.
- `ReminderSession.kt`: reminder timing and dismissal rules.
- `ReminderConfig.kt`: all editable reminder defaults and messages.

Android components and permissions are declared in
`app/src/main/AndroidManifest.xml`.
