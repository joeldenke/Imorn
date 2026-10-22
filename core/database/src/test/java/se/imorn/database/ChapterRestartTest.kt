package se.imorn.database

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import se.imorn.model.Anchor
import se.imorn.model.Chapter
import se.imorn.model.ChapterState
import se.imorn.model.Entry
import se.imorn.model.EntryKind
import se.imorn.model.Phase
import se.imorn.model.RelativeTime
import se.imorn.model.Step
import se.imorn.model.dateOf
import se.imorn.model.deadlineOf
import se.imorn.model.withAnchor
import se.imorn.model.withStepDate
import java.io.File
import java.time.LocalDate
import java.util.TimeZone

/**
 * The data survives a restart: the app is closed and opened again, and the plan stands as it was left. The days are
 * stored as days, so they never move with the phone's time zone.
 */
@RunWith(RobolectricTestRunner::class)
class ChapterRestartTest {
    private val context: Context = RuntimeEnvironment.getApplication()
    private val name = "restart-test.db"
    private val keptRoot = File(context.filesDir, "kept-restart-test")
    private val originalZone = TimeZone.getDefault()
    private var repository: ChapterRepository? = null

    @After
    fun clean() {
        repository?.close()
        repository = null
        context.deleteDatabase(name)
        keptRoot.deleteRecursively()
        TimeZone.setDefault(originalZone)
    }

    /** The app's database, opened the way it is opened when the app starts. */
    private fun openTheApp(): ChapterRepository {
        val database = Room.databaseBuilder(context, ImornDatabase::class.java, name)
            .addMigrations(*ImornDatabase.migrations)
            .allowMainThreadQueries()
            .build()
        return ChapterRepository(database, KeptFiles(keptRoot, context.contentResolver)).also { repository = it }
    }

    /** Closes the app, as the phone does when the app is closed. */
    private fun closeTheApp() {
        repository?.close()
        repository = null
    }

    private fun moving() = Chapter(
        id = "move",
        title = "Flytten",
        templateId = "move",
        templateVersion = 1,
        anchors = listOf(Anchor("moveDay", "Flyttdag", LocalDate.of(2027, 3, 1))),
        phases = listOf(Phase("plan", "Planera")),
        steps = listOf(
            Step("book-van", "Boka flyttbil", phaseId = "plan", at = RelativeTime.Days("moveDay", -56)),
            Step(
                "address",
                "Gör flyttanmälan",
                phaseId = "plan",
                at = RelativeTime.Days("moveDay", -14),
                hardDeadline = RelativeTime.Days("moveDay", 7),
            ),
        ),
        state = ChapterState.Active,
        createdOn = LocalDate.of(2026, 10, 21),
    )

    @Test
    fun `a chapter is still there after a restart`() = runBlocking {
        val chapter = moving()
        openTheApp().save(chapter)
        closeTheApp()
        assertEquals(chapter, openTheApp().chapter("move").first())
    }

    @Test
    fun `the plan's days are the same after a restart`() = runBlocking {
        openTheApp().save(moving())
        closeTheApp()
        val chapter = openTheApp().chapter("move").first()!!
        val address = chapter.steps.first { it.id == "address" }
        assertEquals(LocalDate.of(2027, 2, 15), chapter.dateOf(address))
        assertEquals(LocalDate.of(2027, 3, 8), chapter.deadlineOf(address))
    }

    @Test
    fun `a moved anchor survives a restart`() = runBlocking {
        openTheApp().save(moving().withAnchor("moveDay", LocalDate.of(2027, 4, 1)))
        closeTheApp()
        val chapter = openTheApp().chapter("move").first()!!
        val address = chapter.steps.first { it.id == "address" }
        assertEquals(LocalDate.of(2027, 3, 18), chapter.dateOf(address))
        assertEquals(LocalDate.of(2027, 4, 8), chapter.deadlineOf(address))
    }

    @Test
    fun `a step with its own day keeps it after a restart`() = runBlocking {
        openTheApp().save(moving().withStepDate("book-van", LocalDate.of(2027, 1, 10)))
        closeTheApp()
        val chapter = openTheApp().chapter("move").first()!!
        assertEquals(LocalDate.of(2027, 1, 10), chapter.dateOf(chapter.steps.first { it.id == "book-van" }))
    }

    @Test
    fun `the days do not move with the phone's time zone`() = runBlocking {
        openTheApp().save(moving())
        closeTheApp()
        TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Kiritimati"))
        val here = openTheApp().chapter("move").first()!!
        assertEquals(LocalDate.of(2027, 2, 15), here.dateOf(here.steps.first { it.id == "address" }))
        closeTheApp()
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
        val elsewhere = openTheApp().chapter("move").first()!!
        assertEquals(LocalDate.of(2027, 2, 15), elsewhere.dateOf(elsewhere.steps.first { it.id == "address" }))
    }

    @Test
    fun `what was kept survives a restart`() = runBlocking {
        val app = openTheApp()
        app.save(moving())
        app.save(Entry("e1", "move", EntryKind.Note, LocalDate.of(2027, 1, 10), text = "Packade köket"))
        closeTheApp()
        assertEquals(
            listOf(Entry("e1", "move", EntryKind.Note, LocalDate.of(2027, 1, 10), text = "Packade köket")),
            openTheApp().entries("move").first(),
        )
    }
}
