package se.imorn.feature.timeline

import android.content.Intent
import android.provider.CalendarContract
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import se.imorn.ui.PreviewData
import java.time.LocalDate
import java.time.ZoneOffset

@RunWith(RobolectricTestRunner::class)
class AddToCalendarTest {
    private val day = LocalDate.of(2027, 2, 15)
    private val step = PreviewData.moving.steps.first { it.id == "address-change" }
    private val state = StepUiState(
        chapterTitle = PreviewData.moving.title,
        step = step,
        date = day,
        deadline = null,
        links = emptyList(),
        today = PreviewData.TODAY,
    )

    @Test
    fun theEventIsTheStepAsAWholeDayInUtc() {
        val intent = calendarIntent("Report your move", "Moving", day)
        assertEquals(Intent.ACTION_INSERT, intent.action)
        assertEquals(CalendarContract.Events.CONTENT_URI, intent.data)
        assertEquals("Report your move", intent.getStringExtra(CalendarContract.Events.TITLE))
        assertEquals("Moving", intent.getStringExtra(CalendarContract.Events.DESCRIPTION))
        assertEquals(true, intent.getBooleanExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false))
        assertEquals(
            day.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            intent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, 0),
        )
        assertEquals(
            LocalDate.of(2027, 2, 16).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            intent.getLongExtra(CalendarContract.EXTRA_EVENT_END_TIME, 0),
        )
    }

    @Test
    fun theDescriptionIsTheChapterAndThenWhy() {
        assertEquals("Moving\n\nSo your post follows you.", calendarDescription("Moving", "So your post follows you."))
        assertEquals("Moving", calendarDescription("Moving", null))
        assertEquals("Moving", calendarDescription("Moving", " "))
    }

    @Test
    fun aStepWithADayOpensTheCalendarApp() {
        val app = RuntimeEnvironment.getApplication()
        app.addToCalendar(state)
        val started = shadowOf(app).nextStartedActivity
        assertEquals(Intent.ACTION_INSERT, started.action)
        assertEquals(step.title, started.getStringExtra(CalendarContract.Events.TITLE))
    }

    @Test
    fun aStepWithoutADayOpensNothing() {
        val app = RuntimeEnvironment.getApplication()
        app.addToCalendar(state.copy(date = null))
        assertNull(shadowOf(app).nextStartedActivity)
    }
}
