package se.imorn.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TomorrowTest {
    @Test fun `late steps first, then the next step in each chapter, never more than three`() {
        // 2027-01-10: "notice" (2026-12-01) and "book-van" (2027-01-04) are late, "address" is next.
        val plan = tomorrowPlan(listOf(moving(), moving(id = "renovate")), LocalDate.of(2027, 1, 10))
        assertEquals(TomorrowPlan.MAX_ITEMS, plan.items.size)
        assertTrue(plan.items.all { it.late })
        assertEquals(listOf("notice", "notice", "book-van"), plan.items.map { it.planned.step.id })
    }

    @Test fun `with nothing late it is the next step per chapter, soonest first`() {
        val soon = moving(moveDay = LocalDate.of(2027, 3, 1))
        val later = moving(id = "later", moveDay = LocalDate.of(2027, 6, 1))
        val plan = tomorrowPlan(listOf(later, soon), LocalDate.of(2026, 11, 1))
        assertEquals(listOf("move", "later"), plan.items.map { it.planned.chapterId })
        assertTrue(plan.items.none { it.late })
    }

    @Test fun `an undated next step still shows, after the dated ones`() {
        val unknown = moving(id = "unknown", moveDay = null)
        val plan = tomorrowPlan(listOf(unknown, moving()), LocalDate.of(2026, 11, 1))
        assertEquals(listOf("move", "unknown"), plan.items.map { it.planned.chapterId })
    }

    @Test fun `resting and closed chapters plan nothing, but a resting one's long thread shows when it is close`() {
        val resting = moving(state = ChapterState.Resting)
        val closed = moving(id = "closed", state = ChapterState.Closed)
        // The deposit is due 2027-04-01, within 60 days of 2027-03-01.
        val plan = tomorrowPlan(listOf(resting, closed), LocalDate.of(2027, 3, 1))
        assertTrue(plan.items.isEmpty())
        assertEquals(listOf("deposit" to "move"), plan.later.map { it.step.id to it.chapterId })
    }

    @Test fun `long threads far ahead stay hidden`() {
        assertTrue(tomorrowPlan(listOf(moving()), LocalDate.of(2026, 11, 1)).later.isEmpty())
    }

    @Test fun `no chapters, nothing to show`() {
        val plan = tomorrowPlan(emptyList(), LocalDate.of(2026, 11, 1))
        assertTrue(plan.items.isEmpty() && plan.later.isEmpty())
    }
}
