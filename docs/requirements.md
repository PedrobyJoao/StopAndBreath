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

- App MUST continue an already displayed reminder's countdown while
  the screen is off, without waking the phone or scheduling checks
- App MUST pause countdown while the screen is on but locked
- App MUST require explicit dismissal after unlock, even if countdown
  finished while the screen was off
- App MUST keep track of screen time (non-idle) *1 for the interval
  between reminders
- App MUST define defaults for `n` and `s`
- App MUST, on lock, show a message and the timer
  - App MUST show message bigger than timer (the message is the main focus)
  - App MUST select message randomly from a list hardcoded in the code
- App MUST not use root access
- App MAY use accessibility service

## *1. Non-idle

Non-idle means the screen is on AND unlocked AND no supported
map app is the active window. Idle means the screen is off OR locked
OR a supported map app is active. All usage inside that app is idle,
not only active navigation. Merely running maps in the background
does not qualify. The app does not detect actual user activity.

App detection uses accessibility window-change package metadata.
Screen-content access MUST stay disabled. No location permission,
usage-access permission or additional accessibility grant is needed.
App history MUST NOT be stored or transmitted. Detection is best
effort and depends on Android delivering window-change events.

Reminders MUST NOT cover an active supported map app. If a reminder
has already started, hide it while maps is active and continue its
countdown while unlocked, like screen-off behavior. A lit lock screen
still pauses the countdown. Restore the same reminder and message
when returning to ordinary apps; explicit dismissal is still required,
even if the countdown finished in maps.

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
Idle recovery MUST NOT reset the countdown of a displayed reminder.
Display failures MUST NOT be treated as phone idle time.
