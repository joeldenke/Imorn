package se.imorn.notificationpolicy

import se.imorn.model.Chapter
import se.imorn.model.deadlineOf
import se.imorn.model.upcoming

/**
 * The chapter's open steps as reminders, on the days the plan gives them. Steps without a day (an unknown anchor, or
 * a step made without one) are left out, and long threads wait for their own view instead of a notification.
 */
fun Chapter.reminders(): List<Reminder> = upcoming().mapNotNull { planned ->
    val day = planned.date ?: return@mapNotNull null
    Reminder(
        chapterId = id,
        chapterTitle = title,
        stepId = planned.step.id,
        stepTitle = planned.step.title,
        day = day,
        hardDeadline = deadlineOf(planned.step),
    )
}
