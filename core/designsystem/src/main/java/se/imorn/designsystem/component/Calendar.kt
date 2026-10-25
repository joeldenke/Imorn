package se.imorn.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.imorn.designsystem.theme.ImornTheme

/*
 * The calendar timeline: a chapter read like a diary. A column of big day numbers down the side, the month written
 * above each new month, the apricot band of *now* across the whole width, and what has happened below it in rose.
 */

/** What a day on the calendar is: its color says when it is, as the palette does. */
enum class ImornDayTone {
    /** Waiting: ink. */
    Ahead,

    /** An anchor, such as the moving day: indigo. */
    Anchor,

    /** Late or today: apricot. */
    Now,

    /** Done or kept: rose. */
    Memory,

    /** Far off or without a date: quiet. */
    Quiet,
}

@Composable
private fun ImornDayTone.color(): Color = when (this) {
    ImornDayTone.Ahead -> ImornTheme.colors.onBackground
    ImornDayTone.Anchor -> ImornTheme.colors.primary
    ImornDayTone.Now -> ImornTheme.colors.secondary
    ImornDayTone.Memory -> ImornTheme.colors.tertiary
    ImornDayTone.Quiet -> ImornTheme.colors.onSurfaceVariant
}

/** The width of the calendar's day column, so rows and month headings line up. */
private val DayColumn = 52.dp

/**
 * A day as a calendar shows it: the [day] of the month big, and under it, when given, the [month] in small capitals.
 * [day] is "–" for a step without a date.
 */
@Composable
fun ImornDateBadge(
    day: String,
    modifier: Modifier = Modifier,
    month: String? = null,
    tone: ImornDayTone = ImornDayTone.Ahead,
) {
    Column(modifier = modifier.width(DayColumn), horizontalAlignment = Alignment.End) {
        ImornText(text = day, style = ImornTheme.typography.headlineSmall, color = tone.color())
        if (month != null) {
            ImornText(
                text = month,
                style = ImornTheme.typography.labelSmall,
                color = ImornTheme.colors.onSurfaceVariant,
            )
        }
    }
}

/** The name of a month above its days, such as *MARCH*, read as a heading. */
@Composable
fun ImornMonthHeading(text: String, modifier: Modifier = Modifier) {
    ImornText(
        text = text,
        style = ImornTheme.typography.labelSmall,
        color = ImornTheme.colors.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = DayColumn + ImornTheme.spacing.md, top = ImornTheme.spacing.md)
            .semantics { heading() },
    )
}

/**
 * One day on the calendar: the [day] (with [tone]) in the column at the start and [content] beside it, such as a
 * step's title or a photo. With [onClick] the whole row opens what it shows.
 */
@Composable
fun ImornCalendarRow(
    day: String,
    modifier: Modifier = Modifier,
    tone: ImornDayTone = ImornDayTone.Ahead,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val clickable = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickable)
            .padding(vertical = ImornTheme.spacing.sm + ImornTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
    ) {
        Box(modifier = Modifier.width(DayColumn), contentAlignment = Alignment.TopEnd) {
            ImornText(text = day, style = ImornTheme.typography.headlineSmall, color = tone.color())
        }
        Box(modifier = Modifier.weight(1f).padding(top = ImornTheme.spacing.xs)) {
            content()
        }
    }
}

/**
 * The now-band: what waits is above it, what has happened below. Apricot, the color of the present, across the
 * whole width of the screen (out over the screen's side padding, [bleed]), with [label] ("Now") read as a heading.
 */
@Composable
fun ImornNowBand(label: String, text: String, modifier: Modifier = Modifier, bleed: Dp = ImornTheme.spacing.md) {
    Row(
        modifier = modifier
            .bleed(bleed)
            .background(ImornTheme.colors.secondaryContainer)
            .padding(horizontal = bleed, vertical = ImornTheme.spacing.sm + ImornTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ImornText(
            text = label,
            style = ImornTheme.typography.labelLarge,
            color = ImornTheme.colors.onSecondaryContainer,
            modifier = Modifier
                .width(DayColumn)
                .semantics { heading() },
        )
        ImornText(text = text, style = ImornTheme.typography.titleSmall, color = ImornTheme.colors.onSecondaryContainer)
    }
}

/** Lays the element out [amount] wider on both sides than its parent gives it, so a band reaches the screen's edges. */
internal fun Modifier.bleed(amount: Dp): Modifier = layout { measurable, constraints ->
    val extra = amount.roundToPx() * 2
    val width = constraints.maxWidth + extra
    val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
    layout(constraints.maxWidth, placeable.height) {
        placeable.place(-extra / 2, 0)
    }
}
