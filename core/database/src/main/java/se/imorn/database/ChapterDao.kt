package se.imorn.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
internal abstract class ChapterDao {
    @Transaction
    @Query("SELECT * FROM chapters ORDER BY createdOn, id")
    abstract fun observeAll(): Flow<List<ChapterWithParts>>

    @Transaction
    @Query("SELECT * FROM chapters WHERE id = :id")
    abstract fun observe(id: String): Flow<ChapterWithParts?>

    /**
     * Saves a whole chapter in one transaction: the chapter row is updated in place (so nothing cascades), and its
     * anchors, phases and steps are replaced, so the stored plan is always exactly the one that was saved.
     */
    @Transaction
    open suspend fun save(
        chapter: ChapterEntity,
        anchors: List<AnchorEntity>,
        phases: List<PhaseEntity>,
        steps: List<StepEntity>,
    ) {
        upsertChapter(chapter)
        deleteAnchors(chapter.id)
        deletePhases(chapter.id)
        deleteSteps(chapter.id)
        insertAnchors(anchors)
        insertPhases(phases)
        insertSteps(steps)
    }

    /** Deletes the chapter and, through the foreign keys, everything in it. */
    @Query("DELETE FROM chapters WHERE id = :id")
    abstract suspend fun delete(id: String)

    @Query("SELECT * FROM entries WHERE chapterId = :chapterId ORDER BY day DESC, id")
    abstract fun observeEntries(chapterId: String): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE id = :id")
    abstract fun observeEntry(id: String): Flow<EntryEntity?>

    @Upsert
    abstract suspend fun upsertEntry(entry: EntryEntity)

    @Query("DELETE FROM entries WHERE id = :id")
    abstract suspend fun deleteEntry(id: String)

    @Upsert
    abstract suspend fun upsertChapter(chapter: ChapterEntity)

    @Query("DELETE FROM anchors WHERE chapterId = :chapterId")
    abstract suspend fun deleteAnchors(chapterId: String)

    @Query("DELETE FROM phases WHERE chapterId = :chapterId")
    abstract suspend fun deletePhases(chapterId: String)

    @Query("DELETE FROM steps WHERE chapterId = :chapterId")
    abstract suspend fun deleteSteps(chapterId: String)

    @Insert
    abstract suspend fun insertAnchors(anchors: List<AnchorEntity>)

    @Insert
    abstract suspend fun insertPhases(phases: List<PhaseEntity>)

    @Insert
    abstract suspend fun insertSteps(steps: List<StepEntity>)
}
