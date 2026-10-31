package com.stopandbreathe

import org.junit.Assert.*
import org.junit.Test

class ReminderSessionTest {
    // Updates take millisecond timestamps, not durations to add.
    private fun newSession() = ReminderSession(60_000, 10_000)
    private val session = newSession()

    private fun showReminder() {
        session.update(0, active = true)
        session.update(60_000, active = true)
        session.show(60_000)
    }

    @Test fun `long idle resets interval without counting as use`() {
        session.update(0, active = false)
        session.update(100_000, active = true)
        assertEquals(60L, session.remainingSeconds)

        // user stays off for 380 seconds (which is greater than
        // configured interval)
        session.update(120_000, active = false)
        assertEquals(40L, session.remainingSeconds)
        session.update(500_000, active = false)

        // reseted
        session.update(600_000, active = true)
        assertEquals(60L, session.remainingSeconds)

        session.update(659_999, active = true)
        assertFalse(session.isDue)
        session.update(660_000, active = true)
        assertTrue(session.isDue)
    }

    @Test fun `idle recovery scales with the configured interval`() {
        for (interval in listOf(60_000L, 600_000L)) {
            val timer = ReminderSession(interval, 10_000)
            // Use 90% of the interval, then rest for 10% of it.
            val used = interval * 9 / 10
            timer.update(0, active = true)
            timer.update(used, active = false)
            timer.update(used + interval / 10, active = true)
            // 90% used * 90% retained = 81% used, leaving 19%.
            assertEquals(
                interval * 19 / 100,
                timer.intervalRemainingMillis,
            )
        }
    }

    @Test fun `idle polling does not compound recovery`() {
        session.update(0, active = true)
        session.update(30_000, active = false)
        // 15s idle clears 25% of 30s used: 37.5s remain.
        session.update(45_000, active = false)
        assertEquals(37_500L, session.intervalRemainingMillis)
        
        // 30s total idle halves the original 30s used: 45s remain.
        session.update(60_000, active = false)
        assertEquals(45_000L, session.intervalRemainingMillis)
        session.update(60_000, active = true)
        assertEquals(45_000L, session.intervalRemainingMillis)

        // A new break starts from the newly accumulated usage.
        session.update(70_000, active = false)
        // 25s used, then 30s idle: 12.5s used and 47.5s remain.
        session.update(100_000, active = true)
        assertEquals(47_500L, session.intervalRemainingMillis)
    }

    @Test fun `idle clears usage at interval boundary and beyond`() {
        session.update(0, active = true)
        session.update(54_000, active = false)
        session.update(113_999, active = false)
        // The formula leaves 0.9ms used; recovery rounds down to ms.
        assertEquals(59_999L, session.intervalRemainingMillis)
        // A full 60s break clears usage; longer breaks add no credit.
        session.update(114_000, active = false)
        assertEquals(60_000L, session.intervalRemainingMillis)
        session.update(600_000, active = true)
        assertEquals(60_000L, session.intervalRemainingMillis)
        assertFalse(session.isDue)
    }

    @Test fun `due but unshown reminder also receives recovery`() {
        session.update(0, active = true)
        // The interval ends as the screen turns off, before show().
        session.update(60_000, active = false, screenOn = false)
        assertFalse(session.isDue)
        // 30s idle clears half of the 60s used: 30s remain.
        session.update(90_000, active = true)
        assertEquals(30_000L, session.intervalRemainingMillis)
        assertFalse(session.isDue)
        session.update(120_000, active = true)
        assertTrue(session.isDue)
    }

    @Test fun `countdown pause does not count as idle`() {
        session.update(0, active = true)
        // A countdown-only pause must not forgive active phone use.
        session.update(60_000, true, countdownPaused = true)
        session.update(120_000, active = true)
        assertTrue(session.isDue)
        session.show(120_000)
        // After 3s displayed, freeze the remaining 7s.
        session.update(123_000, true, countdownPaused = true)
        assertEquals(7_000L, session.countdownRemainingMillis)
        // Resuming does not charge the paused time.
        session.update(180_000, active = true)
        assertEquals(7_000L, session.countdownRemainingMillis)
        session.update(181_000, active = true)
        assertEquals(6_000L, session.countdownRemainingMillis)
    }

    @Test fun `lit lock screen pauses countdown without resetting`() {
        showReminder()
        session.update(63_250, active = false, screenOn = true)
        assertEquals(6_750L, session.countdownRemainingMillis)

        // A lit lock screen preserves the remaining 6.75s.
        session.update(600_000, active = false, screenOn = true)
        assertEquals(7L, session.countdownSeconds)
        session.update(700_000, active = true)
        session.show(700_000)
        session.update(706_749, active = true)
        assertEquals(1L, session.countdownSeconds)
        assertFalse(session.dismiss(706_749))
        assertTrue(session.dismiss(706_750))
    }

    @Test fun `screen off counts down but lit lock screen pauses`() {
        showReminder()
        session.update(63_000, active = false, screenOn = false)
        assertEquals(7_000L, session.countdownRemainingMillis)
        assertNull(session.nextUpdateDelayMillis(screenOn = false))

        // 3s off consumes 3s; waking to the lock screen pauses it.
        session.update(66_000, active = false, screenOn = true)
        assertEquals(4_000L, session.countdownRemainingMillis)
        // 34s on the lit lock screen consumes nothing.
        session.update(100_000, active = false, screenOn = false)
        assertEquals(4_000L, session.countdownRemainingMillis)
        // Another 2s off counts before the user returns unlocked.
        session.update(102_000, active = true)
        assertEquals(2_000L, session.countdownRemainingMillis)
        session.update(103_000, active = true)
        assertEquals(1_000L, session.countdownRemainingMillis)
    }

    @Test fun `countdown finished while off still needs dismissal`() {
        showReminder()
        session.update(63_000, active = false, screenOn = false)
        assertNull(session.nextUpdateDelayMillis(screenOn = false))

        // No updates while off; waking accounts for all elapsed time.
        session.update(100_000, active = false, screenOn = true)
        assertEquals(0L, session.countdownRemainingMillis)
        assertTrue(session.showing)
        assertFalse(session.dismiss(100_000))

        // Unlock enables dismissal, not an automatic new interval.
        session.update(110_000, active = true)
        assertTrue(session.showing)
        assertFalse(session.isDue)
        assertTrue(session.dismiss(110_000))
        assertEquals(60_000L, session.intervalRemainingMillis)
    }

    @Test fun `paused reminder schedules checks with screen on`() {
        showReminder()
        session.update(63_000, active = false)
        assertNull(session.nextUpdateDelayMillis(screenOn = false))

        // Model screen-on while the session remains inactive.
        session.update(100_000, active = false)
        val delay = session.nextUpdateDelayMillis(screenOn = true)
        assertEquals(ReminderConfig.INTERVAL_CHECK_MILLIS, delay)
        session.update(100_000 + delay!!, active = false)
        assertEquals(7L, session.countdownSeconds)

        // Model the caller reporting the session active again.
        session.update(110_000, active = true)
        assertEquals(
            ReminderConfig.COUNTDOWN_CHECK_MILLIS,
            session.nextUpdateDelayMillis(screenOn = true),
        )
    }

    @Test fun `idle interval schedules checks while screen is on`() {
        session.update(0, active = true)
        session.update(60_000, active = false)
        // 40s idle clears 40s of a fully used 60s interval.
        session.update(100_000, active = false)
        assertEquals(40_000L, session.intervalRemainingMillis)
        assertFalse(session.isDue)
        assertEquals(
            ReminderConfig.INTERVAL_CHECK_MILLIS,
            session.nextUpdateDelayMillis(screenOn = true),
        )
        assertNull(session.nextUpdateDelayMillis(screenOn = false))
        // Idle lasts through this timestamp: 41s have recovered.
        session.update(101_000, active = true)
        assertEquals(41_000L, session.intervalRemainingMillis)
        assertFalse(session.isDue)
        assertEquals(
            ReminderConfig.INTERVAL_CHECK_MILLIS,
            session.nextUpdateDelayMillis(screenOn = true),
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
        // A finished timer still needs an active user's dismissal.
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
        // Waiting far past zero must not shorten the next interval.
        assertTrue(session.dismiss(600_000))
        assertEquals(60L, session.remainingSeconds)
        assertFalse(session.dismiss(600_001))
        session.update(659_999, active = true)
        assertFalse(session.isDue)
        session.update(660_000, active = true)
        assertTrue(session.isDue)
    }

    @Test fun `countdown starts only when window is shown`() {
        session.update(0, active = true)
        session.update(60_000, active = true)
        // Being overdue does not start the 10s countdown.
        session.update(100_000, active = true)
        assertTrue(session.isDue)
        assertFalse(session.showing)
        session.show(100_000)
        session.update(100_001, active = true)
        assertEquals(10L, session.countdownSeconds)
        // Calling show() again must not restart the countdown.
        session.show(101_000)
        assertEquals(9L, session.countdownSeconds)
    }

    @Test fun `new sessions do not share timing state`() {
        session.update(0, active = true)
        session.update(30_000, active = true)
        val freshIntervalSession = newSession()
        freshIntervalSession.update(40_000, active = true)
        assertEquals(60L, freshIntervalSession.remainingSeconds)

        session.update(60_000, active = true)
        session.show(60_000)
        session.update(65_000, active = true)
        val freshReminderSession = newSession()
        freshReminderSession.update(70_000, active = true)
        assertFalse(freshReminderSession.showing)
        assertEquals(60L, freshReminderSession.remainingSeconds)
    }

    @Test fun `fresh session cannot dismiss or become due`() {
        assertFalse(session.active)
        assertFalse(session.isDue)
        assertFalse(session.showing)
        assertFalse(session.dismiss(100_000))
        assertEquals(60L, session.remainingSeconds)
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
