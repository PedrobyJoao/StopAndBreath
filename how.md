# Stop&Breathe: enforcement options

Screen pinning is excluded because each new session requires system
confirmation. None of the options below requires root; untested
capabilities need device validation.

## 1. Ordinary full-screen overlay — current prototype

1. **How it works:** Displays a `TYPE_APPLICATION_OVERLAY` window over
   ordinary apps, with dismissal disabled until the countdown ends.
2. **Pros:** Simple; works automatically after setup; survives
   ordinary app switching visually.
3. **Limitations:** Notifications and system controls appear above it.
   Does not meet the notification-blocking requirement.
4. **Permissions:** Display over other apps; foreground-service
   permissions for the background timer. Notification permission is
   recommended.

## 2. Managed-device kiosk — Lock Task Mode

1. **How it works:** A device-policy controller allowlists the
   breathing Activity for managed Lock Task Mode, restricting
   navigation and the notification shade.
2. **Pros:** Genuine system-enforced restrictions; no per-session
   pinning confirmation after provisioning.
3. **Limitations:** Requires managed-device provisioning, usually
   device-owner setup; an existing personal phone may need a factory
   reset. Intrusive and unsuitable for ordinary consumer installation.
4. **Permissions:** Device-owner or another supported management
   configuration, plus lock-task allowlisting—not a normal permission
   dialog.

## 3. Full-screen Activity + accessibility monitoring

1. **How it works:** Shows a normal breathing screen. An Accessibility
   Service detects departures, brings it back, and dismisses the
   notification shade.
2. **Pros:** No overlay or managed-device enrollment; no per-reminder
   confirmation.
3. **Limitations:** Reactive: another screen or notification can
   become visible before correction. Not a reliable prevention
   boundary; OEM behavior and Play policy matter.
4. **Permissions:** User enables the Accessibility Service in
   Settings. Window-content access is needed only if the chosen
   detection method requires it.

## 4. Accessibility overlay — higher-priority touch barrier

1. **How it works:** An Accessibility Service displays a
   `TYPE_ACCESSIBILITY_OVERLAY` above the notification shade and
   intercepts touches during the countdown.
2. **Pros:** Stronger than the current overlay; can cover
   notifications and block ordinary interaction; no per-reminder
   confirmation.
   Screen-content reading is unnecessary for the barrier itself.
3. **Limitations:** Still an overlay. System escapes, lock-screen
   behavior, navigation modes, and OEM differences need testing.
   Accessibility use requires Play-policy review.
4. **Permissions:** User enables the Accessibility Service; separate
   display-over-other-apps permission is not required for this window
   type.

## 5. Accessibility input blocking — best next experiment

1. **How it works:** On Android 14+, subscribes to touchscreen input
   using `setMotionEventSources()`. Consumed events do not reach
   ordinary apps or touch-based system navigation. A normal Activity
   displays the countdown; input is restored afterward.
2. **Pros:** Preventive rather than reactive; no overlay, kiosk setup,
   or per-reminder confirmation.
3. **Limitations:** Blocks our own screen's touches too. Requires
   timed release and an accessible emergency escape. Touch exploration
   such as TalkBack conflicts; hardware input and system-driven
   windows remain separate. Does not suppress incoming notification
   banners. Device testing and Play-policy review are essential.
4. **Permissions:** User enables the Accessibility Service once.
   Optional Do Not Disturb policy access can suppress ordinary
   notification visuals; key filtering needs separately declared
   accessibility capabilities if used.

## 6. Shizuku/ADB privileged helper — stronger, harder setup

1. **How it works:** A helper running with shell privileges uses
   system-disable controls for Home, Recents, status-bar expansion,
   and Quick Settings while a normal Activity displays the countdown.
2. **Pros:** Genuine restriction controls without an overlay, root,
   or device-owner enrollment.
3. **Limitations:** Not an ordinary consumer-app capability. Requires
   debugging setup and a running privileged helper; reboot behavior,
   OEM compatibility, and reliable restoration must be validated.
   Relies on non-public app interfaces.
4. **Permissions:** USB/wireless-debugging authorization; Shizuku
   authorization if using Shizuku. These are privileged setup steps,
   not normal Android runtime permissions.

Accessibility-based options need clear disclosure and consent. They
must not prevent users from disabling the service contrary to Google
Play policy. Sideloaded installations may also require
**Allow restricted settings** before service activation.
