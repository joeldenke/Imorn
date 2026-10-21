package se.imorn.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/*
 * The tables behind a Chapter. Dates are epoch days and enums their names, so the schema reads without type
 * converters. Everything that belongs to a chapter goes with it (ON DELETE CASCADE), and `position` keeps the plan's
 * order, which a relation alone does not. Change a table only with a new database version and a tested migration.
 */

@Entity(tableName = "chapters")
internal data class ChapterEntity(
    @PrimaryKey val id: String,
    val title: String,
    val templateId: String?,
    val templateVersion: Int?,
    val state: String,
    val createdOn: Long,
)

@Entity(
    tableName = "anchors",
    primaryKeys = ["chapterId", "id"],
    foreignKeys = [ForeignKey(ChapterEntity::class, ["id"], ["chapterId"], onDelete = ForeignKey.CASCADE)],
)
internal data class AnchorEntity(
    val chapterId: String,
    val id: String,
    val position: Int,
    val label: String,
    val date: Long?,
)

@Entity(
    tableName = "phases",
    primaryKeys = ["chapterId", "id"],
    foreignKeys = [ForeignKey(ChapterEntity::class, ["id"], ["chapterId"], onDelete = ForeignKey.CASCADE)],
)
internal data class PhaseEntity(
    val chapterId: String,
    val id: String,
    val position: Int,
    val title: String,
)

/**
 * A step. Its relative times are three columns each (kind, anchor, value: days, months or the month of next year),
 * and its links are the registry ids separated by spaces.
 */
@Entity(
    tableName = "steps",
    primaryKeys = ["chapterId", "id"],
    foreignKeys = [ForeignKey(ChapterEntity::class, ["id"], ["chapterId"], onDelete = ForeignKey.CASCADE)],
)
internal data class StepEntity(
    val chapterId: String,
    val id: String,
    val position: Int,
    val title: String,
    val phaseId: String?,
    val why: String?,
    val atKind: String?,
    val atAnchor: String?,
    val atValue: Int?,
    val date: Long?,
    val deadlineKind: String?,
    val deadlineAnchor: String?,
    val deadlineValue: Int?,
    val links: String,
    val photoPrompt: String?,
    val longThread: Boolean,
    val note: String?,
    val status: String,
    val doneOn: Long?,
    val fromTemplate: Boolean,
)

@Entity(
    tableName = "entries",
    foreignKeys = [ForeignKey(ChapterEntity::class, ["id"], ["chapterId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("chapterId")],
)
internal data class EntryEntity(
    @PrimaryKey val id: String,
    val chapterId: String,
    val kind: String,
    val day: Long,
    val text: String?,
    val stepId: String?,
)

/** A chapter with everything in it, as Room reads it in one transaction. */
internal data class ChapterWithParts(
    @Embedded val chapter: ChapterEntity,
    @Relation(parentColumn = "id", entityColumn = "chapterId") val anchors: List<AnchorEntity>,
    @Relation(parentColumn = "id", entityColumn = "chapterId") val phases: List<PhaseEntity>,
    @Relation(parentColumn = "id", entityColumn = "chapterId") val steps: List<StepEntity>,
)
