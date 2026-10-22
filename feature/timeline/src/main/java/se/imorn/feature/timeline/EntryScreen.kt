package se.imorn.feature.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import se.imorn.designsystem.component.FULL_IMAGE_SIDE
import se.imorn.designsystem.component.ImornBodyText
import se.imorn.designsystem.component.ImornConfirmDialog
import se.imorn.designsystem.component.ImornImage
import se.imorn.designsystem.component.ImornScaffold
import se.imorn.designsystem.component.ImornText
import se.imorn.designsystem.component.ImornTextButton
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.model.EntryKind
import se.imorn.ui.PreviewData
import se.imorn.ui.dayText

/**
 * A photo, a note or a paper, stateless: the picture as a whole (a PDF page by page), the day it was kept and the
 * step it belongs to, and deleting it. Papers say that they stay on this phone.
 */
@Composable
fun EntryScreen(
    state: EntryUiState,
    kept: KeptEntry,
    onDelete: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val entry = kept.entry
    var deleting by rememberSaveable { mutableStateOf(false) }
    val title = when (entry.kind) {
        EntryKind.Paper -> entry.text ?: stringResource(R.string.paper)
        EntryKind.Photo -> stringResource(R.string.photo)
        EntryKind.Note -> stringResource(R.string.note)
    }
    val paper = if (entry.isPdf) rememberPaper(kept.file) else null
    ImornScaffold(title = title, modifier = modifier) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            contentPadding = PaddingValues(
                start = ImornTheme.spacing.md,
                end = ImornTheme.spacing.md,
                top = ImornTheme.spacing.sm + contentPadding.calculateTopPadding(),
                bottom = ImornTheme.spacing.lg + contentPadding.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
        ) {
            item {
                ImornText(
                    text = listOfNotNull(dayText(entry.on, state.today), state.stepTitle ?: state.chapterTitle)
                        .joinToString(" · "),
                    style = ImornTheme.typography.labelLarge,
                    color = ImornTheme.colors.tertiary,
                )
            }
            if (entry.isPrivate) {
                item { PrivateLabel() }
            }
            when {
                paper is Paper.Open -> items(paper.document.pageCount) { index ->
                    PaperPage(
                        document = paper.document,
                        index = index,
                        contentDescription = stringResource(R.string.paper_page, index + 1, paper.document.pageCount),
                    )
                }
                paper == Paper.Unreadable -> item { ImornBodyText(text = stringResource(R.string.paper_unreadable)) }
                entry.kind != EntryKind.Note && !entry.isPdf -> item {
                    ImornImage(
                        path = kept.file,
                        contentDescription = entry.text ?: title,
                        crop = false,
                        maxSide = FULL_IMAGE_SIDE,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            if (entry.kind != EntryKind.Paper) {
                entry.text?.let { item { ImornBodyText(text = it) } }
            }
            item {
                ImornTextButton(text = stringResource(R.string.entry_delete), onClick = { deleting = true })
            }
        }
    }
    if (deleting) {
        ImornConfirmDialog(
            title = stringResource(R.string.entry_delete_title),
            text = stringResource(R.string.entry_delete_text),
            confirmText = stringResource(R.string.entry_delete),
            dismissText = stringResource(R.string.dialog_cancel),
            onConfirm = {
                deleting = false
                onDelete()
            },
            onDismiss = { deleting = false },
        )
    }
}

@Composable
private fun PreviewEntry(id: String) {
    val entry = PreviewData.kept.first { it.id == id }
    ImornTheme {
        EntryScreen(
            state = EntryUiState(
                chapterTitle = PreviewData.moving.title,
                kept = KeptEntry(entry),
                stepTitle = PreviewData.moving.steps.firstOrNull { it.id == entry.stepId }?.title,
                today = PreviewData.TODAY,
            ),
            kept = KeptEntry(entry),
            onDelete = {},
            contentPadding = PaddingValues(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EntryPhotoPreview() {
    PreviewEntry("preview-photo")
}

@Preview(showBackground = true)
@Composable
private fun EntryPaperPreview() {
    PreviewEntry("preview-receipt")
}
