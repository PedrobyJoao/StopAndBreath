package com.stopandbreathe

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

    @Test fun `fresh tracker does not exempt use`() {
        assertFalse(tracker.mapAppActive)
        val session = newSession()
        session.update(60_000, ON_AND_UNLOCKED)
        assertTrue(session.isDue)
    }

    @Test fun `accepted app switches update map estimate`() {
        assertTrue(window())
        assertTrue(tracker.mapAppActive)
        assertTrue(window("test.other"))
        assertFalse(tracker.mapAppActive)
        // IDs must match exactly, not by prefix.
        assertTrue(window("test.maps.other"))
        assertFalse(tracker.mapAppActive)
        assertTrue(window())
        assertTrue(tracker.mapAppActive)
    }

    @Test fun `overlay preserves maps but own activity exits`() {
        window()
        assertFalse(window("test.reminders", "Overlay"))
        assertFalse(window("test.reminders"))
        assertTrue(tracker.mapAppActive)
        assertTrue(window(
            "test.reminders", "test.reminders.MainActivity",
        ))
        assertFalse(tracker.mapAppActive)
    }

    @Test fun `ignored events preserve map estimate`() {
        for (mapActive in listOf(false, true)) {
            window(if (mapActive) "test.maps" else "test.other")
            assertFalse(window(null))
            assertFalse(window(""))
            assertFalse(window(" "))
            assertFalse(window("test.reminders", "Overlay"))
            assertFalse(window(
                "test.keyboard", isKeyboardPackage = true,
            ))
            assertEquals(mapActive, tracker.mapAppActive)
        }
    }

    private fun newSession() = ReminderSession(60_000, 10_000).apply {
        update(
            0, ON_AND_UNLOCKED, mapAppActive = tracker.mapAppActive,
        )
    }
}
