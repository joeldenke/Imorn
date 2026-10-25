package se.imorn.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * The start screen, *Imorgon*: never more than [MAX_ITEMS] things, so it never shouts. What is late comes first,
 * oldest first, then the next step in each active chapter, soonest first. [later] holds the long threads that are
 * getting close, from active and resting chapters alike, so a resting chapter wakes when it is time.
 */
data class TomorrowPlan(val items: List<TomorrowItem>, val later: List<PlannedStep>) {
    companion object {
        const val MAX_ITEMS = 3

        /** How far ahead a long thread shows under *Later*. */
        const val LATER_DAYS = 60L
    }
}

data class TomorrowItem(val planned: PlannedStep, val late: Boolean)

fun tomorrowPlan(chapters: List<Chapter>, today: LocalDate): TomorrowPlan {
    val active = chapters.filter { it.state == ChapterState.Active }
    val late = active
        .flatMap { it.upcoming() }
        .filter { it.date != null && it.date < today }
        .sortedBy { it.date }
        .map { TomorrowItem(it, late = true) }
    val next = active
        .mapNotNull { chapter -> chapter.upcoming().firstOrNull { it.date == null || it.date >= today } }
        .sortedWith(compareBy<PlannedStep>({ it.date == null }, { it.date }))
        .map { TomorrowItem(it, late = false) }
    val horizon = today.plusDays(TomorrowPlan.LATER_DAYS)
    val later = chapters
        .filter { it.state != ChapterState.Closed }
        .flatMap { it.longThreads() }
        .filter { it.date != null && it.date <= horizon }
        .sortedBy { it.date }
    return TomorrowPlan(items = (late + next).take(TomorrowPlan.MAX_ITEMS), later = later)
}

/** The anchor coming up in a chapter, such as the moving day in 19 days: the start screen counts down to it. */
data class Milestone(val chapterId: String, val chapterTitle: String, val anchor: Anchor, val date: LocalDate) {
    /** How many days until it, 0 on the day. */
    fun daysFrom(today: LocalDate): Long = ChronoUnit.DAYS.between(today, date)
}

/** The nearest known anchor, today or later, in an active chapter; null when there is none. */
fun nextMilestone(chapters: List<Chapter>, today: LocalDate): Milestone? = chapters
    .filter { it.state == ChapterState.Active }
    .flatMap { chapter ->
        chapter.anchors.mapNotNull { anchor ->
            anchor.date?.takeIf { it >= today }?.let { Milestone(chapter.id, chapter.title, anchor, it) }
        }
    }
    .minByOrNull { it.date }
