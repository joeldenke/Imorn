package se.imorn.model

import java.time.LocalDate

/** One moment below the now-line: a step that was done with what was kept with it, or something kept on its own. */
sealed interface StoryItem {
    val day: LocalDate

    /** A step that was done, with the photos and notes captured for it, oldest first. */
    data class Done(val step: PlannedStep, val kept: List<Entry>) : StoryItem {
        override val day: LocalDate get() = step.step.doneOn ?: LocalDate.MIN
    }

    /** A photo or a note that belongs to no step that was done. */
    data class Kept(val entry: Entry) : StoryItem {
        override val day: LocalDate get() = entry.on
    }
}

/**
 * The chapter's story, latest first: the steps that were done (as in [timeline]) with their photos and notes, and
 * what was kept on its own, by day. Papers are never part of the story; see [papers]. A photo or note whose step
 * is no longer done stands on its own, so nothing kept is ever hidden.
 */
fun Chapter.story(entries: List<Entry>): List<StoryItem> {
    val kept = entries.filter { it.chapterId == id && !it.isPrivate }
    val done = timeline().behind
    val doneIds = done.map { it.step.id }.toSet()
    val byStep = kept.filter { it.stepId in doneIds }.groupBy { it.stepId }
    val items = done.map { StoryItem.Done(it, byStep[it.step.id].orEmpty().sortedBy { entry -> entry.on }) } +
        kept.filter { it.stepId !in doneIds }.map { StoryItem.Kept(it) }
    // Stable: on the same day, done steps keep the timeline's order and come before what was kept on its own.
    return items.sortedByDescending { it.day }
}

/** The chapter's papers, latest first. */
fun Chapter.papers(entries: List<Entry>): List<Entry> =
    entries.filter { it.chapterId == id && it.kind == EntryKind.Paper }.sortedByDescending { it.on }
