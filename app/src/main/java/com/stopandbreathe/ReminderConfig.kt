package com.stopandbreathe

object ReminderConfig {
    // User facing config
    
    const val INTERVAL_MINUTES = 10L
    const val COUNTDOWN_SECONDS = 15L

    const val MESSAGE_TEXT_SP = 28f
    const val COUNTDOWN_TEXT_SP = 18f

    val MESSAGES = listOf(
        "Breathing in, I am aware of my in-breath.\nBreathing out, I am aware of my out-breath.",
        "Breathing in, I calm my body.\nBreathing out, I feel ease.",
        "Breathing in, I smile to my body.\nBreathing out, I release the tension in my body. ",
        "Breathing in, I am aware of the pain in me.\nBreathing out, I embrace the pain in me.",
        "Breathing in, I am aware of my feelings.\nBreathing out, I calm my feelings.",
        "Breathing in, I observe the impermanent nature of all things.\nBreathing out, I observe the non-desire nature of all things.",
        "Breathing in, I observe the unborn and undying nature of all things.\nBreathing out, I observe letting go.",
        "Breathing in, I dwell in the present moment.\nBreathing out, I know it is a wonderful moment.",
        "Breathing in, I have arrived.\nBreathing out, I am home.",
        "Breathing in, I acknowledge I'm gonna die one day.\nBreathing out, I am alive here and now.",
    )

    // CONSTANTS for internal implementation
    
    const val INTERVAL_MILLIS = INTERVAL_MINUTES * 60_000L
    const val COUNTDOWN_MILLIS = COUNTDOWN_SECONDS * 1_000L

    const val INTERVAL_CHECK_MILLIS = 1_000L
    const val COUNTDOWN_CHECK_MILLIS = 200L
    const val UI_REFRESH_MILLIS = 500L
    const val OVERLAY_RETRY_MILLIS = 5_000L

}
