package se.imorn.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import se.imorn.designsystem.theme.ImornTheme

/**
 * A screen scaffold: an optional title bar and the app's background, with the standard content padding. The app is
 * edge to edge, so the title keeps clear of the status bar and display cutouts itself: Scaffold only pads the
 * content, never a custom top bar.
 */
@Composable
fun ImornScaffold(
    modifier: Modifier = Modifier,
    title: String? = null,
    // The screen's one floating button, if it has one, such as a chapter's quick +.
    floatingAction: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = floatingAction,
        containerColor = ImornTheme.colors.background,
        topBar = {
            if (title != null) {
                Text(
                    text = title,
                    style = ImornTheme.typography.titleLarge,
                    color = ImornTheme.colors.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                        )
                        .padding(
                            horizontal = ImornTheme.spacing.md,
                            vertical = ImornTheme.spacing.lg,
                        ),
                )
            }
        },
        content = content,
    )
}

/**
 * A card for every block of content: a lighter surface with a hairline outline and the app's shape. With [onClick]
 * the whole card opens what it shows, such as a step or a chapter.
 */
@Composable
fun ImornCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = ImornTheme.shapes.medium
    val colors = CardDefaults.cardColors(
        containerColor = ImornTheme.surfaces.card,
        contentColor = ImornTheme.colors.onSurface,
    )
    val border = BorderStroke(1.dp, ImornTheme.surfaces.cardOutline)
    val body: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(ImornTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm),
            content = { content() },
        )
    }
    if (onClick == null) {
        Card(modifier = modifier, shape = shape, colors = colors, border = border) { body() }
    } else {
        Card(onClick = onClick, modifier = modifier, shape = shape, colors = colors, border = border) { body() }
    }
}

/** The app's primary action: an ink pill, big enough for the thumb. */
@Composable
fun ImornButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = ButtonHeight),
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = ImornTheme.colors.onBackground,
            contentColor = ImornTheme.colors.background,
        ),
        contentPadding = PaddingValues(horizontal = ImornTheme.spacing.lg),
    ) {
        Text(text = text, style = ImornTheme.typography.titleSmall)
    }
}

/** How tall the app's buttons are: easy to hit, also with big text. */
internal val ButtonHeight = 52.dp

/** Text with a required style from [ImornTheme.typography], so text never falls back to Material's defaults. */
@Composable
fun ImornText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = ImornTheme.colors.onSurface,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(
        text = text,
        style = style,
        color = color,
        modifier = modifier,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

/** Body text that follows the app typography and color. */
@Composable
fun ImornBodyText(text: String, modifier: Modifier = Modifier) {
    ImornText(text = text, style = ImornTheme.typography.bodyLarge, modifier = modifier)
}
