package com.stopandbreathe

/** Last-known app classification, not authoritative window focus. */
internal class MapAppTracker(
    private val mapPackages: Set<String>,
    private val ownActivityClass: String,
) {
    enum class Classification { UNKNOWN, MAP, OTHER }

    var classification = Classification.UNKNOWN
        private set

    // Unknown keeps normal startup scheduling, not an exemption.
    val mapAppActive: Boolean
        get() = classification == Classification.MAP

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
        classification = if (app in mapPackages) {
            Classification.MAP
        } else {
            Classification.OTHER
        }
        return true
    }
}
