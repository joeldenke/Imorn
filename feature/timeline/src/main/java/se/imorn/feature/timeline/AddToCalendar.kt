package se.imorn.feature.timeline

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * The calendar app's own *new event* screen, filled in with the step as an all-day event on its day. The user saves
 * it there, so Imorn needs no calendar permission and never reads the calendar.
 */
internal fun calendarIntent(title: String, description: String, day: LocalDate): Intent =
    Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI)
        .putExtra(CalendarContract.Events.TITLE, title)
        .putExtra(CalendarContract.Events.DESCRIPTION, description)
        .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
        // An all-day event runs from midnight to midnight in UTC, whatever the phone's own time zone is.
        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, day.utcMidnight())
        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, day.plusDays(1).utcMidnight())

/** What the event says under its title: the chapter the step belongs to, then why it is done. */
internal fun calendarDescription(chapterTitle: String, why: String?): String =
    listOfNotNull(chapterTitle, why?.takeIf { it.isNotBlank() }).joinToString("\n\n")

/** Opens the calendar app with the step filled in; says so calmly when the phone has no calendar app. */
internal fun Context.addToCalendar(state: StepUiState) {
    val day = state.date ?: return
    val intent = calendarIntent(state.step.title, calendarDescription(state.chapterTitle, state.step.why), day)
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, R.string.step_no_calendar, Toast.LENGTH_LONG).show()
    }
}

private fun LocalDate.utcMidnight(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
