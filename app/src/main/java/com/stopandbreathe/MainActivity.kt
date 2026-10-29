package com.stopandbreathe

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var statusText: TextView
    private lateinit var overlayButton: Button
    private lateinit var notificationButton: Button
    private lateinit var startButton: Button
    private lateinit var testButton: Button
    private lateinit var stopButton: Button

    private val refresh = object : Runnable {
        override fun run() {
            updateStatus()
            handler.postDelayed(this, 500)
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
            // API 35+ enforces edge-to-edge; keep controls clear of system bars.
            setOnApplyWindowInsetsListener { view, insets ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val bars = insets.getInsets(
                        android.view.WindowInsets.Type.systemBars() or
                            android.view.WindowInsets.Type.displayCutout(),
                    )
                    view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
                } else {
                    @Suppress("DEPRECATION")
                    view.setPadding(
                        insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                        insets.systemWindowInsetRight, insets.systemWindowInsetBottom,
                    )
                }
                insets
            }
        }
        setContentView(scroll)
        scroll.requestApplyInsets()

        fun label(value: String, size: Float = 16f): TextView = TextView(this).apply {
            text = value
            textSize = size
            setTextColor(Color.rgb(30, 48, 42))
            setPadding(0, dp(8), 0, dp(12))
            content.addView(this)
        }

        fun button(value: String, action: () -> Unit): Button = Button(this).apply {
            text = value
            isAllCaps = false
            setOnClickListener { action() }
            content.addView(this, LinearLayout.LayoutParams(-1, -2))
        }

        label("Stop&Breathe", 32f)
        label(
            "A reminder every ${ReminderConfig.INTERVAL_MINUTES} minute(s). " +
                "Dismiss after ${ReminderConfig.COUNTDOWN_SECONDS} seconds.",
        )
        statusText = label("")
        overlayButton = button("1. Allow display over other apps") {
            try {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            } catch (_: android.content.ActivityNotFoundException) {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }
        notificationButton = button("2. Allow notifications (recommended)") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            } else {
                startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName))
            }
        }
        startButton = button("Start reminders") { sendCommand(ReminderService.ACTION_START) }
        testButton = button("Test now") { sendCommand(ReminderService.ACTION_TEST) }
        stopButton = button("Stop reminders") { sendCommand(ReminderService.ACTION_STOP) }
        label(
            "Test now also starts periodic reminders. During the countdown, press Home " +
                "or switch apps to check that the reminder stays visible.\n\n" +
                "The next interval begins when you dismiss a reminder. Timing is approximate; " +
                "a sleeping or locked phone waits until you wake and unlock it.\n\n" +
                "This does not lock your device. Android's system controls can still stop the app. " +
                "Reminders do not restart automatically after a reboot or process termination.",
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

    private fun sendCommand(action: String) {
        if (action != ReminderService.ACTION_STOP && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Allow display over other apps first.", Toast.LENGTH_LONG).show()
            return
        }
        try {
            val intent = Intent(this, ReminderService::class.java).setAction(action)
            if (action == ReminderService.ACTION_STOP) startService(intent)
            else startForegroundService(intent)
        } catch (error: RuntimeException) {
            Toast.makeText(this, "Could not start reminders: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateStatus() {
        val state = ReminderService.status
        val overlayAllowed = Settings.canDrawOverlays(this)
        val notificationsAllowed = getSystemService(NotificationManager::class.java).areNotificationsEnabled()
        overlayButton.isEnabled = !overlayAllowed
        overlayButton.text = if (overlayAllowed) "Overlay permission granted" else "1. Allow display over other apps"
        notificationButton.text = if (notificationsAllowed) "Notifications enabled" else "2. Allow notifications (recommended)"
        notificationButton.visibility = if (notificationsAllowed) View.GONE else View.VISIBLE
        startButton.isEnabled = overlayAllowed && !state.running
        testButton.isEnabled = overlayAllowed && !state.showing
        stopButton.isEnabled = state.running && !state.showing
        statusText.text = when {
            state.error != null -> state.error
            state.showing -> "Breathing reminder active. Dismiss it before stopping."
            state.running -> {
                val remaining = ((state.nextReminderAt - SystemClock.elapsedRealtime()).coerceAtLeast(0) + 999) / 1_000
                if (remaining > 0) "Running — next reminder in ${remaining}s."
                else "Running — waiting for the phone to be awake and unlocked."
            }
            else -> "Stopped"
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
