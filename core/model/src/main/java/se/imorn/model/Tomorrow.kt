package se.imorn.model

import java.time.LocalDate

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
