package com.stopandbreathe

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextPaint
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlin.math.roundToInt

/** The reminder's presentation, independent of its timing rules. */
@SuppressLint("ViewConstructor") // Created in code, never from XML.
class ReminderOverlay(context: Context, message: String) :
    FrameLayout(context) {
    private val font = resources.getFont(R.font.dm_sans)
    val messageView = TextView(context).apply {
        text = message
        typeface = font
        gravity = Gravity.CENTER
        setTextColor(context.getColor(R.color.reminder_ink))
        includeFontPadding = false
        setLineSpacing(0f, 1.3f)
    }
    val dismissButton = Button(context).apply {
        setText(R.string.reminder_dismiss)
        typeface = font
        textSize = ReminderConfig.DISMISS_TEXT_SP
        isAllCaps = false
        isEnabled = false
        gravity = Gravity.CENTER
        minWidth = 0
        minimumWidth = 0
        minHeight = dp(48)
        minimumHeight = dp(48)
        setPadding(dp(20), dp(12), dp(20), dp(12))
        backgroundTintList = null
        setBackgroundResource(R.drawable.reminder_button)
        setTextColor(context.getColorStateList(
            R.color.reminder_button_text,
        ))
        stateListAnimator = null
    }
    private val messageScroll = ScrollView(context).apply {
        isFillViewport = true
        isVerticalScrollBarEnabled = false
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            addView(messageView, LinearLayout.LayoutParams(-1, -2))
        })
    }

    init {
        setBackgroundResource(R.drawable.reminder_background)
        isClickable = true
        isFocusableInTouchMode = true
        addView(messageScroll, LayoutParams(-1, -1, Gravity.CENTER))
        addView(dismissButton, LayoutParams(
            -2, -2, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
        ))
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) return true
        return super.dispatchKeyEvent(event)
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val width = (MeasureSpec.getSize(widthSpec) -
            paddingLeft - paddingRight).coerceAtLeast(0)
        val height = (MeasureSpec.getSize(heightSpec) -
            paddingTop - paddingBottom).coerceAtLeast(0)
        val side = maxOf(dp(24), (width * 0.08f).roundToInt())
            .coerceAtMost(width / 2)
        val textWidth = (width - side * 2).coerceAtLeast(1)

        val buttonParams = dismissButton.layoutParams as LayoutParams
        buttonParams.width = minOf(textWidth, dp(320))
        buttonParams.bottomMargin = maxOf(
            dp(24), (height * 0.06f).roundToInt(),
        )
        dismissButton.measure(
            MeasureSpec.makeMeasureSpec(
                buttonParams.width, MeasureSpec.EXACTLY,
            ),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
        )
        val footerSpace = buttonParams.bottomMargin +
            dismissButton.measuredHeight + dp(24)
        val centeredHeight = height - footerSpace * 2
        val scrollParams = messageScroll.layoutParams as LayoutParams
        scrollParams.leftMargin = side
        scrollParams.rightMargin = side
        // Reserve equal space above and below, so the footer never
        // shifts the message away from the screen's true center.
        // On short screens, prioritize readable, scrollable content.
        scrollParams.height = if (centeredHeight >= dp(96)) {
            scrollParams.gravity = Gravity.CENTER
            centeredHeight
        } else {
            scrollParams.gravity = Gravity.TOP
            (height - footerSpace).coerceAtLeast(0)
        }
        fitMessage(textWidth)
        super.onMeasure(widthSpec, heightSpec)
    }

    private fun fitMessage(width: Int) {
        val metrics = resources.displayMetrics
        val baseSp = (width / metrics.density * 0.08f)
            .coerceIn(24f, ReminderConfig.MESSAGE_MAX_TEXT_SP)
        val minSp = maxOf(
            ReminderConfig.MESSAGE_MIN_TEXT_SP, baseSp * 0.60f,
        )
        val paint = TextPaint(messageView.paint).apply {
            textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, baseSp, metrics,
            )
        }
        val longestLine = messageView.text.split('\n').maxOf {
            paint.measureText(it)
        }
        val scale = if (longestLine > 0) width / longestLine else 1f
        val size = (baseSp * scale).coerceIn(minSp, baseSp)
        // SP preserves system font scaling. Long messages wrap and
        // can scroll rather than shrinking below the readable floor.
        messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).roundToInt()
}
