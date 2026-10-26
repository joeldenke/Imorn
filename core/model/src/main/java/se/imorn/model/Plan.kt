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

/** The user's own name for the step. A blank name is not a name, so it is ignored. */
fun Chapter.withStepTitle(stepId: String, title: String): Chapter {
    require(title.isNotBlank()) { "A step needs a name" }
    return updateStep(stepId) { it.copy(title = title.trim()) }
}

/** The step's nearest neighbour in the same phase, or null at the edge. */
private fun Chapter.stepNeighbour(stepId: String, earlier: Boolean): Int? {
    val index = steps.indexOfFirst { it.id == stepId }
    if (index < 0) return null
    val phaseId = steps[index].phaseId
    return if (earlier) {
        (index - 1 downTo 0).firstOrNull { steps[it].phaseId == phaseId }
    } else {
        (index + 1 until steps.size).firstOrNull { steps[it].phaseId == phaseId }
    }
}

/** True when [moveStep] would change the plan: the step has a neighbour in its own phase. */
fun Chapter.canMoveStep(stepId: String, earlier: Boolean): Boolean = stepNeighbour(stepId, earlier) != null

/** Moves the step one place earlier or later among the steps in its own phase. */
fun Chapter.moveStep(stepId: String, earlier: Boolean): Chapter {
    val index = steps.indexOfFirst { it.id == stepId }
    if (index < 0) return this
    val target = stepNeighbour(stepId, earlier) ?: return this
    val reordered = steps.toMutableList()
    val step = reordered.removeAt(index)
    reordered.add(target, step)
    return copy(steps = reordered)
}

/** Removes the step from the plan. Photos and notes kept with it stay in the chapter's story. */
fun Chapter.withoutStep(stepId: String): Chapter {
    require(steps.any { it.id == stepId }) { "No step $stepId in chapter $id" }
    return copy(steps = steps.filterNot { it.id == stepId })
}

/** Adds the user's own step at the end of the plan. */
fun Chapter.withOwnStep(step: Step): Chapter {
    require(steps.none { it.id == step.id }) { "Step ${step.id} already exists in chapter $id" }
    return copy(steps = steps + step.copy(fromTemplate = false))
}

/**
 * A subflow of the chapter, such as *Mortgage and down payment* or *Subscriptions*: a phase with how far along it
 * is. [open] is what waits, in [upcoming]'s order, and [next] the first of it. Steps that don't apply count for
 * nothing; [phase] is null for the user's own steps.
 */
data class Subflow(val phase: Phase?, val done: Int, val total: Int, val open: List<PlannedStep>) {
    val next: PlannedStep? get() = open.firstOrNull()
    val finished: Boolean get() = open.isEmpty()
}

/**
 * The chapter as subflows, in the plan's order, then the user's own steps. A subflow is there when it has a step
 * that applies, also when every step is done, so the chapter shows what is behind it too. Long threads are left
 * out, as in [upcoming]; see [longThreads].
 */
fun Chapter.subflows(): List<Subflow> {
    val upcoming = upcoming()
    val known = phases.map { it.id }.toSet()
    val counted = steps.filter { it.status != StepStatus.NotApplicable && !it.longThread }
    fun subflow(phase: Phase?, belongs: (Step) -> Boolean): Subflow {
        val mine = counted.filter(belongs)
        return Subflow(
            phase = phase,
            done = mine.count { it.status == StepStatus.Done },
            total = mine.size,
            open = upcoming.filter { belongs(it.step) },
        )
    }
    val grouped = phases.map { phase -> subflow(phase) { it.phaseId == phase.id } }
    return (grouped + subflow(null) { it.phaseId !in known }).filter { it.total > 0 }
}

/** Rests, closes or wakes the chapter. A resting chapter keeps only its long threads in view. */
fun Chapter.withState(state: ChapterState): Chapter = copy(state = state)
