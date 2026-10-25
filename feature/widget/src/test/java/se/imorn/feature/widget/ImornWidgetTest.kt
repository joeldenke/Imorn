package se.imorn.feature.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import se.imorn.model.Chapter
import se.imorn.model.ChapterState
import se.imorn.model.Step
import se.imorn.model.StepStatus
import java.time.LocalDate

/** The widget shows the first item of the same plan as the start screen, so the two never disagree. */
class ImornWidgetTest {
    private val today = LocalDate.of(2026, 10, 23)

    private fun chapter(
        id: String,
        step: Step,
        state: ChapterState = ChapterState.Active,
    ) = Chapter(id = id, title = id, steps = listOf(step), state = state, createdOn = today)

    @Test
    fun showsTheNextOpenStep() {
        val move = chapter("move", Step(id = "address-change", title = "Report your move"))

        val item = nextStepOf(listOf(move), today)

        assertEquals("address-change", item?.planned?.step?.id)
        assertEquals("move", item?.planned?.chapterId)
    }

    @Test
    fun aLateStepComesFirst() {
        val late = chapter("move", Step(id = "late", title = "Late", date = today.minusDays(2)))
        val next = chapter("bathroom", Step(id = "next", title = "Next", date = today))

        val item = nextStepOf(listOf(next, late), today)

        assertEquals("late", item?.planned?.step?.id)
    }

    @Test
    fun closedChaptersAreLeftOut() {
        val closed = chapter("done", Step(id = "old", title = "Old"), state = ChapterState.Closed)

        assertNull(nextStepOf(listOf(closed), today))
    }

    @Test
    fun nothingToShowWhenThereAreNoOpenSteps() {
        val done = chapter("move", Step(id = "address-change", title = "Report your move", status = StepStatus.Done))

        assertNull(nextStepOf(listOf(done), today))
    }
}
