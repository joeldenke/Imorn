package se.imorn.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.imorn.designsystem.theme.ImornTheme

/*
 * The bookshelf: Imorn's everyday look. Warm paper, ink-dark text, big friendly type, and a few bold color blocks:
 * the apricot block of what to do now, the chapters standing as book spines on a shelf, and each chapter opening
 * under an indigo cover. Playful in shape, calm in color: the palette keeps its meaning (indigo the future and the
 * overview, apricot now, rose memories).
 */

/** The big number of a countdown, such as *19* over *days left*. */
@Composable
fun ImornCountdown(number: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.End) {
        ImornText(text = number, style = ImornTheme.typography.displayMedium, color = ImornTheme.colors.onBackground)
        ImornText(text = label, style = ImornTheme.typography.labelMedium, color = ImornTheme.colors.onSurfaceVariant)
    }
}

/**
 * The one thing to do now: a big apricot block with the chapter as a small ink label ([chapter]), [meta] beside it
 * (such as *a little late*), the step's [title], why it matters ([why]), and the two answers, *Done* and
 * *Tomorrow*. Tapping the text opens the step ([onOpen], described by [openLabel] to TalkBack).
 */
@Composable
fun ImornNowCard(
    chapter: String,
    title: String,
    doneText: String,
    tomorrowText: String,
    onDone: () -> Unit,
    onTomorrow: () -> Unit,
    onOpen: () -> Unit,
    openLabel: String,
    modifier: Modifier = Modifier,
    meta: String? = null,
    why: String? = null,
) {
    val colors = ImornTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(ImornTheme.shapes.extraLarge)
            .background(colors.secondaryContainer)
            .padding(ImornTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
    ) {
        Column(
            modifier = Modifier.clickable(onClickLabel = openLabel, onClick = onOpen),
            verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm),
            ) {
                ImornText(
                    text = chapter,
                    style = ImornTheme.typography.labelMedium,
                    color = colors.secondaryContainer,
                    maxLines = 1,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.onSecondaryContainer)
                        .padding(horizontal = ImornTheme.spacing.md, vertical = ImornTheme.spacing.xs + 2.dp),
                )
                if (meta != null) {
                    ImornText(
                        text = meta,
                        style = ImornTheme.typography.labelMedium,
                        color = colors.onSecondaryContainer,
                    )
                }
            }
            ImornText(text = title, style = ImornTheme.typography.headlineMedium, color = colors.onSecondaryContainer)
            if (why != null) {
                ImornText(text = why, style = ImornTheme.typography.bodyMedium, color = colors.onSecondaryContainer)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm)) {
            PillButton(
                text = doneText,
                onClick = onDone,
                container = colors.onSecondaryContainer,
                content = colors.secondaryContainer,
                modifier = Modifier.weight(1f),
                ring = true,
            )
            PillButton(
                text = tomorrowText,
                onClick = onTomorrow,
                container = colors.background.copy(alpha = 0.6f),
                content = colors.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun PillButton(
    text: String,
    onClick: () -> Unit,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    ring: Boolean = false,
) {
    Row(
        modifier = modifier
            .heightIn(min = 56.dp)
            .clip(CircleShape)
            .background(container)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = ImornTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (ring) {
            Box(Modifier.size(20.dp).border(2.dp, content, CircleShape))
        }
        Text(text = text, style = ImornTheme.typography.titleMedium, color = content)
    }
}

/**
 * A round box to tick a step off with one tap, 44 dp to hit. TalkBack reads [contentDescription], such as
 * "Mark Report your move as done".
 */
@Composable
fun ImornCheckCircle(contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(role = Role.Checkbox, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(28.dp).border(2.dp, ImornTheme.colors.outline, CircleShape))
    }
}

/** A book spine's color: the chapters on the shelf take turns, so each one is easy to find again. */
enum class ImornSpineTone { Indigo, Rose, Apricot, Sand }

/** One chapter on the shelf, as an [ImornShelf] lays it out. */
class ImornSpine(
    val title: String,
    val meta: String?,
    val tone: ImornSpineTone,
    /** Its height as a share of the shelf's, so the spines differ like real books. */
    val height: Float,
    val onClick: () -> Unit,
)

private val ShelfHeight = 176.dp

/**
 * The chapters as books on a shelf: tall spines with their titles written up the spine, [ImornSpine.meta] (such as
 * *2/5*) at the top, on a wooden-colored board. Scrolls sideways when there are many. Each spine is a button.
 */
@Composable
fun ImornShelf(spines: List<ImornSpine>, modifier: Modifier = Modifier) {
    val colors = ImornTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ShelfHeight)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = ImornTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm),
            verticalAlignment = Alignment.Bottom,
        ) {
            spines.forEachIndexed { index, spine -> Spine(spine, width = SpineWidths[index % SpineWidths.size]) }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.surfaceVariant),
        )
    }
}

private val SpineWidths = listOf(64.dp, 56.dp, 60.dp, 52.dp)

@Composable
private fun Spine(spine: ImornSpine, width: Dp) {
    val colors = ImornTheme.colors
    val (container, content) = when (spine.tone) {
        ImornSpineTone.Indigo -> colors.primary to colors.onPrimary
        ImornSpineTone.Rose -> colors.tertiaryContainer to colors.onTertiaryContainer
        ImornSpineTone.Apricot -> colors.secondaryContainer to colors.onSecondaryContainer
        ImornSpineTone.Sand -> colors.surfaceVariant to colors.onSurfaceVariant
    }
    val label = listOfNotNull(spine.title, spine.meta).joinToString(", ")
    Column(
        modifier = Modifier
            .width(width)
            .height(ShelfHeight * spine.height.coerceIn(MIN_SPINE, 1f))
            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
            .background(container)
            .clickable(role = Role.Button, onClickLabel = null, onClick = spine.onClick)
            .semantics(mergeDescendants = true) { contentDescription = label }
            .padding(vertical = ImornTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = spine.meta.orEmpty(), style = ImornTheme.typography.labelMedium, color = content)
        Text(
            text = spine.title,
            style = ImornTheme.typography.labelLarge,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.upright(),
        )
    }
}

private const val MIN_SPINE = 0.6f

/** Turns the text a quarter up, as on a book's spine, and lays it out in the space that leaves. */
private fun Modifier.upright(): Modifier = layout { measurable, constraints ->
    val turned = constraints.copy(
        minWidth = 0,
        maxWidth = constraints.maxHeight,
        minHeight = 0,
        maxHeight = constraints.maxWidth,
    )
    val placeable = measurable.measure(turned)
    layout(placeable.height, placeable.width) {
        placeable.placeWithLayer(
            x = (placeable.height - placeable.width) / 2,
            y = (placeable.width - placeable.height) / 2,
        ) { rotationZ = -90f }
    }
}

/**
 * A chapter's cover: an indigo block across the top of the screen, from behind the status bar, with rounded lower
 * corners. [content] sits on it in light text ([ImornCoverText], [ImornCoverChip], [ImornStat]). While it is
 * behind the status bar, the bar's icons are light.
 */
@Composable
fun ImornCover(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val topInset = WindowInsets.safeDrawing.getTop(LocalDensity.current)
    var behindStatusBar by remember { mutableStateOf(true) }
    LightSystemBars(enabled = behindStatusBar, navigationBar = false)
    val corner = ImornTheme.spacing.xl + ImornTheme.spacing.xs
    Column(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { behindStatusBar = it.boundsInWindow().bottom > topInset }
            .clip(RoundedCornerShape(bottomStart = corner, bottomEnd = corner))
            .background(ImornTheme.colors.primary)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(
                start = ImornTheme.spacing.md + ImornTheme.spacing.xs,
                end = ImornTheme.spacing.md + ImornTheme.spacing.xs,
                top = ImornTheme.spacing.sm,
                bottom = ImornTheme.spacing.lg,
            ),
        verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
        content = content,
    )
}

/** The chapter's title on its cover, read as a heading. */
@Composable
fun ImornCoverTitle(text: String, modifier: Modifier = Modifier) {
    ImornText(
        text = text,
        style = ImornTheme.typography.displaySmall,
        color = ImornTheme.colors.onPrimary,
        modifier = modifier.semantics { heading() },
    )
}

/** A round button on the cover, such as *More*. */
@Composable
fun ImornCoverButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(ImornTheme.colors.onPrimary.copy(alpha = COVER_TINT))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        ImornIcon(icon = icon, contentDescription = null, tint = ImornTheme.colors.onPrimary)
    }
}

/** A pill on the cover, such as an anchor date. [strong] fills it with apricot, for the date that counts most. */
@Composable
fun ImornCoverChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, strong: Boolean = false) {
    val colors = ImornTheme.colors
    ImornText(
        text = text,
        style = ImornTheme.typography.labelLarge,
        color = if (strong) colors.onSecondaryContainer else colors.onPrimary,
        maxLines = 1,
        modifier = modifier
            .clip(CircleShape)
            .background(if (strong) colors.secondaryContainer else colors.onPrimary.copy(alpha = COVER_TINT))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = ImornTheme.spacing.md, vertical = ImornTheme.spacing.sm),
    )
}

/** A number and what it counts, on the cover: *19 days left*, *2/5 done*. */
@Composable
fun ImornStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(ImornTheme.shapes.small)
            .background(ImornTheme.colors.onPrimary.copy(alpha = COVER_TINT))
            .padding(ImornTheme.spacing.md)
            .semantics(mergeDescendants = true) {},
    ) {
        ImornText(text = value, style = ImornTheme.typography.headlineSmall, color = ImornTheme.colors.onPrimary)
        ImornText(text = label, style = ImornTheme.typography.labelMedium, color = ImornTheme.colors.onPrimary)
    }
}

private const val COVER_TINT = 0.14f

/**
 * How far along something is, as segments, one per step: done ones in apricot. Many steps make thin segments; past
 * [MAX_SEGMENTS] it is one bar. Decorative to TalkBack: say the numbers in text next to it.
 */
@Composable
fun ImornSegments(done: Int, total: Int, modifier: Modifier = Modifier, onCover: Boolean = false) {
    val colors = ImornTheme.colors
    val filled = colors.secondaryContainer
    val empty = if (onCover) colors.onPrimary.copy(alpha = 0.22f) else colors.surfaceVariant
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(if (total <= MAX_SEGMENTS) 4.dp else 0.dp),
    ) {
        if (total in 1..MAX_SEGMENTS) {
            repeat(total) { index ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(if (index < done) filled else empty),
                )
            }
        } else {
            val share = if (total == 0) 0f else done.toFloat() / total
            Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(empty)) {
                Box(Modifier.fillMaxWidth(share).height(8.dp).clip(CircleShape).background(filled))
            }
        }
    }
}

private const val MAX_SEGMENTS = 24

/**
 * Tabs for the views of one thing, such as a chapter's *To do*, *Timeline* and *Papers*: a row of pills, the chosen
 * one in ink. Each reads as a tab to TalkBack.
 */
@Composable
fun ImornTabs(labels: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = ImornTheme.colors
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm)) {
        labels.forEachIndexed { index, label ->
            val chosen = index == selected
            Box(
                modifier = Modifier
                    .heightIn(min = 44.dp)
                    .clip(CircleShape)
                    .background(if (chosen) colors.onBackground else colors.surfaceContainer)
                    .semantics { this.selected = chosen }
                    .clickable(role = Role.Tab) { onSelect(index) }
                    .padding(horizontal = ImornTheme.spacing.md + ImornTheme.spacing.xs),
                contentAlignment = Alignment.Center,
            ) {
                ImornText(
                    text = label,
                    style = ImornTheme.typography.labelLarge,
                    color = if (chosen) colors.background else colors.onSurface,
                )
            }
        }
    }
}

/**
 * A kept photo as a print: a white frame with a caption under the picture, set a little askew ([tilt], in degrees)
 * like a photo pinned to a board.
 */
@Composable
fun ImornPolaroid(
    path: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    caption: String? = null,
    tilt: Float = 0f,
) {
    Column(
        modifier = modifier
            .width(PolaroidWidth)
            .rotate(tilt)
            .shadow(2.dp, RoundedCornerShape(6.dp))
            .background(Color.White, RoundedCornerShape(6.dp))
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm),
    ) {
        ImornImage(
            path = path,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxWidth()
                .height(PolaroidWidth * 0.75f),
        )
        if (caption != null) {
            Text(text = caption, style = ImornTheme.typography.bodySmall, color = PolaroidInk, maxLines = 3)
        }
    }
}

private val PolaroidWidth = 168.dp

/** The caption's ink: the print is always white, also in dark mode, so its text always is dark. */
private val PolaroidInk = Color(0xFF22201C)
