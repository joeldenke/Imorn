package se.imorn.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class MilestoneTest {
    private val today = LocalDate.of(2027, 2, 10)

    @Test fun `the nearest anchor ahead in an active chapter, counted in days`() {
        val soon = moving(moveDay = LocalDate.of(2027, 3, 1), id = "soon")
        val later = moving(moveDay = LocalDate.of(2027, 6, 1), id = "later")
        val milestone = nextMilestone(listOf(later, soon), today)!!
        assertEquals("soon", milestone.chapterId)
        assertEquals("moveDay", milestone.anchor.id)
        assertEquals(19L, milestone.daysFrom(today))
    }

    @Test fun `today counts, the past and unknown dates do not`() {
        assertEquals(0L, nextMilestone(listOf(moving(moveDay = today)), today)!!.daysFrom(today))
        assertNull(nextMilestone(listOf(moving(moveDay = today.minusDays(1))), today))
        assertNull(nextMilestone(listOf(moving(moveDay = null)), today))
    }

    @Test fun `resting and finished chapters count down to nothing`() {
        val resting = moving(state = ChapterState.Resting)
        val closed = moving(id = "closed", state = ChapterState.Closed)
        assertNull(nextMilestone(listOf(resting, closed), today))
    }

    @Test fun `progress counts the steps that apply, without long threads`() {
        val chapter = moving().complete("notice", LocalDate.of(2026, 12, 1)).notApplicable("boxes")
        assertEquals(Progress(done = 1, total = 3), chapter.progress())
        assertEquals(1f / 3, chapter.progress().share, 0.001f)
        assertEquals(0f, Progress(0, 0).share)
    }
}
