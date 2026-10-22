package se.imorn.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Imorn's database: chapters, their plans and what was kept along the way. On the device only, in the app's private
 * storage, and left out of backups (`app/src/main/res/xml`). A new version needs a migration and a test of it; the
 * exported schema in `schemas/` is what the migration starts from.
 *
 * Versions: 1 chapters and their plans (phase 1); 2 kept files on entries, for photos and papers (phase 2).
 */
@Database(
    entities = [ChapterEntity::class, AnchorEntity::class, PhaseEntity::class, StepEntity::class, EntryEntity::class],
    version = 2,
    exportSchema = true,
)
internal abstract class ImornDatabase : RoomDatabase() {
    abstract fun chapters(): ChapterDao

    companion object {
        const val NAME = "imorn.db"

        /** Every migration, in order. */
        val migrations: Array<Migration> = arrayOf(Migration1To2)
    }
}

/** Version 2: an entry can keep a file (a photo or a paper) and its type. Existing entries keep none. */
internal object Migration1To2 : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `entries` ADD COLUMN `file` TEXT")
        db.execSQL("ALTER TABLE `entries` ADD COLUMN `mimeType` TEXT")
    }
}
