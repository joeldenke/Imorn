package se.imorn.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class RelativeTimeTest {
    private val moveDay = LocalDate.of(2027, 3, 1)

    @Test fun `days count before and after the anchor`() {
        assertEquals(LocalDate.of(2027, 2, 15), RelativeTime.Days("moveDay", -14).resolve(moveDay))
        assertEquals(LocalDate.of(2027, 3, 8), RelativeTime.Days("moveDay", 7).resolve(moveDay))
    }

    @Test fun `months keep the day, or take the month's last day`() {
        assertEquals(LocalDate.of(2026, 12, 1), RelativeTime.Months("moveDay", -3).resolve(moveDay))
        assertEquals(LocalDate.of(2027, 2, 28), RelativeTime.Months("a", 1).resolve(LocalDate.of(2027, 1, 31)))
    }

    @Test fun `next year is the first of the month the year after`() {
        assertEquals(LocalDate.of(2028, 4, 1), RelativeTime.NextYear("sale", 4).resolve(LocalDate.of(2027, 11, 30)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a month outside 1-12 is refused`() {
        RelativeTime.NextYear("sale", 13)
    }
}
