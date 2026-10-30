package com.stopandbreathe

/** In-memory timing; callers supply monotonic milliseconds. */
class ReminderSession(
    private val intervalMillis: Long,
    private val countdownMillis: Long,
) {
    init {
        require(intervalMillis > 0)
        require(countdownMillis > 0)
    }

    private var updatedAt: Long? = null

    var active = false
        private set
    var intervalRemainingMillis = intervalMillis
        private set
    var countdownRemainingMillis: Long? = null
        private set

    val showing: Boolean get() = countdownRemainingMillis != null
    val isDue: Boolean
        get() = active && !showing && intervalRemainingMillis == 0L
    val intervalSeconds: Long
        get() = seconds(intervalRemainingMillis)
    val countdownSeconds: Long
        get() = seconds(countdownRemainingMillis ?: 0L)

    /** Account for the old state before a screen transition. */
    fun update(now: Long, active: Boolean) {
        val elapsed = updatedAt?.let { now - it } ?: 0L
        require(elapsed >= 0)
        if (this.active) {
            val remaining = countdownRemainingMillis
            if (remaining != null) {
                countdownRemainingMillis =
                    (remaining - elapsed).coerceAtLeast(0)
            } else {
                intervalRemainingMillis = (intervalRemainingMillis -
                    elapsed).coerceAtLeast(0)
            }
        }
        updatedAt = now
        this.active = active
    }

    /** Call only after the window has been added successfully. */
    fun show(now: Long) {
        update(now, active)
        if (showing) return
        check(isDue)
        countdownRemainingMillis = countdownMillis
    }

    fun dismiss(now: Long): Boolean {
        update(now, active)
        if (!active || countdownRemainingMillis != 0L) return false
        countdownRemainingMillis = null
        intervalRemainingMillis = intervalMillis
        return true
    }

    /** A lit lock screen must keep checking for a missed unlock. */
    fun nextUpdateDelayMillis(screenOn: Boolean): Long? = when {
        !screenOn -> null
        !active -> ReminderConfig.INTERVAL_CHECK_MILLIS
        showing -> ReminderConfig.COUNTDOWN_CHECK_MILLIS
        else -> minOf(
            ReminderConfig.INTERVAL_CHECK_MILLIS,
            intervalRemainingMillis.coerceAtLeast(1),
        )
    }

    private fun seconds(millis: Long): Long =
        millis / 1_000L + if (millis % 1_000L == 0L) 0L else 1L
}
