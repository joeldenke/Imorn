package se.imorn.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LongThreadTest {
    /** The moving day is 2027-03-01; a tax return the following spring is far ahead, the boxes are not. */
    private fun chapter(vararg extra: Step, moveDay: LocalDate? = LocalDate.of(2027, 3, 1)) = Chapter(
        id = "move",
        title = "Moving",
        anchors = listOf(Anchor("moveDay", "Moving day", moveDay)),
        phases = listOf(Phase("plan", "Plan")),
        steps = listOf(
            Step("boxes", "Get boxes", phaseId = "plan", at = RelativeTime.Days("moveDay", -28)),
            Step("tax", "Declare the sale", at = RelativeTime.NextYear("moveDay", 3)),
        ) + extra,
        createdOn = LocalDate.of(2026, 10, 21),
    )

    @Test fun `a step far beyond the anchor is a long thread even without a template mark`() {
        val tax = chapter().steps.first { it.id == "tax" }
        assertFalse(tax.longThread)
        assertTrue(chapter().isLongThread(tax))
    }

    @Test fun `a step near the anchor is not a long thread`() {
        val boxes = chapter().steps.first { it.id == "boxes" }
        assertFalse(chapter().isLongThread(boxes))
    }

    @Test fun `a template mark wins even without a day`() {
        val marked = Step("guarantee", "The plumber's guarantee", longThread = true)
        assertTrue(chapter(marked, moveDay = null).isLongThread(marked))
    }

    @Test fun `a step without a day is only a long thread when the template marks it`() {
        val undated = Step("later", "Something later")
        assertFalse(chapter(undated).isLongThread(undated))
    }

    @Test fun `the anchor line is the latest known anchor`() {
        val twoAnchors = Chapter(
            id = "sale",
            title = "Sale",
            anchors = listOf(
                Anchor("contract", "Contract", LocalDate.of(2027, 1, 1)),
                Anchor("handover", "Handover", LocalDate.of(2027, 6, 1)),
            ),
            createdOn = LocalDate.of(2026, 10, 21),
        )
        assertEquals(LocalDate.of(2027, 6, 1), twoAnchors.anchorLine())
    }

    @Test fun `with every anchor unknown the anchor line is null`() {
        val unknown = Chapter(
            id = "x",
            title = "X",
            anchors = listOf(Anchor("d", "Day")),
            createdOn = LocalDate.of(2026, 10, 21),
        )
        assertEquals(null, unknown.anchorLine())
    }

    @Test fun `far ahead lists only the open far steps, soonest first`() {
        val near = Step("near", "Near", at = RelativeTime.Days("moveDay", 30))
        val far = Step("far", "Far", at = RelativeTime.Months("moveDay", 3))
        val done = Step("done", "Done", at = RelativeTime.Months("moveDay", 4), status = StepStatus.Done)
        val farAhead = chapter(near, far, done).farAhead()
        assertEquals(listOf("far", "tax"), farAhead.ids)
    }

    @Test fun `remaining is the plan without the long threads`() {
        assertEquals(listOf("boxes"), chapter().remaining().ids)
    }

    @Test fun `a chapter with only long threads left is complete`() {
        val onlyThread = Step("tax", "Declare the sale", longThread = true)
        val threadsOnly = Chapter(
            id = "sale",
            title = "Sale",
            steps = listOf(onlyThread),
            createdOn = LocalDate.of(2026, 10, 21),
        )
        assertTrue(threadsOnly.isComplete())
        assertFalse(chapter().isComplete())
    }

    @Test fun `completing does the plan steps and keeps the far ones as long threads`() {
        val completion = chapter(Step("own", "Call grandma")).completed(LocalDate.of(2027, 4, 1))
        val byId = completion.chapter.steps.associateBy { it.id }
        assertEquals(StepStatus.Done, byId.getValue("boxes").status)
        assertEquals(LocalDate.of(2027, 4, 1), byId.getValue("boxes").doneOn)
        assertEquals(StepStatus.Open, byId.getValue("tax").status)
        assertTrue(byId.getValue("tax").longThread)
        assertEquals(listOf("tax"), completion.farAhead.ids)
        assertEquals(1, completion.keepCount)
    }

    @Test fun `resting keeps the threads and leaves the chapter in view`() {
        val resting = chapter().completedAndResting(LocalDate.of(2027, 4, 1))
        assertEquals(ChapterState.Resting, resting.state)
        assertEquals(listOf("tax"), resting.longThreads().ids)
    }

    @Test fun `closing lets the threads go`() {
        val closed = chapter().completedAndClosed(LocalDate.of(2027, 4, 1))
        assertEquals(ChapterState.Closed, closed.state)
        assertTrue(closed.longThreads().isEmpty())
        assertEquals(StepStatus.NotApplicable, closed.steps.first { it.id == "tax" }.status)
    }

    @Test fun `completing with nothing far ahead asks nothing`() {
        val plain = Chapter(
            id = "move",
            title = "Moving",
            anchors = listOf(Anchor("moveDay", "Moving day", LocalDate.of(2027, 3, 1))),
            phases = listOf(Phase("plan", "Plan")),
            steps = listOf(Step("boxes", "Get boxes", phaseId = "plan", at = RelativeTime.Days("moveDay", -28))),
            createdOn = LocalDate.of(2026, 10, 21),
        )
        val completion = plain.completed(LocalDate.of(2027, 4, 1))
        assertEquals(0, completion.keepCount)
        assertTrue(completion.chapter.isComplete())
    }
}
