package se.imorn.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.imorn.designsystem.theme.ImornTheme

/** How big a chapter's mark is: in lists, or at the head of the chapter. */
enum class ImornMarkSize(internal val size: Dp, internal val icon: Dp) {
    Medium(size = 48.dp, icon = 24.dp),
    Large(size = 64.dp, icon = 30.dp),
}

/**
 * A chapter's mark: its template's icon (`home`, `key` …, see [ImornIcons.template]) on the indigo of the overview,
 * and, with [progress] from 0 to 1, a ring in the apricot of the path around it, filling as steps are done.
 * Decorative to TalkBack: the chapter's title and progress are said in text beside it.
 */
@Composable
fun ImornChapterMark(
    icon: String?,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    size: ImornMarkSize = ImornMarkSize.Medium,
) {
    val colors = ImornTheme.colors
    val ring = 3.dp
    Box(
        modifier = modifier
            .size(size.size)
            .drawBehind {
                if (progress == null) return@drawBehind
                val width = ring.toPx()
                val inset = width / 2
                val arc = Size(this.size.width - width, this.size.height - width)
                drawArc(colors.surfaceVariant, 0f, FULL_CIRCLE, false, Offset(inset, inset), arc, style = Stroke(width))
                drawArc(
                    color = colors.secondary,
                    startAngle = TOP,
                    sweepAngle = FULL_CIRCLE * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arc,
                    style = Stroke(width, cap = StrokeCap.Round),
                )
            }
            .padding(if (progress == null) 0.dp else ring + 3.dp)
            .clip(CircleShape)
            .background(colors.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        ImornIcon(
            icon = ImornIcons.template(icon),
            contentDescription = null,
            tint = colors.onPrimaryContainer,
            modifier = Modifier.size(size.icon),
        )
    }
}

private const val FULL_CIRCLE = 360f
private const val TOP = -90f

/** What an [ImornTag] says, in the palette's meaning. */
enum class ImornTagTone {
    /** A plain fact, such as a date. */
    Quiet,

    /** Indigo: the future, such as an anchor date ahead. */
    Ahead,

    /** Apricot: now, such as *today* or *a little late*. Never red. */
    Now,

    /** Rose: something kept, a memory. */
    Memory,
}

/** A small label in a soft pill, such as a step's day or *Only on this phone*. */
@Composable
fun ImornTag(text: String, modifier: Modifier = Modifier, tone: ImornTagTone = ImornTagTone.Quiet) {
    val colors = ImornTheme.colors
    val (background, content) = when (tone) {
        ImornTagTone.Quiet -> colors.surfaceContainer to colors.onSurfaceVariant
        ImornTagTone.Ahead -> colors.primaryContainer to colors.onPrimaryContainer
        ImornTagTone.Now -> colors.secondaryContainer to colors.onSecondaryContainer
        ImornTagTone.Memory -> colors.tertiaryContainer to colors.onTertiaryContainer
    }
    ImornText(
        text = text,
        style = ImornTheme.typography.labelMedium,
        color = content,
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .padding(horizontal = ImornTheme.spacing.sm + ImornTheme.spacing.xs, vertical = ImornTheme.spacing.xs),
    )
}
