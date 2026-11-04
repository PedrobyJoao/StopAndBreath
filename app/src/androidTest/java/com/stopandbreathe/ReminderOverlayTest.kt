package com.stopandbreathe

import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.KeyEvent
import android.view.View
import android.widget.ScrollView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderOverlayTest {
    private val instrumentation =
        InstrumentationRegistry.getInstrumentation()
    private val short = "I have arrived.\nI am home."
    private val long =
        "Breathing in, I know I’m alive and I dive into life.\n" +
        "Breathing out, I know I’m alive and death at the same time."

    @Test
    fun portraitCentersMessageIndependentlyOfFooter() = onMain {
        val overlay = create(short)
        layout(overlay, 400, 800)
        val rect = messageRect(overlay)
        assertEquals(overlay.width / 2f, rect.exactCenterX(), 1f)
        assertEquals(overlay.height / 2f, rect.exactCenterY(), 1f)
        val button = overlay.dismissButton
        assertEquals(
            overlay.width / 2f, (button.left + button.right) / 2f, 1f,
        )
        assertTrue(rect.bottom < button.top)
        assertTrue(button.bottom < overlay.height)
    }

    @Test
    fun longerMessagesUseSmallerDmSansAndWrap() = onMain {
        val brief = create(short)
        val lengthy = create(long)
        layout(brief, 400, 800)
        layout(lengthy, 400, 800)
        assertTrue(brief.messageView.textSize >
            lengthy.messageView.textSize)
        assertTrue(lengthy.messageView.lineCount > 2)
        assertEquals(
            lengthy.resources.getFont(R.font.dm_sans),
            lengthy.messageView.typeface,
        )
        assertEquals(
            lengthy.messageView.typeface,
            lengthy.dismissButton.typeface,
        )
    }

    @Test
    fun insetsAndLandscapeKeepFooterClearOfMessage() = onMain {
        val overlay = create(long)
        overlay.setPadding(dp(overlay, 30), dp(overlay, 24),
            dp(overlay, 10), dp(overlay, 24))
        layout(overlay, 800, 360)
        val button = overlay.dismissButton
        val scroll = overlay.messageView.parent.parent as ScrollView
        assertTrue(scroll.bottom < button.top)
        assertTrue(button.bottom <=
            overlay.height - overlay.paddingBottom)
        val center = (overlay.paddingLeft + overlay.width -
            overlay.paddingRight) / 2f
        assertEquals(center, messageRect(overlay).exactCenterX(), 1f)
    }

    @Test
    fun largeFontsRemainReadableAndLongMessagesCanScroll() = onMain {
        val overlay = create(long.repeat(8), fontScale = 2f)
        layout(overlay, 360, 640)
        val minimum = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            ReminderConfig.MESSAGE_MIN_TEXT_SP,
            overlay.resources.displayMetrics,
        )
        assertTrue(overlay.messageView.textSize >= minimum)
        val scroll = overlay.messageView.parent.parent as ScrollView
        assertTrue(overlay.messageView.height > scroll.height)
        scroll.scrollTo(0, Int.MAX_VALUE)
        assertTrue(scroll.scrollY > 0)
        assertTrue(scroll.bottom < overlay.dismissButton.top)
    }

    private fun create(
        message: String,
        fontScale: Float = 1f,
    ): ReminderOverlay {
        val target = instrumentation.targetContext
        val config = Configuration(target.resources.configuration)
        config.fontScale = fontScale
        val context = ContextThemeWrapper(
            target.createConfigurationContext(config),
            R.style.Theme_StopAndBreathe,
        )
        return ReminderOverlay(context, message)
    }

    private fun layout(view: View, width: Int, height: Int) {
        val w = dp(view, width)
        val h = dp(view, height)
        view.measure(
            View.MeasureSpec.makeMeasureSpec(
                w, View.MeasureSpec.EXACTLY,
            ),
            View.MeasureSpec.makeMeasureSpec(
                h, View.MeasureSpec.EXACTLY,
            ),
        )
        view.layout(0, 0, w, h)
    }

    private fun messageRect(overlay: ReminderOverlay): Rect {
        val rect = Rect()
        overlay.messageView.getDrawingRect(rect)
        overlay.offsetDescendantRectToMyCoords(
            overlay.messageView, rect,
        )
        return rect
    }

    private fun dp(view: View, value: Int): Int =
        (value * view.resources.displayMetrics.density).toInt()

    private fun onMain(block: () -> Unit) =
        instrumentation.runOnMainSync(block)
}
