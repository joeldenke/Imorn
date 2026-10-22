package se.imorn.model

import java.time.LocalDate

/** A step with the day it falls on in its chapter, null when it has none yet. */
data class PlannedStep(val chapterId: String, val chapterTitle: String, val step: Step, val date: LocalDate?)

/** The date of anchor [id], null when it is unknown or there is no such anchor. */
fun Chapter.anchorDate(id: String): LocalDate? = anchors.firstOrNull { it.id == id }?.date

/** The day [step] falls on: the user's own date, otherwise counted from its anchor, or null. */
fun Chapter.dateOf(step: Step): LocalDate? =
    step.date ?: step.at?.let { at -> anchorDate(at.anchor)?.let(at::resolve) }

/** The step's hard deadline, if the template marks one and its anchor is known. */
fun Chapter.deadlineOf(step: Step): LocalDate? =
    step.hardDeadline?.let { deadline -> anchorDate(deadline.anchor)?.let(deadline::resolve) }

private fun Chapter.planned(step: Step) = PlannedStep(id, title, step, dateOf(step))

/**
 * The open steps in the order the list shows them: dated steps by day, then the undated ones in plan order, so a
 * chapter whose anchor is still unknown reads like the template. Long threads are left out; see [longThreads].
 */
fun Chapter.upcoming(): List<PlannedStep> = steps
    .filter { it.status == StepStatus.Open && !it.longThread }
    .map { planned(it) }
    .withIndex()
    .sortedWith(compareBy<IndexedValue<PlannedStep>>({ it.value.date == null }, { it.value.date }, { it.index }))
    .map { it.value }

/** The open long threads, soonest first; those without a day last. */
fun Chapter.longThreads(): List<PlannedStep> = steps
    .filter { it.status == StepStatus.Open && it.longThread }
    .map { planned(it) }
    .sortedWith(compareBy<PlannedStep>({ it.date == null }, { it.date }))

/** The next thing to do in this chapter. */
fun Chapter.next(): PlannedStep? = upcoming().firstOrNull()

/**
 * The chapter as a timeline around the now-line: [ahead] is what waits, soonest first (the view draws it upwards
 * from the line), [behind] what was done, latest first. Steps that don't apply are in neither.
 */
data class Timeline(val ahead: List<PlannedStep>, val behind: List<PlannedStep>)

fun Chapter.timeline(): Timeline = Timeline(
    ahead = upcoming() + longThreads(),
    behind = steps
        .withIndex()
        .filter { it.value.status == StepStatus.Done }
        .sortedWith(compareByDescending<IndexedValue<Step>> { it.value.doneOn }.thenByDescending { it.index })
        .map { planned(it.value) },
)

/** Sets anchor [id] to [date]. Every step counted from it follows, apart from those with a date of their own. */
fun Chapter.withAnchor(id: String, date: LocalDate?): Chapter =
    copy(anchors = anchors.map { if (it.id == id) it.copy(date = date) else it })

private fun Chapter.updateStep(stepId: String, change: (Step) -> Step): Chapter {
    require(steps.any { it.id == stepId }) { "No step $stepId in chapter $id" }
    return copy(steps = steps.map { if (it.id == stepId) change(it) else it })
}

/** Ticks the step off on [on]: it falls through the now-line and becomes part of the story. */
fun Chapter.complete(stepId: String, on: LocalDate): Chapter =
    updateStep(stepId) { it.copy(status = StepStatus.Done, doneOn = on) }

/** Undoes [complete] or [notApplicable]. */
fun Chapter.reopen(stepId: String): Chapter = updateStep(stepId) { it.copy(status = StepStatus.Open, doneOn = null) }

/** "Doesn't apply": the step leaves the plan without becoming a memory. */
fun Chapter.notApplicable(stepId: String): Chapter =
    updateStep(stepId) { it.copy(status = StepStatus.NotApplicable, doneOn = null) }

/** The user's own day for the step, or null to count from its anchor again. "Tomorrow" is `today + 1`. */
fun Chapter.withStepDate(stepId: String, date: LocalDate?): Chapter = updateStep(stepId) { it.copy(date = date) }

/** Adds the user's own step at the end of the plan. */
fun Chapter.withOwnStep(step: Step): Chapter {
    require(steps.none { it.id == step.id }) { "Step ${step.id} already exists in chapter $id" }
    return copy(steps = steps + step.copy(fromTemplate = false))
}

/** A group in the list view: a phase and its open steps, or steps without a known phase when [phase] is null. */
data class PhaseGroup(val phase: Phase?, val steps: List<PlannedStep>)

/**
 * The open steps as the list shows them: by phase in the plan's order, each in [upcoming]'s order, then the steps
 * without a known phase (the user's own). Empty groups are left out, and so are long threads; see [longThreads].
 */
fun Chapter.upcomingByPhase(): List<PhaseGroup> {
    val upcoming = upcoming()
    val known = phases.map { it.id }.toSet()
    val grouped = phases.map { phase -> PhaseGroup(phase, upcoming.filter { it.step.phaseId == phase.id }) }
    return (grouped + PhaseGroup(null, upcoming.filter { it.step.phaseId !in known })).filter { it.steps.isNotEmpty() }
}

/** Rests, closes or wakes the chapter. A resting chapter keeps only its long threads in view. */
fun Chapter.withState(state: ChapterState): Chapter = copy(state = state)
