package com.stopandbreathe

enum class ScreenState {
    OFF,
    ON_AND_LOCKED, // Screen On and phone Locked
    ON_AND_UNLOCKED, // Screen On and phone Unlocked
}

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
    private var idleSince: Long? = null
    private var idleAccumulatedMillis = 0L
    private var countdownPaused = false

    var screenState = ScreenState.ON_AND_LOCKED
        private set
    var intervalRemainingMillis = intervalMillis
        private set
    var countdownRemainingMillis: Long? = null
        private set

    val showing: Boolean get() = countdownRemainingMillis != null
    
    // Ready to show: unlocked, interval elapsed, no current reminder.
    val isDue: Boolean
        get() = screenState == ScreenState.ON_AND_UNLOCKED &&
            !showing && intervalRemainingMillis == 0L
    val remainingSeconds: Long
        get() = seconds(intervalRemainingMillis)
    val countdownSeconds: Long
        get() = seconds(countdownRemainingMillis ?: 0L)

    /** Account for the old state before a screen transition. */
    fun update(
        now: Long,
        screenState: ScreenState,
        countdownPaused: Boolean = false,
    ) {
        val elapsed = updatedAt?.let { now - it } ?: 0L
        require(elapsed >= 0)
        val remaining = countdownRemainingMillis
        if (remaining != null) {
            if (this.screenState != ScreenState.ON_AND_LOCKED &&
                !this.countdownPaused
            ) {
                countdownRemainingMillis =
                    (remaining - elapsed).coerceAtLeast(0)
            }
        } else if (this.screenState == ScreenState.ON_AND_UNLOCKED) {
            intervalRemainingMillis = (intervalRemainingMillis -
                elapsed).coerceAtLeast(0)
        } else {
            idleSince?.let { start ->
                val idleMillis = now - start
                // Use the original snapshot, not each polled value.
                val recovered = if (idleMillis >= intervalMillis) {
                    idleAccumulatedMillis
                } else {
                    (idleAccumulatedMillis.toDouble() *
                        idleMillis / intervalMillis).toLong()
                }
                intervalRemainingMillis = intervalMillis -
                    idleAccumulatedMillis + recovered
            }
        }
        if (screenState == ScreenState.ON_AND_UNLOCKED || showing) {
            idleSince = null
        } else if (idleSince == null) {
            idleSince = now
            idleAccumulatedMillis =
                intervalMillis - intervalRemainingMillis
        }
        updatedAt = now
        this.screenState = screenState
        this.countdownPaused = countdownPaused
    }

    /** Call only after the window has been added successfully. */
    fun show(now: Long) {
        update(now, screenState)
        if (showing) return
        check(isDue)
        countdownRemainingMillis = countdownMillis
    }

    fun dismiss(now: Long): Boolean {
        update(now, screenState)
        if (screenState != ScreenState.ON_AND_UNLOCKED ||
            countdownRemainingMillis != 0L
        ) return false
        countdownRemainingMillis = null
        intervalRemainingMillis = intervalMillis
        return true
    }

    /** A lit lock screen must keep checking for a missed unlock. */
    fun nextUpdateDelayMillis(): Long? = when (screenState) {
        ScreenState.OFF -> null
        ScreenState.ON_AND_LOCKED -> ReminderConfig.INTERVAL_CHECK_MILLIS
        ScreenState.ON_AND_UNLOCKED -> if (showing) {
            ReminderConfig.COUNTDOWN_CHECK_MILLIS
        } else {
            minOf(
                ReminderConfig.INTERVAL_CHECK_MILLIS,
                intervalRemainingMillis.coerceAtLeast(1),
            )
        }
    }

    private fun seconds(millis: Long): Long =
        millis / 1_000L + if (millis % 1_000L == 0L) 0L else 1L
}
