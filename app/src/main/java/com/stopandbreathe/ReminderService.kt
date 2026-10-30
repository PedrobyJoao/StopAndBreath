package com.stopandbreathe

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

data class ReminderStatus(
    val connected: Boolean = false,
    val active: Boolean = false,
    val showing: Boolean = false,
    val intervalSeconds: Long = 0,
    val countdownSeconds: Long = 0,
    val error: String? = null,
)

class ReminderService : AccessibilityService() {
    companion object {
        // Main-thread only. No timing or status is persisted.
        var status = ReminderStatus()
            private set
    }

    private val handler = Handler(Looper.getMainLooper())
    private var session = newSession()
    private lateinit var windows: WindowManager
    private var overlay: View? = null
    private var countdownText: TextView? = null
    private var dismissButton: Button? = null
    private var message: String? = null
    private var connected = false
    private var receiverRegistered = false
    private var error: String? = null

    private val update = Runnable { updateReminder() }
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Trust screen transitions even if the power manager has
            // not exposed the new state yet.
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> updateReminder(false)
                Intent.ACTION_SCREEN_ON -> updateReminder(true)
                Intent.ACTION_USER_PRESENT -> updateReminder()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        cleanup()
        windows = getSystemService(WindowManager::class.java)
        session = newSession()
        error = null
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // USER_PRESENT can come from System UI, not the system
            // UID. All three actions are protected system broadcasts;
            // ordinary apps cannot spoof them.
            registerReceiver(
                screenReceiver, filter, RECEIVER_EXPORTED,
            )
        } else {
            registerReceiver(screenReceiver, filter)
        }
        receiverRegistered = true
        connected = true
        updateReminder()
    }

    // This service does not subscribe to accessibility events or read
    // window content. Its only accessibility feature is its overlay.
    override fun onAccessibilityEvent(
        event: AccessibilityEvent?,
    ) = Unit

    override fun onInterrupt() {
        // Interrupting feedback must not dismiss a reminder.
    }

    private fun isScreenOn(): Boolean =
        getSystemService(PowerManager::class.java).isInteractive

    private fun isUnlocked(): Boolean =
        !getSystemService(KeyguardManager::class.java)
            .isKeyguardLocked

    private fun isAvailable(): Boolean = isScreenOn() && isUnlocked()

    private fun updateReminder(screenOn: Boolean = isScreenOn()) {
        handler.removeCallbacks(update)
        if (!connected) return
        val active = screenOn && isUnlocked()
        session.update(SystemClock.elapsedRealtime(), active)
        var delay = session.nextUpdateDelayMillis(screenOn)

        if (!active) {
            // Never cover the keyguard or keep the screen awake.
            // The session and its chosen message survive this pause.
            removeOverlay()
        } else {
            try {
                if (session.isDue) {
                    val nextMessage = ReminderConfig.MESSAGES.random()
                    addOverlay(nextMessage)
                    session.show(SystemClock.elapsedRealtime())
                    message = nextMessage
                    delay = ReminderConfig.COUNTDOWN_CHECK_MILLIS
                } else if (session.showing && overlay == null) {
                    addOverlay(checkNotNull(message))
                }
                renderCountdown()
                error = null
            } catch (_: RuntimeException) {
                removeOverlay()
                // A failed window must not consume the countdown.
                session.update(SystemClock.elapsedRealtime(), false)
                error = "Could not display the breathing reminder. " +
                    "Check accessibility access in Settings."
                delay = ReminderConfig.OVERLAY_RETRY_MILLIS
            }
        }

        publishStatus()
        // Keep checking a lit lock screen even if USER_PRESENT is
        // missed. Screen-off cancels checks until the next broadcast.
        // No foreground service, exact alarm, or wake lock is needed.
        if (delay != null) handler.postDelayed(update, delay)
    }

    private fun renderCountdown() {
        val remaining = session.countdownSeconds
        countdownText?.text = if (remaining > 0) {
            "Take a breath. Dismiss in ${remaining}s."
        } else {
            "0s — continue when you're ready."
        }
        dismissButton?.isEnabled = session.active && remaining == 0L
    }

    private fun addOverlay(text: String) {
        if (overlay != null) return
        val root = object : FrameLayout(this) {
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                    return true
                }
                return super.dispatchKeyEvent(event)
            }
        }.apply {
            setBackgroundColor(Color.rgb(17, 42, 36))
            isClickable = true
            isFocusableInTouchMode = true
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(32), dp(24), dp(32))
        }
        val messageView = TextView(this).apply {
            this.text = text
            textSize = ReminderConfig.MESSAGE_TEXT_SP
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, dp(32))
        }
        val countdown = TextView(this).apply {
            textSize = ReminderConfig.COUNTDOWN_TEXT_SP
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(210, 231, 220))
            setPadding(0, 0, 0, dp(16))
        }
        val dismiss = Button(this).apply {
            this.text = "Dismiss"
            isAllCaps = false
            isEnabled = false
            setOnClickListener {
                // Recheck the real screen state and monotonic timer;
                // disabled styling alone is not the dismissal guard.
                session.update(
                    SystemClock.elapsedRealtime(), isAvailable(),
                )
                if (session.dismiss(SystemClock.elapsedRealtime())) {
                    removeOverlay()
                    message = null
                }
                updateReminder()
            }
        }
        content.addView(
            messageView, LinearLayout.LayoutParams(-1, -2),
        )
        content.addView(countdown, LinearLayout.LayoutParams(-1, -2))
        content.addView(dismiss, LinearLayout.LayoutParams(-1, -2))
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(content)
        }
        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))
        // The barrier fills the screen; inset only its content so
        // cutouts and system navigation do not obscure Dismiss.
        root.setOnApplyWindowInsetsListener { _, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bars = insets.getInsets(
                    WindowInsets.Type.systemBars() or
                        WindowInsets.Type.displayCutout(),
                )
                scroll.setPadding(
                    bars.left, bars.top, bars.right, bars.bottom,
                )
            } else {
                @Suppress("DEPRECATION")
                scroll.setPadding(
                    insets.systemWindowInsetLeft,
                    insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight,
                    insets.systemWindowInsetBottom,
                )
            }
            insets
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.OPAQUE,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setFitInsetsTypes(0)
            }
        }
        windows.addView(root, params)
        overlay = root
        countdownText = countdown
        dismissButton = dismiss
        root.requestFocus()
        root.requestApplyInsets()
    }

    private fun removeOverlay() {
        overlay?.let { view ->
            try {
                windows.removeViewImmediate(view)
            } catch (_: IllegalArgumentException) {
                // Revoking access may already detach the window.
            }
        }
        overlay = null
        countdownText = null
        dismissButton = null
    }

    private fun publishStatus() {
        status = ReminderStatus(
            connected = connected,
            active = session.active,
            showing = session.showing,
            intervalSeconds = session.intervalSeconds,
            countdownSeconds = session.countdownSeconds,
            error = error,
        )
    }

    private fun cleanup() {
        connected = false
        handler.removeCallbacksAndMessages(null)
        if (receiverRegistered) {
            unregisterReceiver(screenReceiver)
            receiverRegistered = false
        }
        removeOverlay()
        message = null
        status = ReminderStatus()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        cleanup()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        cleanup()
        super.onDestroy()
    }

    private fun newSession() = ReminderSession(
        ReminderConfig.INTERVAL_MILLIS,
        ReminderConfig.COUNTDOWN_MILLIS,
    )

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
