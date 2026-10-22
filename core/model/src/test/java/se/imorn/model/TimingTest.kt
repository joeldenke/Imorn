package se.imorn.model

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.TimeZone

/**
 * The plan is made of days, not times. A day has no time of day and no time zone, so the plan must be the same in
 * every time zone, over summer time, and after a restart. Only *today* follows the phone.
 */
class TimingTest {
    private val originalZone = TimeZone.getDefault()

    @After
    fun restoreTheZone() {
        TimeZone.setDefault(originalZone)
    }

    @Test
    fun `the plan is the same in every time zone`() {
        val expected = listOf(
            "notice" to LocalDate.of(2026, 12, 1),
            "book-van" to LocalDate.of(2027, 1, 4),
            "address" to LocalDate.of(2027, 2, 15),
            "boxes" to null,
        )
        for (zone in ZONES) {
            TimeZone.setDefault(TimeZone.getTimeZone(zone))
            val chapter = moving()
            val address = chapter.steps.first { it.id == "address" }
            assertEquals("in $zone", expected, chapter.upcoming().map { it.step.id to it.date })
            assertEquals("in $zone", LocalDate.of(2027, 3, 8), chapter.deadlineOf(address))
        }
    }

    @Test
    fun `summer time does not move a day`() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Stockholm"))
        // The clocks go forward on 2027-03-28 and back on 2027-10-31.
        val spring = moving(moveDay = LocalDate.of(2027, 3, 28))
        val springAddress = spring.steps.first { it.id == "address" }
        assertEquals(LocalDate.of(2027, 3, 14), spring.dateOf(springAddress))
        assertEquals(LocalDate.of(2027, 4, 4), spring.deadlineOf(springAddress))

        val autumn = moving(moveDay = LocalDate.of(2027, 10, 31))
        val autumnAddress = autumn.steps.first { it.id == "address" }
        assertEquals(LocalDate.of(2027, 10, 17), autumn.dateOf(autumnAddress))
        assertEquals(LocalDate.of(2027, 11, 7), autumn.deadlineOf(autumnAddress))
    }

    @Test
    fun `a day is the same everywhere in the world at the same moment`() {
        // The same moment is a different day in different places, but the day of a plan is not a moment.
        TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Kiritimati"))
        val chapter = moving()
        assertEquals(LocalDate.of(2027, 2, 15), chapter.dateOf(chapter.steps.first { it.id == "address" }))
    }

    @Test
    fun `a moved anchor moves the plan and its deadlines`() {
        val chapter = moving().withAnchor("moveDay", LocalDate.of(2027, 4, 1))
        val address = chapter.steps.first { it.id == "address" }
        assertEquals(LocalDate.of(2027, 3, 18), chapter.dateOf(address))
        assertEquals(LocalDate.of(2027, 4, 8), chapter.deadlineOf(address))
        assertEquals(LocalDate.of(2027, 1, 1), chapter.dateOf(chapter.steps.first { it.id == "notice" }))
    }

    @Test
    fun `a step with its own day keeps it when the anchor moves`() {
        val own = LocalDate.of(2027, 1, 10)
        val chapter = moving().withStepDate("book-van", own).withAnchor("moveDay", LocalDate.of(2027, 4, 1))
        assertEquals(own, chapter.dateOf(chapter.steps.first { it.id == "book-van" }))
    }

    @Test
    fun `an unknown anchor leaves the steps undated, in plan order`() {
        val chapter = moving(moveDay = null)
        assertTrue(chapter.upcoming().all { it.date == null })
        assertNull(chapter.deadlineOf(chapter.steps.first { it.id == "address" }))
        assertEquals(listOf("book-van", "notice", "address", "boxes"), chapter.upcoming().ids)
    }

    @Test
    fun `reading the plan again gives the same days, in any time zone`() {
        val chapter = moving()
        val first = chapter.upcoming().map { it.step.id to it.date }
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
        assertEquals(first, chapter.upcoming().map { it.step.id to it.date })
        assertEquals(first, chapter.upcoming().map { it.step.id to it.date })
    }

    private companion object {
        /** Zones far from each other, with and without summer time. */
        val ZONES = listOf("UTC", "Europe/Stockholm", "America/New_York", "Pacific/Kiritimati", "Pacific/Midway")
    }
}
