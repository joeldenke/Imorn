package se.imorn.database

import se.imorn.model.Anchor
import se.imorn.model.Chapter
import se.imorn.model.ChapterState
import se.imorn.model.Entry
import se.imorn.model.EntryKind
import se.imorn.model.LinkRef
import se.imorn.model.Phase
import se.imorn.model.RelativeTime
import se.imorn.model.Step
import se.imorn.model.StepStatus
import java.time.LocalDate

/* Between the model and the tables. Unknown names (from a newer app, say) fall back to a safe default. */

private fun LocalDate.toDay() = toEpochDay()

private fun Long.toDate(): LocalDate = LocalDate.ofEpochDay(this)

private const val DAYS = "days"
private const val MONTHS = "months"
private const val NEXT_YEAR = "nextYear"

private fun RelativeTime?.kind() = when (this) {
    is RelativeTime.Days -> DAYS
    is RelativeTime.Months -> MONTHS
    is RelativeTime.NextYear -> NEXT_YEAR
    null -> null
}

private fun RelativeTime?.value() = when (this) {
    is RelativeTime.Days -> offset
    is RelativeTime.Months -> offset
    is RelativeTime.NextYear -> month
    null -> null
}

private fun relativeTime(kind: String?, anchor: String?, value: Int?): RelativeTime? {
    if (anchor == null || value == null) return null
    return when (kind) {
        DAYS -> RelativeTime.Days(anchor, value)
        MONTHS -> RelativeTime.Months(anchor, value)
        NEXT_YEAR -> RelativeTime.NextYear(anchor, value)
        else -> null
    }
}

private inline fun <reified T : Enum<T>> enumOf(name: String, default: T): T =
    enumValues<T>().firstOrNull { it.name == name } ?: default

internal fun Chapter.toEntity() = ChapterEntity(
    id = id,
    title = title,
    templateId = templateId,
    templateVersion = templateVersion,
    state = state.name,
    createdOn = createdOn.toDay(),
)

internal fun Chapter.anchorEntities() = anchors.mapIndexed { position, anchor ->
    AnchorEntity(id, anchor.id, position, anchor.label, anchor.date?.toDay())
}

internal fun Chapter.phaseEntities() = phases.mapIndexed { position, phase ->
    PhaseEntity(id, phase.id, position, phase.title)
}

internal fun Chapter.stepEntities() = steps.mapIndexed { position, step ->
    StepEntity(
        chapterId = id,
        id = step.id,
        position = position,
        title = step.title,
        phaseId = step.phaseId,
        why = step.why,
        atKind = step.at.kind(),
        atAnchor = step.at?.anchor,
        atValue = step.at.value(),
        date = step.date?.toDay(),
        deadlineKind = step.hardDeadline.kind(),
        deadlineAnchor = step.hardDeadline?.anchor,
        deadlineValue = step.hardDeadline.value(),
        links = step.links.joinToString(" ") { it.id },
        photoPrompt = step.photoPrompt,
        longThread = step.longThread,
        note = step.note,
        status = step.status.name,
        doneOn = step.doneOn?.toDay(),
        fromTemplate = step.fromTemplate,
    )
}

internal fun ChapterWithParts.toChapter() = Chapter(
    id = chapter.id,
    title = chapter.title,
    templateId = chapter.templateId,
    templateVersion = chapter.templateVersion,
    anchors = anchors.sortedBy { it.position }.map { Anchor(it.id, it.label, it.date?.toDate()) },
    phases = phases.sortedBy { it.position }.map { Phase(it.id, it.title) },
    steps = steps.sortedBy { it.position }.map { it.toStep() },
    state = enumOf(chapter.state, ChapterState.Active),
    createdOn = chapter.createdOn.toDate(),
)

private fun StepEntity.toStep() = Step(
    id = id,
    title = title,
    phaseId = phaseId,
    why = why,
    at = relativeTime(atKind, atAnchor, atValue),
    date = date?.toDate(),
    hardDeadline = relativeTime(deadlineKind, deadlineAnchor, deadlineValue),
    links = links.split(' ').filter { it.isNotEmpty() }.map(::LinkRef),
    photoPrompt = photoPrompt,
    longThread = longThread,
    note = note,
    status = enumOf(status, StepStatus.Open),
    doneOn = doneOn?.toDate(),
    fromTemplate = fromTemplate,
)

internal fun Entry.toEntity() = EntryEntity(id, chapterId, kind.name, on.toDay(), text, stepId)

internal fun EntryEntity.toEntry() = Entry(
    id = id,
    chapterId = chapterId,
    kind = enumOf(kind, EntryKind.Note),
    on = day.toDate(),
    text = text,
    stepId = stepId,
)
