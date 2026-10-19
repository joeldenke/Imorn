package se.imorn.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
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

/** A surface card with the app's shape and outline, used for every block of content. */
@Composable
fun ImornCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = ImornTheme.colors.surface,
            contentColor = ImornTheme.colors.onSurface,
        ),
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

/** Body text that follows the app typography and color. */
@Composable
fun ImornBodyText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = ImornTheme.colors.onSurface,
        modifier = modifier,
    )
}
