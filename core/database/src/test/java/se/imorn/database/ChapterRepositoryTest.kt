package se.imorn.database

import android.content.Context
import android.net.Uri
import androidx.room.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import se.imorn.model.Anchor
import se.imorn.model.Chapter
import se.imorn.model.ChapterState
import se.imorn.model.Entry
import se.imorn.model.EntryKind
import se.imorn.model.LinkRef
import se.imorn.model.PDF
import se.imorn.model.Phase
import se.imorn.model.RelativeTime
import se.imorn.model.Step
import se.imorn.model.StepStatus
import se.imorn.model.complete
import java.io.File
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class ChapterRepositoryTest {
    private val context: Context = RuntimeEnvironment.getApplication()
    private val keptRoot = File(context.filesDir, "kept-test")
    private val repository = ChapterRepository(
        Room.inMemoryDatabaseBuilder(context, ImornDatabase::class.java).allowMainThreadQueries().build(),
        KeptFiles(keptRoot, context.contentResolver),
    )

    @After fun close() {
        repository.close()
        keptRoot.deleteRecursively()
    }

    /** A file outside the app's kept files, as the picker or the camera would hand one over. */
    private fun source(name: String, bytes: ByteArray): Uri =
        Uri.fromFile(File(context.cacheDir, name).apply { writeBytes(bytes) })

    private fun moving(id: String = "move", createdOn: LocalDate = LocalDate.of(2026, 10, 21)) = Chapter(
        id = id,
        title = "Flytten till Gävle",
        templateId = "move",
        templateVersion = 1,
        anchors = listOf(Anchor("moveDay", "Flyttdag", LocalDate.of(2027, 3, 1)), Anchor("keys", "Nycklar")),
        phases = listOf(Phase("plan", "Planera"), Phase("papers", "Papper")),
        steps = listOf(
            Step(
                id = "address-change",
                title = "Gör flyttanmälan",
                phaseId = "papers",
                why = "Senast en vecka efter flytten.",
                at = RelativeTime.Days("moveDay", -14),
                hardDeadline = RelativeTime.Days("moveDay", 7),
                links = listOf(LinkRef("skatteverket.flytt"), LinkRef("adressandring.eftersandning")),
            ),
            Step(
                id = "terminate-lease",
                title = "Säg upp hyreskontraktet",
                phaseId = "plan",
                at = RelativeTime.Months("moveDay", -3),
                date = LocalDate.of(2026, 11, 20),
                note = "Ring hyresvärden",
            ),
            Step(
                id = "meter-readings",
                title = "Läs av mätarna",
                at = RelativeTime.Days("moveDay", 0),
                photoPrompt = "Fota mätarställningarna",
                status = StepStatus.NotApplicable,
            ),
            Step(
                id = "tax-return",
                title = "Deklarera",
                at = RelativeTime.NextYear("moveDay", 4),
                longThread = true,
            ),
            Step(id = "own-1", title = "Ring mormor", fromTemplate = false),
        ),
        state = ChapterState.Active,
        createdOn = createdOn,
    ).complete("terminate-lease", LocalDate.of(2026, 11, 21))

    @Test fun `a saved chapter reads back exactly`() = runBlocking {
        val chapter = moving()
        repository.save(chapter)
        assertEquals(chapter, repository.chapter("move").first())
    }

    @Test fun `saving again replaces the plan, in the new order`() = runBlocking {
        val chapter = moving()
        repository.save(chapter)
        val changed = chapter.copy(
            title = "Flytten",
            state = ChapterState.Resting,
            steps = chapter.steps.drop(1).reversed(),
            phases = chapter.phases.take(1),
        )
        repository.save(changed)
        assertEquals(changed, repository.chapter("move").first())
    }

    @Test fun `chapters are listed oldest first`() = runBlocking {
        repository.save(moving(id = "later", createdOn = LocalDate.of(2026, 12, 1)))
        repository.save(moving(id = "earlier", createdOn = LocalDate.of(2026, 1, 1)))
        assertEquals(listOf("earlier", "later"), repository.chapters.first().map { it.id })
    }

    @Test fun `a missing chapter is null`() = runBlocking {
        assertNull(repository.chapter("nope").first())
    }

    @Test fun `entries are latest first`() = runBlocking {
        repository.save(moving())
        val note = Entry("e1", "move", EntryKind.Note, LocalDate.of(2027, 1, 1), text = "Packade köket")
        val photo = Entry("e2", "move", EntryKind.Photo, LocalDate.of(2027, 2, 1), stepId = "meter-readings")
        repository.save(note)
        repository.save(photo)
        assertEquals(listOf(photo, note), repository.entries("move").first())
    }

    @Test fun `deleting a chapter takes its plan and entries with it, and nothing else`() = runBlocking {
        repository.save(moving())
        repository.save(moving(id = "other"))
        repository.save(Entry("e1", "move", EntryKind.Note, LocalDate.of(2027, 1, 1), text = "Packade köket"))
        repository.delete("move")
        assertNull(repository.chapter("move").first())
        assertTrue(repository.entries("move").first().isEmpty())
        assertEquals(listOf("other"), repository.chapters.first().map { it.id })
        assertEquals(5, repository.chapter("other").first()?.steps?.size)
    }

    @Test fun `a kept paper is copied into the chapter's folder, and deleting it deletes the file`() = runBlocking {
        repository.save(moving())
        val bytes = "%PDF-1.4 not really".toByteArray()
        val paper = Entry("lease", "move", EntryKind.Paper, LocalDate.of(2027, 1, 1), text = "Lease")
        val saved = repository.keep(paper, source("lease.pdf", bytes), mimeType = PDF)
        assertEquals("lease.pdf", saved.file)
        assertEquals(PDF, saved.mimeType)
        assertEquals("Lease", saved.text)
        assertEquals(saved, repository.entry("lease").first())
        val file = repository.fileOf(saved)
        assertNotNull(file)
        assertTrue(file!!.readBytes().contentEquals(bytes))
        repository.delete(saved)
        assertNull(repository.entry("lease").first())
        assertFalse(file.exists())
    }

    @Test fun `deleting a chapter deletes its files and no other chapter's`() = runBlocking {
        repository.save(moving())
        repository.save(moving(id = "other"))
        val paper = Entry("p1", "move", EntryKind.Paper, LocalDate.of(2027, 1, 1))
        val kept = repository.keep(paper, source("a.pdf", byteArrayOf(1, 2, 3)), mimeType = PDF)
        val other = repository.keep(paper.copy(id = "p2", chapterId = "other"), source("b.pdf", byteArrayOf(4)), PDF)
        repository.delete("move")
        assertFalse(repository.fileOf(kept)!!.exists())
        assertTrue(repository.fileOf(other)!!.exists())
        assertEquals(listOf(other), repository.entries("other").first())
    }

    @Test fun `ids with odd characters become safe file names`() {
        assertEquals("a_b_c-1", KeptFiles.safe("a/b.c-1"))
    }
}
