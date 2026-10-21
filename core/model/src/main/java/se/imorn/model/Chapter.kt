package se.imorn.model

import java.time.LocalDate

/**
 * A chapter of life: one project such as *Moving to Gävle* or *The bathroom*, made from a template or from scratch.
 * Its steps are both the checklist (above the now-line) and, once done, the story (below it): one list, two views.
 *
 * Texts are in the language the chapter was made in. Dates are local dates; Imorn plans days, not times.
 */
data class Chapter(
    val id: String,
    val title: String,
    /** The template it was made from and its version, so template updates can be matched to it; null if own. */
    val templateId: String? = null,
    val templateVersion: Int? = null,
    val anchors: List<Anchor> = emptyList(),
    val phases: List<Phase> = emptyList(),
    /** In plan order: the template's order, then the user's own steps as they were added. */
    val steps: List<Step> = emptyList(),
    val state: ChapterState = ChapterState.Active,
    val createdOn: LocalDate,
)

/** Active chapters plan the days. A resting one only keeps its long threads; a closed one is a memory. */
enum class ChapterState { Active, Resting, Closed }

/** A date everything else is counted from: the moving day, the handover, the due date. Null until it is known. */
data class Anchor(val id: String, val label: String, val date: LocalDate? = null)

/** A group of steps, such as *Paperwork* or *Packing*. */
data class Phase(val id: String, val title: String)

/**
 * Something to do. Its day is the user's own [date] when set, otherwise [at] counted from an anchor; with neither,
 * or with the anchor still unknown, it has no date and keeps its place in the plan.
 */
data class Step(
    /** Unique in the chapter: the template's step id, which never changes, or a generated one for own steps. */
    val id: String,
    val title: String,
    val phaseId: String? = null,
    /** Why it matters, from the template. */
    val why: String? = null,
    val at: RelativeTime? = null,
    /** A date the user chose. It wins over [at] and stays when an anchor moves. */
    val date: LocalDate? = null,
    /** The last day that really counts (marked in the template), if there is one. */
    val hardDeadline: RelativeTime? = null,
    val links: List<LinkRef> = emptyList(),
    /** Asked once when the step is done, such as "Photograph the meter readings". */
    val photoPrompt: String? = null,
    /** Far ahead (a tax return next spring) and kept when the chapter rests. */
    val longThread: Boolean = false,
    val note: String? = null,
    val status: StepStatus = StepStatus.Open,
    /** The day it was done, for the timeline below the now-line. */
    val doneOn: LocalDate? = null,
    /** False for steps the user added. */
    val fromTemplate: Boolean = true,
)

enum class StepStatus {
    Open,
    Done,

    /** "Doesn't apply": hidden from the plan and the story, and never asked about again. */
    NotApplicable,
}

/** A link by its id in the link registry (`content/links.json`), so a moved page is fixed in one place. */
@JvmInline
value class LinkRef(val id: String)

/** Something kept along the way: a note, a photo, a paper. Captured in phase 2; the model is here already. */
data class Entry(
    val id: String,
    val chapterId: String,
    val kind: EntryKind,
    val on: LocalDate,
    val text: String? = null,
    /** The step it belongs to, when it was captured as one was done. */
    val stepId: String? = null,
)

enum class EntryKind { Note, Photo, Paper }
