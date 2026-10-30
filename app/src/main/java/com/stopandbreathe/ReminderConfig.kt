package com.stopandbreathe

object ReminderConfig {
    // Editable reminder defaults. Rebuild after changes.
    const val INTERVAL_MINUTES = 1L
    const val COUNTDOWN_SECONDS = 10L

    const val INTERVAL_MILLIS = INTERVAL_MINUTES * 60_000L
    const val COUNTDOWN_MILLIS = COUNTDOWN_SECONDS * 1_000L

    const val INTERVAL_CHECK_MILLIS = 1_000L
    const val COUNTDOWN_CHECK_MILLIS = 200L
    const val UI_REFRESH_MILLIS = 500L
    const val OVERLAY_RETRY_MILLIS = 5_000L

    const val MESSAGE_TEXT_SP = 28f
    const val COUNTDOWN_TEXT_SP = 18f

    // Duplicates are intentional placeholders; edit each entry later.
    val MESSAGES = listOf(
        "Breathing in, I'm mindful.\nBreathing out, I'm alive.",
        "Breathing in, I'm mindful.\nBreathing out, I'm alive.",
        "Breathing in, I'm mindful.\nBreathing out, I'm alive.",
    )
}
