# Stop And Breathe - Android 8.0+

Sometimes, we lose control of our smartphone usage. Suddenly, 1 hour has
passed and you have been just mindless scrolling through everywhere
and nowhere for no specific reason at all.

While your brain just sits there receiving non-stop information,
using your energy and time.

Sometimes we do have a reason, be work or some other responsability, but
still our brain needs some rest. Besides the fact that sometimes we jump
from our responsabilities to consume other unintended things: "Oh, this email
is boring. I'll read in a second, let me just check instagram for a while."

We already have some app blockers which may come handy but sometimes just not
flexible enough to our crazy routines. What if we could have just a reminder?
That block us for a few seconds, not for hours.

Stop And Breathe comes as a simple, gentle, but strict, reminder to breathe
and be aware of the present moment.

It breaks the spell of an infinite reward system that our smartphones
provide. It gives some rest to our precious brains.

After the reminder block timer elapses, you may just go back to what
you were doing. Or naturally, being more aware of your actions for a moment,
you might decide to do something else, or simply just breathe and be.

```
Breathing in, I'm aware of my mind and body
Breathing out, I take care of my mind and body
```

## How it works

After every 15 minutes of phone usage, a reminder will be shown with a
mindful message and a countdown timer with a dismissal button (initially
disabled). The dismissal button is only enabled after 20 seconds.

During this interval, you won't be able to change windows, check notifications
or close the reminder itself.

> In the next versions, we'll have different reminder levels (mini, long,
> rest) and they will be configurable by users

Time away from your phone helps reset the interval, partially or completely.

Some apps are considered as idle time, so using them won't add to your
accumulated screen time and reminders won't be shown (e.g.: maps,
banks, clock...).

### Permissons

I tried to use as few permissions as possible. Although, to get to
the level of strictness that I wanted, the app needed to be used as an
accessibility service.

Android doesn't have a very narrowed set of permissions for accessibility
services, so even though we're only displaying the reminder screen and
detecting window changes (for idle apps), Android will ask users to
provide super broad permissions which btw one of them is
literally set to `false` in the app configuration itself.

The permissions set by the app as an accessibility service are:

```
android:canRetrieveWindowContent="false"
android:accessibilityEventTypes="typeWindowStateChanged"
android:accessibilityFeedbackType="feedbackGeneric"
```

> See app configured permissions here: `./app/src/main/res/xml/reminder_accessibility_service.xml`

For the end user, Android will ask you to give the following
permissions to the app:

1. View and control screen: it can read screen content and display
   content over other apps
2. View and perform actions: it can track your interactions with an app
   or a hardware sensor, and interact with apps on your behalf

While literally in the app config, we set `android:canRetrieveWindowContent="false"`.

**So no, the app isn't configured to read your screen content!!!**

The app only needs ability to:

- Display over other apps for the reminder
- Track window changes for apps considered as idle time

You can read [How: overlay accessibility](./docs/how.md) and [Other possible
Hows](./docs/possible_hows.md) to see more.

## Security and data privacy

Besides the narrow permissions configured in the app as explained above,
being open source is already something towards supporting privacy and security.

But open sourcing is not enough, so we plan to have in the CI/CD some sec scanners
to identify vulnerabilities, and possibly, bad usage of user data.

It will come in the next version: v0.2

## Run locally

Requires Android 8.0+ (API 26) on a device or emulator.

### Standard setup

Install JDK 21 and the Android SDK (via Android Studio):

- Android SDK Platform 36
- Build Tools 36.0.0
- Platform Tools

Set `ANDROID_HOME` to your Android SDK directory.

### Nix setup

With Nix flakes enabled, enter the development shell:

```sh
nix develop
```

This provides Java, Android Studio, and the Android SDK.

### Build, run, and test

Run these from the project root, using either setup above:

```sh
# Build the debug APK
./gradlew assembleDebug

# Install on a running emulator or USB-debugging device
./gradlew installDebug

# Run unit tests
./gradlew testDebugUnitTest
```

### Features:

- [x] basic reminder functionality with a _non-annoying_ idle
      time tracking
- [x] maps apps are considered idle time. No
      reminder shall be shown while using maps apps!
- [ ] add more idle apps: banks, calculator, clock...
- [ ] automatic language detection
  - [ ] some different languages: english, pt-br, spanish...
- [ ] calendar: if you have any specific work to do, e.g.: meeting,
      in your cellphone, you can select a date and time to disable
      the reminder.
  - [ ] google calendar integration: events disable reminder
- [ ] reminder levels (mini, long and rest)
  - [ ] strictness levels for specific days
- [ ] EXTREME-STRICT option: save last reminder info when phone goes off,
      keep blocking on recovery
- [ ] feat: kids version (different messages)
- [ ] maybe allow users to choose if they want strict version
      with accessibility permission or less strict version without accessibility
      permission.

## License

Licensed under the [MIT License](LICENSE).
