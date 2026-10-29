package com.stopandbreathe

/** Timing only; callers supply monotonic elapsed-realtime milliseconds. */
class ReminderSession(private val intervalMillis: Long, private val countdownMillis: Long) {
    init {
        require(intervalMillis > 0)
        require(countdownMillis > 0)
    }

    var running = false
        private set
    var nextReminderAt: Long? = null
        private set
    var dismissAvailableAt: Long? = null
        private set

    val showing: Boolean get() = dismissAvailableAt != null

    fun start(now: Long) {
        if (running) return
        running = true
        nextReminderAt = now + intervalMillis
    }

    fun isDue(now: Long): Boolean =
        running && !showing && nextReminderAt?.let { now >= it } == true

    fun show(now: Long) {
        check(running)
        if (showing) return
        nextReminderAt = null
        dismissAvailableAt = now + countdownMillis
    }

    fun secondsRemaining(now: Long): Long {
        val deadline = dismissAvailableAt ?: return 0
        return ((deadline - now).coerceAtLeast(0) + 999L) / 1_000L
    }

    fun dismiss(now: Long): Boolean {
        val deadline = dismissAvailableAt ?: return false
        if (now < deadline) return false
        dismissAvailableAt = null
        nextReminderAt = now + intervalMillis
        return true
    }

    fun stop(): Boolean {
        // The app's own controls cannot bypass an active reminder.
        if (showing) return false
        running = false
        nextReminderAt = null
        return true
    }
}
