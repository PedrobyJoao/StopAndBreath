package com.stopandbreathe

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val repository =
        "https://github.com/PedrobyJoao/StopAndBreath"
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var statusText: TextView
    private lateinit var enableButton: Button

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
        setContentView(R.layout.activity_main)
        val scroll = findViewById<ScrollView>(R.id.main_scroll)
        scroll.setOnApplyWindowInsetsListener { view, insets ->
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
        scroll.requestApplyInsets()

        findViewById<TextView>(R.id.introduction).text = getString(
            R.string.introduction,
            ReminderConfig.INTERVAL_MINUTES,
            ReminderConfig.COUNTDOWN_SECONDS,
        )
        statusText = findViewById(R.id.reminder_status)
        enableButton = findViewById<Button>(R.id.enable_accessibility)
            .apply { setOnClickListener { showSetup() } }
        findViewById<View>(R.id.manage_accessibility)
            .setOnClickListener {
                if (accessibilityEnabled()) openSettings()
                else showSetup()
            }
        findViewById<View>(R.id.security_details)
            .setOnClickListener { showSecurity() }
        link(findViewById(R.id.report_link), "$repository/issues/new")
        @Suppress("DEPRECATION")
        val version = packageManager.getPackageInfo(packageName, 0)
            .versionName.orEmpty()
        findViewById<TextView>(R.id.app_version).text = getString(
            R.string.app_version, version,
        )
    }

    private fun showSetup() {
        AlertDialog.Builder(this)
            .setTitle(R.string.setup_title)
            .setMessage(R.string.setup_steps)
            .setNegativeButton(R.string.not_now, null)
            .setPositiveButton(R.string.open_settings) { _, _ ->
                openSettings()
            }
            .show()
    }

    private fun showSecurity() {
        val dialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_security)
            .setPositiveButton(R.string.close_dialog, null)
            .show()
        dialog.window?.setTitle(getString(R.string.security_title))
        link(dialog.findViewById(R.id.source_link), repository)
        link(
            dialog.findViewById(R.id.configuration_link),
            "$repository/blob/main/app/src/main/res/xml/" +
                "reminder_accessibility_service.xml#L6",
        )
    }

    private fun openSettings() = openIntent(
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
        R.string.settings_unavailable,
    )

    private fun link(view: View, url: String) {
        view.setOnClickListener {
            openIntent(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)),
                R.string.browser_unavailable,
            )
        }
    }

    private fun openIntent(intent: Intent, error: Int) {
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
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
        val enabled = accessibilityEnabled()
        enableButton.visibility = if (enabled) View.GONE
        else View.VISIBLE
        val state = ReminderService.status
        statusText.text = when {
            !enabled -> getString(R.string.status_access_needed)
            !state.connected ->
                getString(R.string.status_connecting)
            state.error != null -> state.error
            state.mapAppActive &&
                state.screenState == ScreenState.ON_AND_UNLOCKED ->
                if (state.showing) {
                    getString(
                        R.string.status_maps_reminder,
                        duration(state.countdownSeconds),
                    )
                } else getString(R.string.status_maps_idle)
            state.showing -> {
                val message = when {
                    state.screenState == ScreenState.ON_AND_LOCKED ->
                        R.string.status_reminder_paused
                    state.screenState == ScreenState.OFF ->
                        R.string.status_reminder_hidden
                    state.countdownSeconds > 0 ->
                        R.string.status_reminder_active
                    else -> R.string.status_ready_to_dismiss
                }
                getString(message, duration(state.countdownSeconds))
            }
            state.screenState != ScreenState.ON_AND_UNLOCKED ->
                getString(R.string.status_idle)
            else -> getString(
                R.string.status_next_reminder,
                duration(state.remainingSeconds),
            )
        }
    }

    private fun duration(seconds: Long): String = if (seconds < 60) {
        getString(R.string.duration_seconds, seconds)
    } else {
        getString(
            R.string.duration_minutes_seconds,
            seconds / 60, seconds % 60,
        )
    }
}
