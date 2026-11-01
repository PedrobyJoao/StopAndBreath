# Map-app idle time

`ReminderConfig.MAP_APP_PACKAGES` is the editable list of supported
Android package IDs. It covers general maps, road navigation,
open-source maps, hiking, trekking and cycling. All usage inside a
listed app counts as idle while it is the active window. Keeping it
open in the background does not make other apps idle.

The list uses exact matches, never labels, substrings or prefixes.
Include each store/paid variant separately. Check IDs against the
app's official download links, Google Play's `id` URL parameter, or
F-Droid's `/packages/` URL. Some older IDs remain for installed apps
that are no longer listed in stores. The list cannot cover every app;
add verified IDs to the config rather than adding detection rules.

Useful package sources:

- <https://wiki.openstreetmap.org/wiki/Android#Navigation>
- <https://f-droid.org/packages/>
- <https://play.google.com/store/apps>

## Permissions and privacy

The existing accessibility service subscribes only to
`typeWindowStateChanged`, configured in
`app/src/main/res/xml/reminder_accessibility_service.xml`.
`canRetrieveWindowContent` remains `false` there. The service examines
package/class metadata, not screen text, routes or location. No new
permissions are added, and no app history is saved or transmitted.

`MapAppTracker` keeps metadata filtering outside reminder timing.
Only its last-known `UNKNOWN`, `MAP` or `OTHER` classification
is cached in process memory. Service cleanup and reconnection reset
the reminder session, but retain that estimate while the process
survives. Process termination clears it. Unknown follows normal
startup scheduling; it does not suppress reminders indefinitely.

This cache avoids discarding an observation on reconnect.

Keyboard windows do not end map idle time. Our reminder overlay's
own window events do not change the detected app. Detection is best
effort: Android must deliver the relevant window-change events. It
does not inspect navigation notifications or exempt background maps,
Android Auto, unfocused split-screen panes or picture-in-picture
solely because maps is visible.
