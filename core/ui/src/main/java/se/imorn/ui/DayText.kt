package se.imorn.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
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
