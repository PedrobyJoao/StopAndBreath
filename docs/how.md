## 1. Accessibility overlay — higher-priority touch barrier

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
   Android may still show a broad screen-reading warning: there is no
   separate overlay-only accessibility permission. Our service sets
   `android:canRetrieveWindowContent="false"` and subscribes to no
   accessibility events. Screen-on/unlocked timing uses ordinary
   Android APIs, not screen content or app-usage history.
