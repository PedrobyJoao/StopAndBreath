# Stop And Breath

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

## Development Guidelines

- Aim for simplicity, both code and architecture wise. If the requirements
  can be fulfilled in an efficient way with a simple solution, prefer it
  rather than making it more complex
- Only useful tests: don't add duplicated and not meaningful tests.
- Security: the system must be secure. Don't write unsafe code. Be
  careful with user data and user's machine.
- Max 70 chars per line when writing (including spaces), also for docs
- When you finish your code edition, unless it's a trivial change,
  spawn two agents to review your code and tests (if added). If they
  find problems, discuss between you three, arrive at a conclusion,
  and fix the problem if there was really one. Then repeat the process
  spawning two new agents (not using the previous ones), and so on.
