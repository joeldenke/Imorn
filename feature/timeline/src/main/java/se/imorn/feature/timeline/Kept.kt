package se.imorn.feature.timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import se.imorn.database.ChapterRepository
import se.imorn.designsystem.component.ImornIcon
import se.imorn.designsystem.component.ImornIcons
import se.imorn.designsystem.component.ImornText
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.model.Entry

/**
 * An entry as the screens show it, with the paths of its kept file and thumbnail. They are null for notes, in
 * previews, and when a picture could not be drawn: the screens then show a quiet tile instead.
 */
data class KeptEntry(val entry: Entry, val thumbnail: String? = null, val file: String? = null)

internal fun ChapterRepository.kept(entry: Entry) =
    KeptEntry(entry, thumbnailOf(entry)?.absolutePath, fileOf(entry)?.absolutePath)

/** "Only on this phone": papers are private, and the screens say so where they show them. */
@Composable
internal fun PrivateLabel(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.xs),
    ) {
        ImornIcon(
            icon = ImornIcons.Lock,
            contentDescription = null,
            modifier = Modifier.size(ImornTheme.spacing.md),
            tint = ImornTheme.colors.primary,
        )
        ImornText(
            text = stringResource(R.string.paper_private),
            style = ImornTheme.typography.labelMedium,
            color = ImornTheme.colors.primary,
        )
    }
}

/** Opens entry [id] when tapped, such as a thumbnail or a note inside a step's card. */
internal fun Modifier.clickableEntry(id: String, onOpen: (String) -> Unit) =
    clickable(role = Role.Button) { onOpen(id) }
