package com.stopandbreathe

/** Last-known map estimate, not authoritative window focus. */
internal class MapAppTracker(
    private val mapPackages: Set<String>,
    private val ownActivityClass: String,
) {
    // Default to normal startup scheduling, not a map exemption.
    var mapAppActive = false
        private set

    /** Use metadata only; retain no event history. */
    fun onWindowChanged(
        app: String?,
        className: String?,
        ownPackage: String,
        isKeyboardPackage: Boolean,
    ): Boolean {
        if (app.isNullOrBlank()) return false
        // Ignore delayed overlay events, even after window removal.
        // Our real activity still counts as returning to this app.
        if (app == ownPackage && className != ownActivityClass) {
            return false
        }
        // Preserve map idle time while typing. This package-level
        // heuristic also ignores keyboard settings activities.
        if (isKeyboardPackage) return false
        mapAppActive = app in mapPackages
        return true
    }
}
