package se.imorn.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import se.imorn.designsystem.theme.ImornTheme

/** Where the timeline's rail runs, from the start of the row, and how much room it takes. */
private val RailX = 12.dp
private val RailWidth = 32.dp

/** Where a row's dot sits, from its top: level with the first line of the card beside it. */
private val DotY = 26.dp

/** A step or a memory on the timeline's rail: what its dot looks like. */
enum class ImornRailMark {
    /** Waiting: an indigo ring. */
    Ahead,

    /** The next thing to do, right above the now-line: a filled apricot dot with a glow. */
    Next,

    /** Done: a filled rose dot. */
    Done,

    /** A photo or a note kept on its own: a rose diamond. */
    Memory,
}

/**
 * One row of a timeline: the rail with its [mark] at the start, [content] (usually a card) beside it. Rows laid one
 * after the other with no space between them draw one unbroken rail; [first] and [last] end it.
 */
@Composable
fun ImornTimelineRow(
    mark: ImornRailMark,
    modifier: Modifier = Modifier,
    first: Boolean = false,
    last: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = ImornTheme.colors
    val ahead = mark == ImornRailMark.Ahead || mark == ImornRailMark.Next
    val rail = (if (ahead) colors.primary else colors.tertiary).copy(alpha = RAIL_ALPHA)
    val dot = when (mark) {
        ImornRailMark.Ahead -> colors.primary
        ImornRailMark.Next -> colors.secondary
        ImornRailMark.Done, ImornRailMark.Memory -> colors.tertiary
    }
    val background = ImornTheme.colors.background
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val x = RailX.toPx()
                val y = DotY.toPx()
                val width = 2.dp.toPx()
                drawLine(rail, Offset(x, if (first) y else 0f), Offset(x, if (last) y else size.height), width)
                val radius = 6.dp.toPx()
                when (mark) {
                    ImornRailMark.Ahead -> {
                        drawCircle(background, radius, Offset(x, y))
                        drawCircle(dot, radius, Offset(x, y), style = Stroke(width))
                    }

                    ImornRailMark.Next -> {
                        drawCircle(dot.copy(alpha = 0.25f), radius * 2, Offset(x, y))
                        drawCircle(dot, radius, Offset(x, y))
                    }

                    ImornRailMark.Done -> drawCircle(dot, radius, Offset(x, y))

                    ImornRailMark.Memory -> rotate(DIAMOND, Offset(x, y)) {
                        drawRect(dot, Offset(x - radius * 0.8f, y - radius * 0.8f), Size(radius * 1.6f, radius * 1.6f))
                    }
                }
            }
            .padding(start = RailWidth, bottom = ImornTheme.spacing.md),
    ) {
        content()
    }
}

private const val RAIL_ALPHA = 0.35f
private const val DIAMOND = 45f

/**
 * The now-line: what waits is above it, what has happened below. Apricot, the color of the present in Gryning,
 * glowing where it crosses the timeline's rail, with its [label] ("Now") on a pill, read as a heading by TalkBack.
 */
@Composable
fun ImornNowLine(label: String, modifier: Modifier = Modifier) {
    val colors = ImornTheme.colors
    val now = colors.secondary
    val above = colors.primary.copy(alpha = RAIL_ALPHA)
    val below = colors.tertiary.copy(alpha = RAIL_ALPHA)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val x = RailX.toPx()
                val y = size.height / 2
                drawLine(above, Offset(x, 0f), Offset(x, y), 2.dp.toPx())
                drawLine(below, Offset(x, y), Offset(x, size.height), 2.dp.toPx())
                drawLine(
                    brush = Brush.horizontalGradient(listOf(now, now.copy(alpha = 0.1f))),
                    start = Offset(x, y),
                    end = Offset(size.width, y),
                    strokeWidth = 2.dp.toPx(),
                )
                drawCircle(now.copy(alpha = 0.2f), 14.dp.toPx(), Offset(x, y))
                drawCircle(now, 7.dp.toPx(), Offset(x, y))
            }
            .padding(start = RailWidth, top = ImornTheme.spacing.sm, bottom = ImornTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ImornText(
            text = label,
            style = ImornTheme.typography.labelLarge,
            color = colors.onSecondary,
            modifier = Modifier
                .semantics { heading() }
                .clip(CircleShape)
                .background(now)
                .padding(horizontal = ImornTheme.spacing.md, vertical = ImornTheme.spacing.xs),
        )
    }
}

/** A text field with a [label], in the app's colors and shape. */
@Composable
fun ImornTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        singleLine = singleLine,
        shape = ImornTheme.shapes.small,
        modifier = modifier,
    )
}

/**
 * Asks for some text, such as an own step (one line) or a note (several, with [singleLine] false). Confirming is
 * only possible with something written.
 */
@Composable
fun ImornTextInputDialog(
    title: String,
    label: String,
    confirmText: String,
    dismissText: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    singleLine: Boolean = true,
) {
    var text by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            ImornTextField(value = text, onValueChange = { text = it }, label = label, singleLine = singleLine)
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text.trim()) }, enabled = text.isNotBlank()) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText)
            }
        },
    )
}

/** Asks before something that cannot be undone, such as deleting a chapter. */
@Composable
fun ImornConfirmDialog(
    title: String,
    text: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText)
            }
        },
    )
}
