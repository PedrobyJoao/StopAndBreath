package com.stopandbreathe

object ReminderConfig {
    // Short defaults for device testing. Change these before everyday use.
    const val INTERVAL_MINUTES = 1L
    const val COUNTDOWN_SECONDS = 10L

    const val INTERVAL_MILLIS = INTERVAL_MINUTES * 60_000L
    const val COUNTDOWN_MILLIS = COUNTDOWN_SECONDS * 1_000L
}
