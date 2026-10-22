package se.imorn.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * The app's day is the phone's day: it turns at local midnight, in the phone's own time zone, and never jumps when the
 * clocks go forward or back.
 */
class DeviceTodayTest {
    @Test
    fun `the day is the day on the phone's clock`() {
        val today = DeviceToday(Clock.fixed(Instant.parse("2027-01-10T12:00:00Z"), ZoneId.of("Europe/Stockholm")))
        assertEquals(LocalDate.of(2027, 1, 10), today.now())
    }

    @Test
    fun `the day turns at local midnight, not at UTC midnight`() {
        // 23:59:59 in Stockholm is 22:59:59 UTC: still the tenth.
        val before = DeviceToday(Clock.fixed(Instant.parse("2027-01-10T22:59:59Z"), ZoneId.of("Europe/Stockholm")))
        assertEquals(LocalDate.of(2027, 1, 10), before.now())
        // A second later it is midnight in Stockholm: the eleventh.
        val after = DeviceToday(Clock.fixed(Instant.parse("2027-01-10T23:00:01Z"), ZoneId.of("Europe/Stockholm")))
        assertEquals(LocalDate.of(2027, 1, 11), after.now())
    }

    @Test
    fun `the same moment is a different day in different places`() {
        val moment = Instant.parse("2027-01-10T23:30:00Z")
        val stockholm = DeviceToday(Clock.fixed(moment, ZoneId.of("Europe/Stockholm")))
        val newYork = DeviceToday(Clock.fixed(moment, ZoneId.of("America/New_York")))
        assertEquals(LocalDate.of(2027, 1, 11), stockholm.now())
        assertEquals(LocalDate.of(2027, 1, 10), newYork.now())
    }

    @Test
    fun `summer time does not jump a day`() {
        val zone = ZoneId.of("Europe/Stockholm")
        // The clocks go forward at 01:00 UTC on 2027-03-28: the day is the twenty-eighth on both sides of it.
        val springBefore = DeviceToday(Clock.fixed(Instant.parse("2027-03-27T23:30:00Z"), zone))
        val springAfter = DeviceToday(Clock.fixed(Instant.parse("2027-03-28T00:30:00Z"), zone))
        assertEquals(LocalDate.of(2027, 3, 28), springBefore.now())
        assertEquals(LocalDate.of(2027, 3, 28), springAfter.now())
        // And back at 01:00 UTC on 2027-10-31: the thirty-first on both sides of it.
        val autumnBefore = DeviceToday(Clock.fixed(Instant.parse("2027-10-30T23:30:00Z"), zone))
        val autumnAfter = DeviceToday(Clock.fixed(Instant.parse("2027-10-31T00:30:00Z"), zone))
        assertEquals(LocalDate.of(2027, 10, 31), autumnBefore.now())
        assertEquals(LocalDate.of(2027, 10, 31), autumnAfter.now())
    }

    @Test
    fun `the day is read fresh, so a restart or a passing midnight changes it`() {
        val moment = Instant.parse("2027-01-10T23:59:59Z")
        val clock = Clock.fixed(moment, ZoneOffset.UTC)
        assertEquals(LocalDate.of(2027, 1, 10), DeviceToday(clock).now())
        val later = Clock.fixed(moment.plusSeconds(2), ZoneOffset.UTC)
        assertEquals(LocalDate.of(2027, 1, 11), DeviceToday(later).now())
    }

    @Test
    fun `the app's day is the phone's own day`() {
        assertEquals(LocalDate.now(), DeviceToday().now())
    }
}
