package se.imorn.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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

/** A screen scaffold: an optional title bar and the app's background, with the standard content padding. */
@Composable
fun ImornScaffold(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
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

/** A card for every block of content: a lighter surface with a hairline outline and the app's shape. */
@Composable
fun ImornCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = ImornTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = ImornTheme.surfaces.card,
            contentColor = ImornTheme.colors.onSurface,
        ),
        border = BorderStroke(1.dp, ImornTheme.surfaces.cardOutline),
    ) {
        Column(
            modifier = Modifier.padding(ImornTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm),
            content = { content() },
        )
    }
}

/** The app's primary action button. */
@Composable
fun ImornButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(onClick = onClick, modifier = modifier, enabled = enabled) {
        Text(text = text)
    }
}

/** Text with a required style from [ImornTheme.typography], so text never falls back to Material's defaults. */
@Composable
fun ImornText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = ImornTheme.colors.onSurface,
) {
    Text(text = text, style = style, color = color, modifier = modifier)
}

/** Body text that follows the app typography and color. */
@Composable
fun ImornBodyText(text: String, modifier: Modifier = Modifier) {
    ImornText(text = text, style = ImornTheme.typography.bodyLarge, modifier = modifier)
}
