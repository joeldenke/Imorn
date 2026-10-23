package se.imorn.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SubflowTest {
    @Test fun `each phase is a subflow with its progress, own steps last`() {
        val chapter = moving().complete("notice", LocalDate.of(2026, 12, 1)).withOwnStep(Step("own-1", "Call grandma"))
        val subflows = chapter.subflows()
        assertEquals(listOf("plan", "papers", null), subflows.map { it.phase?.id })
        val plan = subflows[0]
        assertEquals(1, plan.done)
        assertEquals(3, plan.total)
        assertEquals(listOf("book-van", "boxes"), plan.open.ids)
        assertEquals("book-van", plan.next?.step?.id)
    }

    @Test fun `a finished subflow stays, with nothing next`() {
        val chapter = moving().complete("address", LocalDate.of(2027, 2, 1))
        val papers = chapter.subflows().first { it.phase?.id == "papers" }
        assertTrue(papers.finished)
        assertEquals(1, papers.done)
        assertNull(papers.next)
    }

    @Test fun `steps that don't apply count for nothing, and a phase of only those is gone`() {
        val subflows = moving().notApplicable("address").notApplicable("notice").subflows()
        assertEquals(listOf("plan"), subflows.map { it.phase?.id })
        assertEquals(2, subflows.single().total)
    }

    @Test fun `long threads are not part of a subflow`() {
        val total = moving().subflows().sumOf { it.total }
        assertEquals(4, total)
    }
}
