package com.stopandbreathe

import com.stopandbreathe.MapAppTracker.Classification.MAP
import com.stopandbreathe.MapAppTracker.Classification.OTHER
import com.stopandbreathe.MapAppTracker.Classification.UNKNOWN
import com.stopandbreathe.ScreenState.ON_AND_UNLOCKED
import org.junit.Assert.*
import org.junit.Test

class MapAppTrackerTest {
    private val tracker = MapAppTracker(
        mapPackages = setOf("test.maps"),
        ownActivityClass = "test.reminders.MainActivity",
    )

    private fun window(
        app: String? = "test.maps",
        className: String? = null,
        isKeyboardPackage: Boolean = false,
    ) = tracker.onWindowChanged(
        app, className, "test.reminders", isKeyboardPackage,
    )

    @Test fun `fresh tracker is unknown without exempting use`() {
        assertEquals(UNKNOWN, tracker.classification)
        assertFalse(tracker.mapAppActive)
        val session = newSession()
        session.update(60_000, ON_AND_UNLOCKED)
        assertTrue(session.isDue)
    }

    @Test fun `accepted app switches replace last classification`() {
        assertTrue(window())
        assertEquals(MAP, tracker.classification)
        assertTrue(tracker.mapAppActive)
        assertTrue(window("test.other"))
        assertEquals(OTHER, tracker.classification)
        assertFalse(tracker.mapAppActive)
        // IDs must match exactly, not by prefix.
        assertTrue(window("test.maps.other"))
        assertEquals(OTHER, tracker.classification)
        assertTrue(window())
        assertEquals(MAP, tracker.classification)
    }

    @Test fun `overlay preserves maps but own activity exits`() {
        window()
        assertFalse(window("test.reminders", "Overlay"))
        assertFalse(window("test.reminders"))
        assertEquals(MAP, tracker.classification)
        assertTrue(window(
            "test.reminders", "test.reminders.MainActivity",
        ))
        assertEquals(OTHER, tracker.classification)
    }

    @Test fun `ignored events neither resolve nor replace state`() {
        for (state in listOf(UNKNOWN, MAP, OTHER)) {
            when (state) {
                UNKNOWN -> Unit
                MAP -> window()
                OTHER -> window("test.other")
            }
            assertFalse(window(null))
            assertFalse(window(""))
            assertFalse(window(" "))
            assertFalse(window("test.reminders", "Overlay"))
            assertFalse(window(
                "test.keyboard", isKeyboardPackage = true,
            ))
            assertEquals(state, tracker.classification)
        }
    }

    private fun newSession() = ReminderSession(60_000, 10_000).apply {
        update(
            0, ON_AND_UNLOCKED, mapAppActive = tracker.mapAppActive,
        )
    }
}
