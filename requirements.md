Simple human written requirements for the app.

This Android app provides periodic mindful-breathing reminders.
Working by every `n` minutes, displaying a full-screen popup window containing a mindful message,
locking its screen for `s` seconds.

`n` minutes: the number of minutes between reminders.
`s` seconds: the number of seconds the popup window should remain blocking.

## Requirements:

Functional:

- App MUST starts automatically while phone is on
- App MUST lock popup window until defined timer elapses
  - User MUST not be able to close the window
  - User MUST not be able to scroll through notifications
  - User MAY not be able to see notifications banner popups
- App MUST have a close button that becomes clickable
  only after timer have elapsed
- App MUST NOT preserve accumulated screen time counter
  if the phone is turned off or the process killed

Non-functional requirements:

- App MUST pause countdown if screen turns off in the middle of the
  reminder
- App MUST keep track of screen time (non-idle) *1 for the interval
  between reminders
- App MUST define defaults for `n` and `s`
- App MUST, on lock, show a message and the timer
  - App MUST show message bigger than timer (the message is the main focus)
  - App MUST select message randomly from a list hardcoded in the code
- App MUST not use root access
- App MAY use accessibility service

*1: Non-idle means the screen is on AND unlocked.
Idle means the screen is off OR locked.
The app does not attempt to detect actual user activity.

During each uninterrupted idle period, accumulated screen time MUST
decrease proportionally to the idle duration and configured interval:

```text
accumulatedAfter =
    accumulatedBefore × max(0, 1 − idleDuration / n)
```

Here, accumulatedBefore is the value at the start of that idle period.
idleDuration and n MUST use the same time unit.
An idle period of n minutes or longer MUST clear accumulated time.
Shorter idle periods MUST clear the corresponding fraction.
Recovery MUST NOT depend on how often the app checks the timer.

Recovery applies only to the interval between reminders, including
a due reminder that has not yet been displayed.
An already displayed reminder MUST retain its paused countdown.
Display failures MUST NOT be treated as phone idle time.
