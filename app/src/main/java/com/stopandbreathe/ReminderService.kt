package com.stopandbreathe

import android.Manifest
import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

data class ReminderStatus(
    val running: Boolean = false,
    val showing: Boolean = false,
    val nextReminderAt: Long = 0,
    val error: String? = null,
)

class ReminderService : Service() {
    companion object {
        const val ACTION_START = "com.stopandbreathe.START"
        const val ACTION_TEST = "com.stopandbreathe.TEST"
        const val ACTION_STOP = "com.stopandbreathe.STOP"
        private const val CHANNEL_ID = "breathing_reminders"
        private const val NOTIFICATION_ID = 1

        // UI and service both run on the main thread. Deliberately not persisted:
        // process termination must not leave behind a false "running" indicator.
        var status = ReminderStatus()
            private set
    }

    private val handler = Handler(Looper.getMainLooper())
    private val session = ReminderSession(ReminderConfig.INTERVAL_MILLIS, ReminderConfig.COUNTDOWN_MILLIS)
    private lateinit var windows: WindowManager
    private var overlay: View? = null
    private var countdownText: TextView? = null
    private var dismissButton: Button? = null
    private var foreground = false

    private val update = Runnable { updateReminder() }
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (session.running) updateReminder()
        }
    }

    override fun onCreate() {
        super.onCreate()
        windows = getSystemService(WindowManager::class.java)
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Breathing reminders", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Shown while mindful-breathing reminders are enabled."
                setShowBadge(false)
            },
        )
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(screenReceiver, filter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            if (session.stop()) {
                handler.removeCallbacks(update)
                publishStatus()
                stopSelf()
            }
            return START_NOT_STICKY
        }
        if (intent?.action != ACTION_START && intent?.action != ACTION_TEST) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (!Settings.canDrawOverlays(this)) {
            fail("Overlay permission is missing. Grant it, then start reminders again.")
            return START_NOT_STICKY
        }

        try {
            // Always enter the foreground before doing any window/timer work.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIFICATION_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(NOTIFICATION_ID, buildNotification())
            }
            foreground = true
            session.start(SystemClock.elapsedRealtime())
            publishStatus()
            if (intent.action == ACTION_TEST && !session.showing && canShowReminder()) showReminder()
            updateReminder()
        } catch (error: RuntimeException) {
            fail("Could not start reminders: ${error.message ?: error.javaClass.simpleName}")
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun updateReminder() {
        handler.removeCallbacks(update)
        if (!session.running) return
        if (!Settings.canDrawOverlays(this)) {
            fail("Overlay permission was revoked. Reminders have stopped.")
            return
        }

        val now = SystemClock.elapsedRealtime()
        if (!session.showing && session.isDue(now)) {
            // No wake lock, exact alarm, or lock-screen takeover. An overdue
            // reminder is shown on the next screen-on/unlock broadcast.
            if (!canShowReminder()) return
            try {
                showReminder()
            } catch (error: RuntimeException) {
                fail("Could not display reminder: ${error.message ?: error.javaClass.simpleName}")
                return
            }
        }

        if (session.showing) {
            val remaining = session.secondsRemaining(SystemClock.elapsedRealtime())
            countdownText?.text = if (remaining > 0) "Take a breath. Dismiss in ${remaining}s." else "Continue when you're ready."
            dismissButton?.isEnabled = remaining == 0L
            // Once unlocked, the overlay stays until explicitly dismissed.
            if (remaining > 0) handler.postDelayed(update, 200)
        } else {
            val delay = ((session.nextReminderAt ?: now) - now).coerceAtLeast(1)
            handler.postDelayed(update, delay)
        }
        publishStatus()
    }

    private fun canShowReminder(): Boolean =
        getSystemService(PowerManager::class.java).isInteractive &&
            !getSystemService(KeyguardManager::class.java).isKeyguardLocked

    private fun showReminder() {
        if (overlay != null) return
        val root = object : FrameLayout(this) {
            // Back must not dismiss our window, including with hardware keys.
            // Home, system gestures, and Android's own controls remain available.
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                if (event.keyCode == KeyEvent.KEYCODE_BACK) return true
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
        val message = TextView(this).apply {
            setText(R.string.breathing_message)
            textSize = 28f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, dp(32))
        }
        val countdown = TextView(this).apply {
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(210, 231, 220))
            setPadding(0, 0, 0, dp(16))
            text = "Take a breath. Dismiss in ${ReminderConfig.COUNTDOWN_SECONDS}s."
        }
        val dismiss = Button(this).apply {
            text = "Dismiss"
            isAllCaps = false
            isEnabled = false
            setOnClickListener {
                if (session.dismiss(SystemClock.elapsedRealtime())) {
                    removeOverlay()
                    publishStatus()
                    updateNotification()
                    updateReminder()
                }
            }
        }
        content.addView(message, LinearLayout.LayoutParams(-1, -2))
        content.addView(countdown, LinearLayout.LayoutParams(-1, -2))
        content.addView(dismiss, LinearLayout.LayoutParams(-1, -2))
        root.addView(ScrollView(this).apply {
            isFillViewport = true
            addView(content)
        }, FrameLayout.LayoutParams(-1, -1))

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.OPAQUE,
        ).apply { gravity = Gravity.TOP or Gravity.START }

        windows.addView(root, params)
        overlay = root
        countdownText = countdown
        dismissButton = dismiss
        // Begin the countdown only after WindowManager successfully adds it.
        session.show(SystemClock.elapsedRealtime())
        root.requestFocus()
        publishStatus()
        updateNotification()
    }

    private fun removeOverlay() {
        overlay?.let { view ->
            // Permission revocation or system cleanup may already detach it.
            try {
                windows.removeViewImmediate(view)
            } catch (_: IllegalArgumentException) {
                // Already removed by Android.
            }
        }
        overlay = null
        countdownText = null
        dismissButton = null
    }

    private fun publishStatus() {
        status = ReminderStatus(session.running, session.showing, session.nextReminderAt ?: 0)
    }

    private fun buildNotification(): Notification {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val builder = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Stop&Breathe reminders are on")
            .setContentText(
                if (session.showing) "Breathing reminder active — dismiss it when ready."
                else "A breathing reminder every ${ReminderConfig.INTERVAL_MINUTES} minute(s).",
            )
            .setContentIntent(open)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_SERVICE)
        if (!session.showing) {
            val stop = PendingIntent.getService(
                this, 1, Intent(this, ReminderService::class.java).setAction(ACTION_STOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            builder.addAction(Notification.Action.Builder(null, "Stop reminders", stop).build())
        }
        return builder.build()
    }

    private fun updateNotification() {
        // Foreground services may start without this permission, but do not
        // issue ordinary notification updates when the user has denied it.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        if (foreground) getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, buildNotification())
    }

    private fun fail(message: String) {
        handler.removeCallbacks(update)
        removeOverlay()
        status = ReminderStatus(error = message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        stopSelf()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        unregisterReceiver(screenReceiver)
        removeOverlay()
        stopForeground(STOP_FOREGROUND_REMOVE)
        status = ReminderStatus(error = status.error)
        super.onDestroy()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
