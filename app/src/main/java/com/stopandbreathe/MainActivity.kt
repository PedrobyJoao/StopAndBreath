package com.stopandbreathe

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.WindowInsets
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var statusText: TextView

    private val refresh = object : Runnable {
        override fun run() {
            updateStatus()
            handler.postDelayed(
                this, ReminderConfig.UI_REFRESH_MILLIS,
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(24))
        }
        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(250, 250, 247))
            addView(content)
            setOnApplyWindowInsetsListener { view, insets ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val bars = insets.getInsets(
                        WindowInsets.Type.systemBars() or
                            WindowInsets.Type.displayCutout(),
                    )
                    view.setPadding(
                        bars.left, bars.top, bars.right, bars.bottom,
                    )
                } else {
                    @Suppress("DEPRECATION")
                    view.setPadding(
                        insets.systemWindowInsetLeft,
                        insets.systemWindowInsetTop,
                        insets.systemWindowInsetRight,
                        insets.systemWindowInsetBottom,
                    )
                }
                insets
            }
        }
        setContentView(scroll)
        scroll.requestApplyInsets()

        fun label(value: String, size: Float = 16f): TextView =
            TextView(this).apply {
                text = value
                textSize = size
                setTextColor(Color.rgb(30, 48, 42))
                setPadding(0, dp(8), 0, dp(12))
                content.addView(this)
            }

        label("Stop&Breathe", 32f)
        label(
            "A reminder every " +
                "${ReminderConfig.INTERVAL_MINUTES} minute(s) " +
                "of screen-on, unlocked time. Dismiss after " +
                "${ReminderConfig.COUNTDOWN_SECONDS} seconds.",
        )
        statusText = label("")
        label(
            "Enable Stop&Breathe in accessibility settings. " +
                "Reminders start automatically and return after " +
                "reboot once you unlock your phone. There are no " +
                "in-app Start or Stop controls.",
        )
        label(
            "Accessibility access displays a full-screen breathing " +
                "reminder, blocking ordinary touches until you can " +
                "dismiss it. The service does not read screen " +
                "content or collect or transmit personal data.",
        )
        content.addView(Button(this).apply {
            text = "Open accessibility settings"
            isAllCaps = false
            setOnClickListener {
                try {
                    val intent = Intent(
                        Settings.ACTION_ACCESSIBILITY_SETTINGS,
                    )
                    startActivity(intent)
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(
                        this@MainActivity,
                        "Accessibility settings are unavailable.",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
        }, LinearLayout.LayoutParams(-1, -2))
        label(
            "Timers pause while the screen is off or locked. " +
                "Dismiss stays visible but disabled during the " +
                "countdown. The next interval begins at dismissal. " +
                "Reboot or process termination resets all progress.",
        )
        label(
            "Blocking is best-effort: system controls, emergency " +
                "screens and notifications may remain available. " +
                "You can revoke accessibility access in Settings. " +
                "Android 8.0 or newer is required; behavior varies " +
                "across devices.",
        )
    }

    override fun onResume() {
        super.onResume()
        handler.removeCallbacks(refresh)
        refresh.run()
    }

    override fun onPause() {
        handler.removeCallbacks(refresh)
        super.onPause()
    }

    private fun accessibilityEnabled(): Boolean {
        val manager = getSystemService(
            AccessibilityManager::class.java,
        )
        val target = ComponentName(this, ReminderService::class.java)
        return manager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK,
        ).any { service ->
            val info = service.resolveInfo.serviceInfo
            ComponentName(info.packageName, info.name) == target
        }
    }

    private fun updateStatus() {
        val state = ReminderService.status
        statusText.text = when {
            !accessibilityEnabled() ->
                "Enable accessibility access to begin reminders."
            !state.connected ->
                "Access enabled — waiting for Android to connect."
            state.error != null -> state.error
            state.showing -> {
                val timer = "${state.countdownSeconds}s remaining."
                if (!state.active) "Reminder paused — $timer"
                else if (state.countdownSeconds > 0) {
                    "Breathing reminder active — $timer"
                } else "Breathing reminder ready to dismiss."
            }
            !state.active ->
                "Paused — waiting for the screen to be on " +
                    "and unlocked."
            else -> "Next reminder in ${state.remainingSeconds}s " +
                "of screen-on, unlocked time."
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
