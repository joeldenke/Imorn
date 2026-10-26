package se.imorn.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/*
 * Long threads: the steps that outlive a chapter. *Declare the sale in May next year*, *the plumber's guarantee*,
 * *the child's name within three months*. They are why a finished chapter rests instead of closing, so nothing
 * important is forgotten a year later.
 *
 * The rule is deterministic and shared by the app and the templates: a step is a long thread when either
 * - the template marks it as one (the top-level `longThreads` array in the JSON, carried onto the step as
 *   Step.longThread), or
 * - its day falls LONG_THREAD_MONTHS months or more after the chapter's anchor line.
 *
 * The anchor line is the latest known anchor date: the move day, the handover, the due date. Steps without a day
 * are never derived as long threads; the template must mark those explicitly. Nothing here guesses: the same plan
 * gives the same answer every time.
 */

/** How far after the chapter's anchor line a step falls to count as a long thread on its own. */
const val LONG_THREAD_MONTHS = 1L

/** The chapter's anchor line: the latest known anchor date, or null while every anchor is unknown. */
fun Chapter.anchorLine(): LocalDate? = anchors.mapNotNull { it.date }.maxOrNull()

/** Whether [step] counts as a long thread: the template marked it, or its day is far beyond the anchor line. */
fun Chapter.isLongThread(step: Step): Boolean = step.longThread || dateOf(step).let { day ->
    val line = anchorLine()
    day != null && line != null && ChronoUnit.MONTHS.between(line, day) >= LONG_THREAD_MONTHS
}

/** The open steps that still lie far ahead: the candidate long threads kept when the chapter is complete. */
fun Chapter.farAhead(): List<PlannedStep> = steps
    .filter { it.status == StepStatus.Open && isLongThread(it) }
    .map { PlannedStep(id, title, it, dateOf(it)) }
    .sortedWith(compareBy<PlannedStep>({ it.date == null }, { it.date }))

/** The plan steps that are not long threads: what is left to do before the chapter can be complete. */
fun Chapter.remaining(): List<PlannedStep> = steps
    .filter { it.status == StepStatus.Open && !isLongThread(it) }
    .map { PlannedStep(id, title, it, dateOf(it)) }

/** Whether the chapter has nothing left but its long threads: the moment it can be completed and rest. */
fun Chapter.isComplete(): Boolean = remaining().isEmpty()

/**
 * Completes the chapter: every remaining plan step falls through the now-line, and the far-ahead steps are marked
 * as long threads so they survive. Answers the [Chapter] and the [far ahead][farAhead] steps it will keep, so the
 * screen can ask *"There are N things further ahead. Should I keep track of them?"*.
 *
 * The caller then either [rests][rest] it (keeping the threads) or [closes][close] it (letting them go).
 */
data class Completion(val chapter: Chapter, val farAhead: List<PlannedStep>) {
    val keepCount: Int get() = farAhead.size

    /** Keeps the threads: the chapter becomes [resting][ChapterState.Resting] and only they stay in view. */
    fun rest(): Chapter = chapter.withState(ChapterState.Resting)

    /**
     * Lets the threads go: the chapter becomes [closed][ChapterState.Closed] and its open long threads are marked
     * *doesn't apply*, so they never surface again.
     */
    fun close(): Chapter {
        val dropped = chapter.steps.map { step ->
            if (step.longThread && step.status == StepStatus.Open) {
                step.copy(status = StepStatus.NotApplicable)
            } else {
                step
            }
        }
        return chapter.copy(steps = dropped).withState(ChapterState.Closed)
    }
}

/** Completes the chapter on [on]: the steps that wait now are done, and the far-ahead ones become long threads. */
fun Chapter.completed(on: LocalDate): Completion {
    val farAhead = farAhead()
    val farAheadIds = farAhead.map { it.step.id }.toSet()
    val finished = steps.map { step ->
        when {
            step.status != StepStatus.Open -> step
            step.id in farAheadIds -> step.copy(longThread = true)
            else -> step.copy(status = StepStatus.Done, doneOn = on)
        }
    }
    return Completion(copy(steps = finished), farAhead)
}

/** Completes the chapter keeping its long threads: the calm answer to *"Should I keep track of them?"*. */
fun Chapter.completedAndResting(on: LocalDate): Chapter = completed(on).rest()

/** Completes the chapter and lets it go: the answer when the user says no. */
fun Chapter.completedAndClosed(on: LocalDate): Chapter = completed(on).close()
