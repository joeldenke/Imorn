package se.imorn.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Imorn's database: chapters, their plans and what was kept along the way. On the device only, in the app's private
 * storage, and left out of backups (`app/src/main/res/xml`). A new version needs a migration and a test of it; the
 * exported schema in `schemas/` is what the migration starts from.
 */
@Database(
    entities = [ChapterEntity::class, AnchorEntity::class, PhaseEntity::class, StepEntity::class, EntryEntity::class],
    version = 1,
    exportSchema = true,
)
internal abstract class ImornDatabase : RoomDatabase() {
    abstract fun chapters(): ChapterDao

    companion object {
        const val NAME = "imorn.db"
    }
}
