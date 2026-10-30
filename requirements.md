Simple human written requirements for the app.

This Android app provides periodic mindful-breathing reminders.
Working by every `n` minutes, displaying a full-screen popup window containing a mindful message,
locking its screen for `s` seconds.

`n` minutes: the number of minutes between reminders.
`s` seconds: the number of seconds the popup window should remain blocking.

## Requirements:

Functional:

- App MUST lock popup window until defined timer elapses
  - User MUST not be able to close the window
  - User MUST not be able to scroll through notifications
  - User MAY not be able to see notifications banner popups
- App MUST have a close button that becomes clickable
  only after timer have elapsed

Non-functional requirements:

- App MUST keep track of screen time (non-idle) *1 for the interval
  between reminders
- App MUST define defaults for `n` and `s`
- App MUST, on lock, show a message and the timer
  - App MUST show message bigger than timer (the message is the main focus)
  - App MUST select message randomly from a list hardcoded in the code
- App MUST not use root access
- App MAY use accessibility service

*1: Non-idle will be determined by screen being on. So the app doesn't have
to try to detect user activity.
