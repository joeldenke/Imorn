package se.imorn.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PlanTest {
    @Test fun `steps count from the anchor`() {
        val chapter = moving()
        val address = chapter.steps.first { it.id == "address" }
        assertEquals(LocalDate.of(2027, 2, 15), chapter.dateOf(address))
        assertEquals(LocalDate.of(2027, 3, 8), chapter.deadlineOf(address))
    }

    @Test fun `the list is dated steps by day, then undated ones in plan order, without long threads`() {
        assertEquals(listOf("notice", "book-van", "address", "boxes"), moving().upcoming().ids)
        assertEquals("notice", moving().next()?.step?.id)
    }

    @Test fun `with the anchor unknown the plan keeps the template's order and has no dates`() {
        val upcoming = moving(moveDay = null).upcoming()
        assertEquals(listOf("book-van", "notice", "address", "boxes"), upcoming.ids)
        assertTrue(upcoming.all { it.date == null })
    }

    @Test fun `moving the anchor moves the plan, but not a step with its own date`() {
        val own = LocalDate.of(2027, 1, 10)
        val chapter = moving().withStepDate("book-van", own).withAnchor("moveDay", LocalDate.of(2027, 4, 1))
        assertEquals(own, chapter.dateOf(chapter.steps.first { it.id == "book-van" }))
        assertEquals(LocalDate.of(2027, 3, 18), chapter.dateOf(chapter.steps.first { it.id == "address" }))
    }

    @Test fun `clearing a step's own date counts from the anchor again`() {
        val chapter = moving().withStepDate("address", LocalDate.of(2027, 1, 1)).withStepDate("address", null)
        assertEquals(LocalDate.of(2027, 2, 15), chapter.dateOf(chapter.steps.first { it.id == "address" }))
    }

    @Test fun `a done step falls through the now-line, latest first`() {
        val chapter = moving()
            .complete("notice", LocalDate.of(2026, 12, 1))
            .complete("book-van", LocalDate.of(2027, 1, 5))
        val timeline = chapter.timeline()
        assertEquals(listOf("address", "boxes", "deposit"), timeline.ahead.ids)
        assertEquals(listOf("book-van", "notice"), timeline.behind.ids)
    }

    @Test fun `a step that doesn't apply leaves both sides of the line, and reopening brings it back`() {
        val skipped = moving().notApplicable("notice")
        assertTrue("notice" !in skipped.timeline().ahead.ids && "notice" !in skipped.timeline().behind.ids)
        val reopened = skipped.reopen("notice")
        assertEquals("notice", reopened.next()?.step?.id)
        assertNull(reopened.steps.first { it.id == "notice" }.doneOn)
    }

    @Test fun `own steps go last in the plan and are marked as own`() {
        val chapter = moving(moveDay = null).withOwnStep(Step("own-1", "Call grandma"))
        assertEquals("own-1", chapter.upcoming().last().step.id)
        assertEquals(false, chapter.steps.last().fromTemplate)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `an own step cannot reuse an id`() {
        moving().withOwnStep(Step("boxes", "Boxes again"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `changing a missing step is a bug`() {
        moving().complete("nope", LocalDate.of(2027, 1, 1))
    }
}
