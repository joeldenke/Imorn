package se.imorn.database

import android.content.Context
import androidx.room.Room
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.imorn.model.Chapter
import se.imorn.model.Entry

/**
 * The chapters, saved in Room on the device. One instance for the whole app, so every screen reads the same
 * database: the flows emit again after each save, so a screen never shows a stale plan. Saves are whole chapters
 * in one transaction; change a chapter with the functions in `:core:model` (`complete`, `withAnchor` …) and save it.
 */
@SingleIn(AppScope::class)
class ChapterRepository internal constructor(private val database: ImornDatabase) {
    @Inject constructor(context: Context) : this(
        Room.databaseBuilder(context, ImornDatabase::class.java, ImornDatabase.NAME).build(),
    )

    private val dao = database.chapters()

    /** Every chapter, oldest first. */
    val chapters: Flow<List<Chapter>> = dao.observeAll().map { rows -> rows.map { it.toChapter() } }

    fun chapter(id: String): Flow<Chapter?> = dao.observe(id).map { it?.toChapter() }

    suspend fun save(chapter: Chapter) {
        dao.save(chapter.toEntity(), chapter.anchorEntities(), chapter.phaseEntities(), chapter.stepEntities())
    }

    /** Deletes the chapter with its plan and everything kept in it. */
    suspend fun delete(id: String) = dao.delete(id)

    /** What was kept in the chapter, latest first. */
    fun entries(chapterId: String): Flow<List<Entry>> =
        dao.observeEntries(chapterId).map { rows -> rows.map { it.toEntry() } }

    /** Saves an entry; its chapter must be saved already. */
    suspend fun save(entry: Entry) = dao.upsertEntry(entry.toEntity())

    suspend fun deleteEntry(id: String) = dao.deleteEntry(id)

    /** Closes the database; for tests. */
    internal fun close() = database.close()
}
