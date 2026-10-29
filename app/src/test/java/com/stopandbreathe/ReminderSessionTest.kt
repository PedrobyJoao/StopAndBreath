package com.stopandbreathe

import org.junit.Assert.*
import org.junit.Test

class ReminderSessionTest {
    private val session = ReminderSession(intervalMillis = 60_000, countdownMillis = 10_000)

    @Test fun `start schedules first reminder without resetting on duplicate start`() {
        session.start(1_000)
        session.start(2_000)
        assertEquals(61_000L, session.nextReminderAt)
        assertFalse(session.isDue(60_999))
        assertTrue(session.isDue(61_000))
    }

    @Test fun `countdown rounds up and cannot be bypassed`() {
        session.start(0)
        session.show(5_000)
        assertEquals(10L, session.secondsRemaining(5_000))
        assertEquals(10L, session.secondsRemaining(5_001))
        assertEquals(1L, session.secondsRemaining(14_999))
        assertFalse(session.dismiss(14_999))
        assertFalse(session.stop())
        assertTrue(session.running)
        assertTrue(session.showing)
        assertNull(session.nextReminderAt)
    }

    @Test fun `dismiss schedules next interval from dismissal`() {
        session.start(0)
        session.show(5_000)
        assertEquals(0L, session.secondsRemaining(15_000))
        assertTrue(session.dismiss(15_000))
        assertFalse(session.showing)
        assertEquals(75_000L, session.nextReminderAt)
        assertTrue(session.stop())
        assertFalse(session.running)
        assertNull(session.nextReminderAt)
    }

    @Test fun `unlocked reminder still requires explicit dismissal`() {
        session.start(0)
        session.show(0)
        assertEquals(0L, session.secondsRemaining(100_000))
        assertFalse(session.isDue(100_000))
        assertFalse(session.stop())
        assertTrue(session.showing)
        assertTrue(session.dismiss(100_000))
    }

    @Test fun `repeated test does not reset countdown`() {
        session.start(0)
        session.show(1_000)
        session.show(5_000)
        assertEquals(11_000L, session.dismissAvailableAt)
        assertEquals(6L, session.secondsRemaining(5_000))
    }

    @Test fun `overdue reminder can show once after sleep`() {
        session.start(0)
        assertTrue(session.isDue(600_000))
        session.show(600_000)
        assertFalse(session.isDue(600_000))
        assertEquals(10L, session.secondsRemaining(600_000))
    }

    @Test fun `stopped session can restart`() {
        session.start(0)
        assertTrue(session.stop())
        assertFalse(session.isDue(100_000))
        session.start(100_000)
        assertEquals(160_000L, session.nextReminderAt)
    }

    @Test fun `late dismissal does not schedule a burst of missed reminders`() {
        session.start(0)
        session.show(60_000)
        assertTrue(session.dismiss(600_000))
        assertEquals(660_000L, session.nextReminderAt)
        assertFalse(session.isDue(600_000))
        assertFalse(session.dismiss(600_001))
        assertEquals(660_000L, session.nextReminderAt)
    }

    @Test fun `inactive session has no pending reminder`() {
        assertFalse(session.running)
        assertFalse(session.showing)
        assertFalse(session.isDue(600_000))
        assertFalse(session.dismiss(600_000))
        assertEquals(0L, session.secondsRemaining(600_000))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid interval is rejected`() {
        ReminderSession(0, 10_000)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid countdown is rejected`() {
        ReminderSession(60_000, 0)
    }
}
