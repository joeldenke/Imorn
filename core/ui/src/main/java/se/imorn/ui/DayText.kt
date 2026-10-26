package se.imorn.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit

/**
 * A day as people say it: today, tomorrow, yesterday, otherwise the date in the user's locale. [today] comes from
 * the screen's ViewModel, never from the clock here, so previews and screenshots show a fixed day.
 */
@Composable
fun dayText(date: LocalDate?, today: LocalDate): String {
    if (date == null) return stringResource(R.string.day_no_date)
    val locale = LocalConfiguration.current.locales[0]
    return when (ChronoUnit.DAYS.between(today, date)) {
        0L -> stringResource(R.string.day_today)
        1L -> stringResource(R.string.day_tomorrow)
        -1L -> stringResource(R.string.day_yesterday)
        else -> date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
    }
}

/** The day of the month alone, for a calendar's day column; "–" for no date. */
fun dayNumber(date: LocalDate?): String = date?.dayOfMonth?.toString() ?: NO_DAY

private const val NO_DAY = "–"

/** The month in short capitals under a day number, such as *FEB*. */
@Composable
fun monthShort(date: LocalDate): String {
    val locale = LocalConfiguration.current.locales[0]
    return date.month.getDisplayName(TextStyle.SHORT, locale).trimEnd('.').uppercase(locale)
}

/** A month heading on the calendar, such as *MARCH*, with the year when it isn't this year's. */
@Composable
fun monthHeading(date: LocalDate, today: LocalDate): String {
    val locale = LocalConfiguration.current.locales[0]
    val month = date.month.getDisplayName(TextStyle.FULL_STANDALONE, locale).uppercase(locale)
    return if (date.year == today.year) month else "$month ${date.year}"
}

/** The whole day as a heading says it, such as *Wednesday 10 February*. */
@Composable
fun longDayText(date: LocalDate): String {
    val locale = LocalConfiguration.current.locales[0]
    val text = date.format(DateTimeFormatter.ofPattern(LONG_DAY, locale))
    return text.replaceFirstChar { it.titlecase(locale) }
}

private const val LONG_DAY = "EEEE d MMMM"
