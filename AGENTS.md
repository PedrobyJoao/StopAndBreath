# Stop And Breath

This Android app provides periodic mindful-breathing reminders.

Every `n` minutes, it displays a full-screen popup window containing a mindful message.
The window cannot be dismissed through its own controls until `s` seconds have
elapsed and should remain visible over ordinary apps.

## Docs

- Software requirements: `requirements.md`
- Roadmap: `roadmap.md`

## Main code files

Under `app/src/main/java/com/stopandbreathe/`:

- `MainActivity.kt`: accessibility setup and reminder status.
- `ReminderService.kt`: automatic accessibility service and overlays.
- `ReminderSession.kt`: reminder timing and dismissal rules.
- `ReminderConfig.kt`: all editable reminder defaults and messages.

Android components and permissions are declared in
`app/src/main/AndroidManifest.xml`.

## Development Guidelines

- Aim for simplicity, both code and architecture wise. If the requirements
  can be fulfilled in an efficient way with a simple solution, prefer it
  rather than making it more complex
- Only useful tests: don't add duplicated and not meaningful tests.
- Security: the system must be secure. Don't write unsafe code. Be
  careful with user data and user's machine.
- Max 70 chars per line when writing (including spaces), also for docs
