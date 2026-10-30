package com.stopandbreathe

import org.junit.Assert.*
import org.junit.Test

class ReminderSessionTest {
    private fun newSession() = ReminderSession(60_000, 10_000)
    private val session = newSession()

    private fun showReminder() {
        session.update(0, active = true)
        session.update(60_000, active = true)
        session.show(60_000)
    }

    @Test fun `interval counts only active time across pauses`() {
        session.update(0, active = false)
        session.update(100_000, active = true)
        assertEquals(60L, session.intervalSeconds)
        session.update(120_000, active = false)
        assertEquals(40L, session.intervalSeconds)
        session.update(500_000, active = false)
        session.update(600_000, active = true)
        assertEquals(40L, session.intervalSeconds)
        session.update(639_999, active = true)
        assertFalse(session.isDue)
        session.update(640_000, active = true)
        assertTrue(session.isDue)
    }

    @Test fun `due reminder waits while inactive without catchup`() {
        session.update(0, active = true)
        session.update(60_000, active = false)
        assertFalse(session.isDue)
        session.update(600_000, active = true)
        assertTrue(session.isDue)
        assertFalse(session.showing)
        session.show(600_000)
        assertEquals(10L, session.countdownSeconds)
        assertFalse(session.isDue)
    }

    @Test fun `countdown pauses and resumes without resetting`() {
        showReminder()
        session.update(63_250, active = false)
        assertEquals(6_750L, session.countdownRemainingMillis)
        session.update(600_000, active = false)
        assertEquals(7L, session.countdownSeconds)
        session.update(700_000, active = true)
        session.show(700_000)
        session.update(706_749, active = true)
        assertEquals(1L, session.countdownSeconds)
        assertFalse(session.dismiss(706_749))
        assertTrue(session.dismiss(706_750))
    }

    @Test fun `paused reminder recovers without unlock broadcast`() {
        showReminder()
        session.update(63_000, active = false)
        assertNull(session.nextUpdateDelayMillis(screenOn = false))

        // SCREEN_ON arrives while locked. USER_PRESENT never arrives.
        session.update(100_000, active = false)
        val delay = session.nextUpdateDelayMillis(screenOn = true)
        assertEquals(ReminderConfig.INTERVAL_CHECK_MILLIS, delay)
        session.update(100_000 + delay!!, active = false)
        assertEquals(7L, session.countdownSeconds)

        // The scheduled check detects unlock and resumes the session.
        session.update(110_000, active = true)
        assertTrue(session.active)
        assertTrue(session.showing)
        assertEquals(7L, session.countdownSeconds)
        assertEquals(
            ReminderConfig.COUNTDOWN_CHECK_MILLIS,
            session.nextUpdateDelayMillis(screenOn = true),
        )
        assertFalse(session.dismiss(116_999))
        assertTrue(session.dismiss(117_000))
    }

    @Test fun `due interval checks keyguard without busy loop`() {
        session.update(0, active = true)
        session.update(60_000, active = false)
        session.update(100_000, active = false)
        assertEquals(0L, session.intervalRemainingMillis)
        assertFalse(session.isDue)
        assertEquals(
            ReminderConfig.INTERVAL_CHECK_MILLIS,
            session.nextUpdateDelayMillis(screenOn = true),
        )
        assertNull(session.nextUpdateDelayMillis(screenOn = false))
        session.update(101_000, active = true)
        assertTrue(session.isDue)
        assertEquals(
            1L, session.nextUpdateDelayMillis(screenOn = true),
        )
    }

    @Test fun `dismiss checks time even between UI updates`() {
        showReminder()
        assertEquals(10L, session.countdownSeconds)
        assertFalse(session.dismiss(69_999))
        assertEquals(1L, session.countdownSeconds)
        assertTrue(session.showing)
        assertTrue(session.dismiss(70_000))
        assertFalse(session.showing)
    }

    @Test fun `zero countdown waits for active explicit dismissal`() {
        showReminder()
        session.update(80_000, active = false)
        assertEquals(0L, session.countdownSeconds)
        assertTrue(session.showing)
        assertFalse(session.dismiss(90_000))
        session.update(100_000, active = true)
        assertTrue(session.showing)
        assertFalse(session.isDue)
        assertTrue(session.dismiss(100_000))
    }

    @Test fun `dismiss starts a full interval without catchup`() {
        showReminder()
        assertTrue(session.dismiss(600_000))
        assertEquals(60L, session.intervalSeconds)
        assertFalse(session.dismiss(600_001))
        session.update(659_999, active = true)
        assertFalse(session.isDue)
        session.update(660_000, active = true)
        assertTrue(session.isDue)
    }

    @Test fun `countdown starts only when window is shown`() {
        session.update(0, active = true)
        session.update(60_000, active = true)
        session.update(100_000, active = true)
        assertTrue(session.isDue)
        assertFalse(session.showing)
        session.show(100_000)
        session.update(100_001, active = true)
        assertEquals(10L, session.countdownSeconds)
        session.show(101_000)
        assertEquals(9L, session.countdownSeconds)
    }

    @Test fun `new service session resets all progress`() {
        session.update(0, active = true)
        session.update(30_000, active = true)
        val afterIntervalRestart = newSession()
        afterIntervalRestart.update(40_000, active = true)
        assertEquals(60L, afterIntervalRestart.intervalSeconds)

        session.update(60_000, active = true)
        session.show(60_000)
        session.update(65_000, active = true)
        val afterReminderRestart = newSession()
        afterReminderRestart.update(70_000, active = true)
        assertFalse(afterReminderRestart.showing)
        assertEquals(60L, afterReminderRestart.intervalSeconds)
    }

    @Test fun `fresh session cannot dismiss or become due`() {
        assertFalse(session.active)
        assertFalse(session.isDue)
        assertFalse(session.showing)
        assertFalse(session.dismiss(100_000))
        assertEquals(60L, session.intervalSeconds)
        assertEquals(0L, session.countdownSeconds)
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
