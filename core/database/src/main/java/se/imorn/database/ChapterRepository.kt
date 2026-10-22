package se.imorn.database

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.Room
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.imorn.model.Chapter
import se.imorn.model.Entry
import se.imorn.model.EntryKind
import java.io.File
import java.io.IOException

/**
 * The chapters, saved in Room on the device. One instance for the whole app, so every screen reads the same
 * database: the flows emit again after each save, so a screen never shows a stale plan. Saves are whole chapters
 * in one transaction; change a chapter with the functions in `:core:model` (`complete`, `withAnchor` …) and save it.
 *
 * Photos and papers are files in the app's private storage ([KeptFiles]); this is the only way to add or delete
 * them, so deleting an entry or a chapter always deletes its files too.
 */
@SingleIn(AppScope::class)
class ChapterRepository internal constructor(private val database: ImornDatabase, private val files: KeptFiles) {
    @Inject constructor(context: Context) : this(
        Room.databaseBuilder(context, ImornDatabase::class.java, ImornDatabase.NAME)
            .addMigrations(*ImornDatabase.migrations)
            .build(),
        KeptFiles(File(context.filesDir, KeptFiles.FOLDER), context.contentResolver),
    )

    private val dao = database.chapters()

    /** Every chapter, oldest first. */
    val chapters: Flow<List<Chapter>> = dao.observeAll().map { rows -> rows.map { it.toChapter() } }

    fun chapter(id: String): Flow<Chapter?> = dao.observe(id).map { it?.toChapter() }

    suspend fun save(chapter: Chapter) {
        dao.save(chapter.toEntity(), chapter.anchorEntities(), chapter.phaseEntities(), chapter.stepEntities())
    }

    /** Deletes the chapter with its plan and everything kept in it, files included. */
    suspend fun delete(id: String) {
        dao.delete(id)
        files.deleteChapter(id)
    }

    /** What was kept in the chapter, latest first. */
    fun entries(chapterId: String): Flow<List<Entry>> =
        dao.observeEntries(chapterId).map { rows -> rows.map { it.toEntry() } }

    fun entry(id: String): Flow<Entry?> = dao.observeEntry(id).map { it?.toEntry() }

    /** Saves an entry, such as a note; its chapter must be saved already. */
    suspend fun save(entry: Entry) = dao.upsertEntry(entry.toEntity())

    /**
     * Keeps [source] (a photo from the camera or the picker, or a paper) as [entry]'s file in the app's private
     * storage and saves the entry. A paper without a name gets the one the file had. Returns the saved entry.
     */
    suspend fun keep(entry: Entry, source: Uri, mimeType: String? = null): Entry {
        val kept = files.keep(entry.chapterId, entry.id, source, mimeType)
        val name = if (entry.kind == EntryKind.Paper && entry.text == null) files.displayName(source) else entry.text
        val saved = entry.copy(file = kept.name, mimeType = kept.mimeType, text = name)
        try {
            save(saved)
        } catch (e: Exception) {
            files.delete(entry.chapterId, kept.name)
            throw e
        }
        return saved
    }

    /**
     * [keep], for what the user just picked or took: a file that can't be read (or whose read permission ran out)
     * is left out and null returned, so nothing half-kept is saved.
     */
    suspend fun keepIfReadable(entry: Entry, source: Uri, mimeType: String? = null): Entry? = try {
        keep(entry, source, mimeType)
    } catch (e: IOException) {
        Log.w(TAG, "Couldn't keep a picked file", e)
        null
    } catch (e: SecurityException) {
        Log.w(TAG, "Couldn't keep a picked file", e)
        null
    }

    /** Deletes an entry and its file. */
    suspend fun delete(entry: Entry) {
        dao.deleteEntry(entry.id)
        entry.file?.let { files.delete(entry.chapterId, it) }
    }

    /** The entry's kept file, if it has one. */
    fun fileOf(entry: Entry): File? = entry.file?.let { files.file(entry.chapterId, it) }

    /** The entry's thumbnail, if it has one that could be drawn. */
    fun thumbnailOf(entry: Entry): File? =
        entry.file?.let { files.thumbnail(entry.chapterId, it) }?.takeIf { it.exists() }

    /** Closes the database; for tests. */
    internal fun close() = database.close()

    private companion object {
        const val TAG = "ChapterRepository"
    }
}
