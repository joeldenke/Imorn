package se.imorn.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StoryTest {
    private val chapter = moving()
        .complete("notice", LocalDate.of(2026, 12, 1))
        .complete("book-van", LocalDate.of(2027, 1, 5))

    private fun entry(id: String, kind: EntryKind, on: LocalDate, stepId: String? = null) =
        Entry(id, chapter.id, kind, on, stepId = stepId)

    @Test
    fun `done steps carry what was kept with them`() {
        val photo = entry("p", EntryKind.Photo, LocalDate.of(2027, 1, 5), stepId = "book-van")
        val note = entry("n", EntryKind.Note, LocalDate.of(2027, 1, 6), stepId = "book-van")
        val story = chapter.story(listOf(note, photo))
        val done = story.first() as StoryItem.Done
        assertEquals("book-van", done.step.step.id)
        assertEquals(listOf("p", "n"), done.kept.map { it.id })
        assertEquals(2, story.size)
    }

    @Test
    fun `the story is latest first, with things kept on their own in between`() {
        val note = entry("n", EntryKind.Note, LocalDate.of(2026, 12, 20))
        val story = chapter.story(listOf(note))
        assertEquals(
            listOf(LocalDate.of(2027, 1, 5), LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 1)),
            story.map { it.day },
        )
        assertTrue(story[1] is StoryItem.Kept)
    }

    @Test
    fun `a photo for a step that is open again stands on its own`() {
        val photo = entry("p", EntryKind.Photo, LocalDate.of(2027, 1, 5), stepId = "book-van")
        val story = chapter.reopen("book-van").story(listOf(photo))
        assertEquals(listOf("p"), story.filterIsInstance<StoryItem.Kept>().map { it.entry.id })
    }

    @Test
    fun `papers are private and never part of the story`() {
        val paper = entry("contract", EntryKind.Paper, LocalDate.of(2027, 1, 2))
        assertTrue(paper.isPrivate)
        assertTrue(chapter.story(listOf(paper)).none { it is StoryItem.Kept })
        assertEquals(listOf("contract"), chapter.papers(listOf(paper)).map { it.id })
    }

    @Test
    fun `entries from another chapter are left out`() {
        val other = Entry("x", "other", EntryKind.Note, LocalDate.of(2027, 1, 1))
        assertTrue(chapter.story(listOf(other)).none { it is StoryItem.Kept })
        assertTrue(chapter.papers(listOf(other.copy(kind = EntryKind.Paper))).isEmpty())
    }
}
