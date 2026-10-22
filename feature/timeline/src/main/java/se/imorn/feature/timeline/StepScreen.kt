package se.imorn.feature.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import se.imorn.content.ENGLISH
import se.imorn.content.Link
import se.imorn.content.LinkKind
import se.imorn.content.SWEDISH
import se.imorn.content.text
import se.imorn.designsystem.component.ImornBodyText
import se.imorn.designsystem.component.ImornButton
import se.imorn.designsystem.component.ImornCard
import se.imorn.designsystem.component.ImornDatePickerDialog
import se.imorn.designsystem.component.ImornImage
import se.imorn.designsystem.component.ImornScaffold
import se.imorn.designsystem.component.ImornSecondaryButton
import se.imorn.designsystem.component.ImornText
import se.imorn.designsystem.component.ImornTextButton
import se.imorn.designsystem.component.ImornTextField
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.model.EntryKind
import se.imorn.model.StepStatus
import se.imorn.ui.PhotoPromptDialog
import se.imorn.ui.PreviewData
import se.imorn.ui.dayText
import java.net.URI
import java.time.LocalDate

/**
 * A step, stateless: when, why, where to do it (link cards that always show the organisation and its domain, so the
 * user sees where they go), a note, and the answers. Links open in the browser; the app itself needs no network.
 */
@Composable
fun StepScreen(
    state: StepUiState,
    language: String,
    onDone: () -> Unit,
    onReopen: () -> Unit,
    onTomorrow: () -> Unit,
    onSetDate: (LocalDate?) -> Unit,
    onNotApplicable: () -> Unit,
    onSaveNote: (String) -> Unit,
    onTakePhoto: () -> Unit,
    onOpenEntry: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val step = state.step
    var picking by rememberSaveable { mutableStateOf(false) }
    // The photo prompt, asked once right after *Done*.
    var prompting by rememberSaveable { mutableStateOf(false) }
    var note by rememberSaveable(step.note) { mutableStateOf(step.note.orEmpty()) }
    ImornScaffold(title = state.chapterTitle, modifier = modifier) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(scaffoldPadding)
                .padding(contentPadding)
                .padding(horizontal = ImornTheme.spacing.md, vertical = ImornTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
        ) {
            ImornText(text = step.title, style = ImornTheme.typography.headlineSmall)
            ImornText(
                text = when (step.status) {
                    StepStatus.Done -> stringResource(R.string.chapter_done_on, dayText(step.doneOn, state.today))
                    StepStatus.NotApplicable -> stringResource(R.string.step_not_applicable_note)
                    StepStatus.Open -> dayText(state.date, state.today)
                },
                style = ImornTheme.typography.titleMedium,
                color = ImornTheme.colors.onSurfaceVariant,
            )
            state.deadline?.let {
                ImornBodyText(text = stringResource(R.string.step_deadline, dayText(it, state.today)))
            }
            step.why?.let { ImornBodyText(text = it) }
            step.photoPrompt?.let {
                ImornText(
                    text = it,
                    style = ImornTheme.typography.bodyMedium,
                    color = ImornTheme.colors.tertiary,
                )
            }
            if (step.status == StepStatus.Open) {
                Row(horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm)) {
                    ImornButton(
                        text = stringResource(R.string.step_done),
                        onClick = {
                            onDone()
                            prompting = step.photoPrompt != null
                        },
                    )
                    ImornSecondaryButton(text = stringResource(R.string.step_tomorrow), onClick = onTomorrow)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm)) {
                    ImornTextButton(text = stringResource(R.string.step_choose_date), onClick = { picking = true })
                    if (step.date != null) {
                        ImornTextButton(
                            text = stringResource(R.string.step_follow_plan),
                            onClick = { onSetDate(null) },
                        )
                    }
                }
                ImornTextButton(text = stringResource(R.string.step_not_applicable), onClick = onNotApplicable)
            } else {
                ImornSecondaryButton(text = stringResource(R.string.step_reopen), onClick = onReopen)
            }
            KeptWithStep(state.kept, step.title, onTakePhoto, onOpenEntry)
            state.links.forEach { LinkCard(it, language) }
            ImornTextField(
                value = note,
                onValueChange = { note = it },
                label = stringResource(R.string.step_note),
                modifier = Modifier.fillMaxWidth(),
            )
            if (note.trim() != step.note.orEmpty()) {
                ImornSecondaryButton(text = stringResource(R.string.step_save_note), onClick = { onSaveNote(note) })
            }
        }
    }
    val prompt = step.photoPrompt
    if (prompting && prompt != null) {
        PhotoPromptDialog(
            prompt = prompt,
            onTakePhoto = {
                prompting = false
                onTakePhoto()
            },
            onDismiss = { prompting = false },
        )
    }
    if (picking) {
        ImornDatePickerDialog(
            initial = state.date ?: state.today,
            confirmText = stringResource(R.string.dialog_ok),
            dismissText = stringResource(R.string.dialog_cancel),
            onPick = {
                onSetDate(it)
                picking = false
            },
            onDismiss = { picking = false },
        )
    }
}

/** The photos kept with the step, as thumbnails that open them, then its notes, and a way to add a photo. */
@Composable
private fun KeptWithStep(
    kept: List<KeptEntry>,
    title: String,
    onTakePhoto: () -> Unit,
    onOpenEntry: (String) -> Unit,
) {
    val photos = kept.filter { it.entry.kind == EntryKind.Photo }
    if (photos.isNotEmpty()) {
        Row(horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm)) {
            photos.forEach { photo ->
                ImornImage(
                    path = photo.thumbnail,
                    contentDescription = stringResource(R.string.photo_of, title),
                    modifier = Modifier
                        .size(ImornTheme.spacing.xl * 2)
                        .clickableEntry(photo.entry.id, onOpenEntry),
                )
            }
        }
    }
    kept.filter { it.entry.kind == EntryKind.Note }.forEach { note ->
        ImornCard(modifier = Modifier.fillMaxWidth(), onClick = { onOpenEntry(note.entry.id) }) {
            ImornBodyText(text = note.entry.text.orEmpty())
        }
    }
    ImornTextButton(text = stringResource(R.string.step_add_photo), onClick = onTakePhoto)
}

/** A link: the organisation and its domain first, so it is clear where it goes, then what to do there. */
@Composable
private fun LinkCard(link: Link, language: String) {
    val uriHandler = LocalUriHandler.current
    val url = link.url
    ImornCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = url?.let { { uriHandler.openUri(it) } },
    ) {
        val domain = url?.let { URI(it).host?.removePrefix("www.") }
        ImornText(
            text = listOfNotNull(link.org, domain).joinToString(" · "),
            style = ImornTheme.typography.labelLarge,
            color = ImornTheme.colors.primary,
        )
        ImornText(text = link.title.text(language), style = ImornTheme.typography.bodyLarge)
        link.searchHint.text(language).takeIf { it.isNotEmpty() }?.let {
            ImornText(
                text = stringResource(R.string.step_search_for, it),
                style = ImornTheme.typography.bodyMedium,
                color = ImornTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun StepPreview() {
    val chapter = PreviewData.moving
    val step = chapter.steps.first { it.id == "address-change" }
    ImornTheme {
        StepScreen(
            state = StepUiState(
                chapterTitle = chapter.title,
                step = step,
                date = LocalDate.of(2027, 2, 15),
                deadline = LocalDate.of(2027, 3, 8),
                links = listOf(
                    Link(
                        id = "skatteverket.flytt",
                        org = "Skatteverket",
                        kind = LinkKind.Authority,
                        url = "https://www.skatteverket.se/",
                        title = mapOf(SWEDISH to "Gör flyttanmälan", ENGLISH to "Report your move"),
                        searchHint = mapOf(SWEDISH to "flyttanmälan", ENGLISH to "change of address"),
                    ),
                ),
                today = PreviewData.TODAY,
            ),
            language = ENGLISH,
            onDone = {},
            onReopen = {},
            onTomorrow = {},
            onSetDate = {},
            onNotApplicable = {},
            onSaveNote = {},
            onTakePhoto = {},
            onOpenEntry = {},
            contentPadding = PaddingValues(),
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun StepDonePreview() {
    val chapter = PreviewData.moving
    val step = chapter.steps.first { it.id == "book-movers" }
    ImornTheme {
        StepScreen(
            state = StepUiState(
                chapterTitle = chapter.title,
                step = step,
                date = LocalDate.of(2027, 1, 4),
                deadline = null,
                links = emptyList(),
                today = PreviewData.TODAY,
                kept = PreviewData.kept.filter { it.stepId == step.id }.map(::KeptEntry),
            ),
            language = ENGLISH,
            onDone = {},
            onReopen = {},
            onTomorrow = {},
            onSetDate = {},
            onNotApplicable = {},
            onSaveNote = {},
            onTakePhoto = {},
            onOpenEntry = {},
            contentPadding = PaddingValues(),
        )
    }
}
