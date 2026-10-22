package se.imorn.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import se.imorn.model.Entry
import se.imorn.model.EntryKind
import java.io.File
import java.time.LocalDate

/**
 * Every migration from the exported schema it starts from: the old database is made with the tables in `schemas/`,
 * filled, and opened with Room, which runs the migrations and then checks the result against today's entities.
 */
@RunWith(RobolectricTestRunner::class)
class MigrationTest {
    private val context: Context = RuntimeEnvironment.getApplication()
    private val name = "migration-test.db"

    @After fun delete() {
        context.deleteDatabase(name)
    }

    /** Creates database version [version] from its exported schema, the way that app version left it. */
    private fun createVersion(version: Int, fill: SQLiteDatabase.() -> Unit) {
        val schema = File("schemas/se.imorn.database.ImornDatabase/$version.json").readText()
        val database = JSONObject(schema).getJSONObject("database")
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { db ->
            val entities = database.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                val table = entity.getString("tableName")
                db.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
                val indices = entity.optJSONArray("indices") ?: continue
                for (j in 0 until indices.length()) {
                    db.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", table))
                }
            }
            val setup = database.getJSONArray("setupQueries")
            for (i in 0 until setup.length()) db.execSQL(setup.getString(i))
            db.fill()
            db.version = version
        }
    }

    @Test fun `version 1 to 2 keeps chapters and entries, which have no file`() = runBlocking {
        val day = LocalDate.of(2027, 1, 1).toEpochDay()
        createVersion(1) {
            execSQL("INSERT INTO chapters VALUES ('move', 'Flytten', 'move', 1, 'Active', $day)")
            execSQL("INSERT INTO entries VALUES ('e1', 'move', 'Note', $day, 'Packade köket', NULL)")
        }
        val database = Room.databaseBuilder(context, ImornDatabase::class.java, name)
            .addMigrations(*ImornDatabase.migrations)
            .allowMainThreadQueries()
            .build()
        val repository = ChapterRepository(database, KeptFiles(File(context.filesDir, "kept"), context.contentResolver))
        try {
            assertEquals("Flytten", repository.chapter("move").first()?.title)
            assertEquals(
                listOf(Entry("e1", "move", EntryKind.Note, LocalDate.of(2027, 1, 1), text = "Packade köket")),
                repository.entries("move").first(),
            )
        } finally {
            repository.close()
        }
    }
}
