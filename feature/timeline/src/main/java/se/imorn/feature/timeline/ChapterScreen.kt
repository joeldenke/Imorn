package se.imorn.feature.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import se.imorn.designsystem.component.ImornBodyText
import se.imorn.designsystem.component.ImornCard
import se.imorn.designsystem.component.ImornChoiceCard
import se.imorn.designsystem.component.ImornConfirmDialog
import se.imorn.designsystem.component.ImornDatePickerDialog
import se.imorn.designsystem.component.ImornFloatingButton
import se.imorn.designsystem.component.ImornIcons
import se.imorn.designsystem.component.ImornImage
import se.imorn.designsystem.component.ImornNowLine
import se.imorn.designsystem.component.ImornScaffold
import se.imorn.designsystem.component.ImornSecondaryButton
import se.imorn.designsystem.component.ImornSheet
import se.imorn.designsystem.component.ImornSheetAction
import se.imorn.designsystem.component.ImornText
import se.imorn.designsystem.component.ImornTextButton
import se.imorn.designsystem.component.ImornTextInputDialog
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.model.Chapter
import se.imorn.model.ChapterState
import se.imorn.model.Entry
import se.imorn.model.EntryKind
import se.imorn.model.PlannedStep
import se.imorn.model.StoryItem
import se.imorn.model.longThreads
import se.imorn.model.papers
import se.imorn.model.story
import se.imorn.model.timeline
import se.imorn.model.upcomingByPhase
import se.imorn.ui.Capture
import se.imorn.ui.PreviewData
import se.imorn.ui.dayText
import java.time.LocalDate

/**
 * A chapter, stateless. The list and the timeline are the same steps: the list groups what waits by phase, the
 * timeline puts it above the now-line (soonest closest to it) and the story below, latest first: what was done,
 * with the photos and notes kept along the way. Papers have a view of their own and are never part of the story.
 * The quick + catches something on the way: a photo, a step or a note.
 */
@Composable
fun ChapterScreen(
    state: ChapterUiState,
    chapter: Chapter,
    capture: Capture,
    onShow: (ChapterView) -> Unit,
    onOpenStep: (String) -> Unit,
    onOpenEntry: (String) -> Unit,
    onSetAnchor: (String, LocalDate?) -> Unit,
    onAddStep: (String) -> Unit,
    onAddNote: (String) -> Unit,
    onSetState: (ChapterState) -> Unit,
    onDelete: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    var picking by rememberSaveable { mutableStateOf<String?>(null) }
    var adding by rememberSaveable { mutableStateOf(false) }
    var writing by rememberSaveable { mutableStateOf(false) }
    var quickAdd by rememberSaveable { mutableStateOf(false) }
    var deleting by rememberSaveable { mutableStateOf(false) }
    ImornScaffold(
        title = chapter.title,
        modifier = modifier,
        floatingAction = {
            ImornFloatingButton(
                icon = ImornIcons.Add,
                contentDescription = stringResource(R.string.quick_add),
                onClick = { quickAdd = true },
                modifier = Modifier.padding(contentPadding),
            )
        },
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(scaffoldPadding)
                .padding(contentPadding)
                .padding(horizontal = ImornTheme.spacing.md, vertical = ImornTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
        ) {
            if (chapter.state != ChapterState.Active) {
                ImornBodyText(text = stringResource(stateNote(chapter.state)))
            }
            chapter.anchors.forEach { anchor ->
                ImornCard(modifier = Modifier.fillMaxWidth(), onClick = { picking = anchor.id }) {
                    ImornText(
                        text = anchor.label,
                        style = ImornTheme.typography.labelLarge,
                        color = ImornTheme.colors.onSurfaceVariant,
                    )
                    ImornText(
                        text = anchor.date?.let { dayText(it, state.today) }
                            ?: stringResource(R.string.chapter_anchor_unknown),
                        style = ImornTheme.typography.titleMedium,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm)) {
                ChapterView.entries.forEach { view ->
                    ImornChoiceCard(
                        selected = state.view == view,
                        onClick = { onShow(view) },
                        modifier = Modifier.weight(1f),
                    ) {
                        ImornText(text = stringResource(viewLabel(view)), style = ImornTheme.typography.labelLarge)
                    }
                }
            }
            when (state.view) {
                ChapterView.List -> ListView(chapter, state.today, onOpenStep)
                ChapterView.Timeline -> TimelineView(chapter, state, onOpenStep, onOpenEntry)
                ChapterView.Papers -> PapersView(chapter, state, capture.choosePaper, onOpenEntry)
            }
            if (state.view != ChapterView.Papers) {
                ImornSecondaryButton(text = stringResource(R.string.chapter_add_step), onClick = { adding = true })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm)) {
                if (chapter.state == ChapterState.Active) {
                    ImornTextButton(
                        text = stringResource(R.string.chapter_rest),
                        onClick = { onSetState(ChapterState.Resting) },
                    )
                    ImornTextButton(
                        text = stringResource(R.string.chapter_close),
                        onClick = { onSetState(ChapterState.Closed) },
                    )
                } else {
                    ImornTextButton(
                        text = stringResource(R.string.chapter_wake),
                        onClick = { onSetState(ChapterState.Active) },
                    )
                }
                ImornTextButton(text = stringResource(R.string.chapter_delete), onClick = { deleting = true })
            }
            // Room below the last button for the quick +.
            Spacer(Modifier.height(ImornTheme.spacing.xl * 2))
        }
    }
    if (quickAdd) {
        QuickAddSheet(
            capture = capture,
            onAddStep = { adding = true },
            onWriteNote = { writing = true },
            onDismiss = { quickAdd = false },
        )
    }
    if (writing) {
        ImornTextInputDialog(
            title = stringResource(R.string.quick_note),
            label = stringResource(R.string.note_label),
            confirmText = stringResource(R.string.note_save),
            dismissText = stringResource(R.string.dialog_cancel),
            onConfirm = {
                onAddNote(it)
                writing = false
            },
            onDismiss = { writing = false },
            singleLine = false,
        )
    }
    picking?.let { id ->
        ImornDatePickerDialog(
            initial = chapter.anchors.firstOrNull { it.id == id }?.date ?: state.today,
            confirmText = stringResource(R.string.dialog_ok),
            dismissText = stringResource(R.string.dialog_cancel),
            onPick = {
                onSetAnchor(id, it)
                picking = null
            },
            onDismiss = { picking = null },
        )
    }
    if (adding) {
        ImornTextInputDialog(
            title = stringResource(R.string.chapter_add_step),
            label = stringResource(R.string.chapter_add_step_label),
            confirmText = stringResource(R.string.chapter_add_step_confirm),
            dismissText = stringResource(R.string.dialog_cancel),
            onConfirm = {
                onAddStep(it)
                adding = false
            },
            onDismiss = { adding = false },
        )
    }
    if (deleting) {
        ImornConfirmDialog(
            title = stringResource(R.string.chapter_delete_title),
            text = stringResource(R.string.chapter_delete_text),
            confirmText = stringResource(R.string.chapter_delete),
            dismissText = stringResource(R.string.dialog_cancel),
            onConfirm = {
                deleting = false
                onDelete()
            },
            onDismiss = { deleting = false },
        )
    }
}

private fun stateNote(state: ChapterState) = when (state) {
    ChapterState.Active -> R.string.chapter_state_active
    ChapterState.Resting -> R.string.chapter_state_resting
    ChapterState.Closed -> R.string.chapter_state_closed
}

private fun viewLabel(view: ChapterView) = when (view) {
    ChapterView.List -> R.string.chapter_view_list
    ChapterView.Timeline -> R.string.chapter_view_timeline
    ChapterView.Papers -> R.string.chapter_view_papers
}

/** The quick +: catch something on the way. Each choice closes the sheet first. */
@Composable
private fun QuickAddSheet(capture: Capture, onAddStep: () -> Unit, onWriteNote: () -> Unit, onDismiss: () -> Unit) {
    fun choose(action: () -> Unit): () -> Unit = {
        onDismiss()
        action()
    }
    ImornSheet(onDismiss = onDismiss) {
        ImornSheetAction(ImornIcons.Camera, stringResource(R.string.quick_take_photo), choose(capture.takePhoto))
        ImornSheetAction(ImornIcons.Photo, stringResource(R.string.quick_choose_photo), choose(capture.choosePhoto))
        ImornSheetAction(ImornIcons.Step, stringResource(R.string.chapter_add_step), choose(onAddStep))
        ImornSheetAction(ImornIcons.Note, stringResource(R.string.quick_note), choose(onWriteNote))
        ImornSheetAction(ImornIcons.Paper, stringResource(R.string.paper_add), choose(capture.choosePaper))
        Spacer(Modifier.height(ImornTheme.spacing.lg))
    }
}

private const val PHOTO_ASPECT = 4f / 3f

@Composable
private fun SectionTitle(text: String) {
    ImornText(text = text, style = ImornTheme.typography.titleMedium, color = ImornTheme.colors.onBackground)
}

@Composable
private fun ListView(chapter: Chapter, today: LocalDate, onOpenStep: (String) -> Unit) {
    val groups = chapter.upcomingByPhase()
    if (groups.isEmpty()) {
        ImornBodyText(text = stringResource(R.string.chapter_nothing_left))
    }
    groups.forEach { group ->
        SectionTitle(group.phase?.title ?: stringResource(R.string.chapter_own_steps))
        group.steps.forEach { StepCard(it, today, onOpenStep) }
    }
    val later = chapter.longThreads()
    if (later.isNotEmpty()) {
        SectionTitle(stringResource(R.string.chapter_later))
        later.forEach { StepCard(it, today, onOpenStep) }
    }
}

@Composable
private fun TimelineView(
    chapter: Chapter,
    state: ChapterUiState,
    onOpenStep: (String) -> Unit,
    onOpenEntry: (String) -> Unit,
) {
    val today = state.today
    val kept = state.kept.associateBy { it.entry.id }
    // Furthest ahead at the top, so the next step sits right above the now-line.
    chapter.timeline().ahead.asReversed().forEach { StepCard(it, today, onOpenStep) }
    ImornNowLine(label = stringResource(R.string.chapter_now))
    val story = chapter.story(state.kept.map { it.entry })
    if (story.isEmpty()) {
        ImornBodyText(text = stringResource(R.string.chapter_nothing_done))
    }
    story.forEach { item ->
        when (item) {
            is StoryItem.Done -> DoneCard(item, today, kept, onOpenStep, onOpenEntry)
            is StoryItem.Kept -> KeptCard(kept[item.entry.id] ?: KeptEntry(item.entry), today, onOpenEntry)
        }
    }
}

/** A step that was done, with the photos (as a row of thumbnails) and notes kept with it. */
@Composable
private fun DoneCard(
    item: StoryItem.Done,
    today: LocalDate,
    kept: Map<String, KeptEntry>,
    onOpenStep: (String) -> Unit,
    onOpenEntry: (String) -> Unit,
) {
    val step = item.step.step
    ImornCard(modifier = Modifier.fillMaxWidth(), onClick = { onOpenStep(step.id) }) {
        ImornText(
            text = stringResource(R.string.chapter_done_on, dayText(step.doneOn, today)),
            style = ImornTheme.typography.labelLarge,
            color = ImornTheme.colors.tertiary,
        )
        ImornText(text = step.title, style = ImornTheme.typography.bodyLarge)
        val photos = item.kept.filter { it.kind == EntryKind.Photo }
        if (photos.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm)) {
                photos.forEach { photo ->
                    ImornImage(
                        path = kept[photo.id]?.thumbnail,
                        contentDescription = stringResource(R.string.photo_of, step.title),
                        modifier = Modifier
                            .size(ImornTheme.spacing.xl * 2)
                            .clickableEntry(photo.id, onOpenEntry),
                    )
                }
            }
        }
        item.kept.filter { it.kind == EntryKind.Note }.forEach { note ->
            ImornText(
                text = note.text.orEmpty(),
                style = ImornTheme.typography.bodyMedium,
                color = ImornTheme.colors.onSurfaceVariant,
                modifier = Modifier.clickableEntry(note.id, onOpenEntry),
            )
        }
    }
}

/** A photo or a note kept on its own, on the day it was kept. */
@Composable
private fun KeptCard(kept: KeptEntry, today: LocalDate, onOpenEntry: (String) -> Unit) {
    val entry = kept.entry
    ImornCard(modifier = Modifier.fillMaxWidth(), onClick = { onOpenEntry(entry.id) }) {
        ImornText(
            text = dayText(entry.on, today),
            style = ImornTheme.typography.labelLarge,
            color = ImornTheme.colors.tertiary,
        )
        if (entry.kind == EntryKind.Photo) {
            ImornImage(
                path = kept.thumbnail,
                contentDescription = entry.text ?: stringResource(R.string.photo),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(PHOTO_ASPECT),
            )
        }
        entry.text?.let { ImornText(text = it, style = ImornTheme.typography.bodyLarge) }
    }
}

/** The chapter's papers, private to this phone: letters, contracts and receipts, latest first. */
@Composable
private fun PapersView(chapter: Chapter, state: ChapterUiState, onAddPaper: () -> Unit, onOpenEntry: (String) -> Unit) {
    val kept = state.kept.associateBy { it.entry.id }
    PrivateLabel()
    ImornBodyText(text = stringResource(R.string.papers_intro))
    ImornSecondaryButton(text = stringResource(R.string.paper_add), onClick = onAddPaper)
    val papers = chapter.papers(state.kept.map { it.entry })
    if (papers.isEmpty()) {
        ImornBodyText(text = stringResource(R.string.papers_empty))
    }
    papers.forEach { paper -> PaperCard(kept[paper.id] ?: KeptEntry(paper), state.today, onOpenEntry) }
}

@Composable
private fun PaperCard(kept: KeptEntry, today: LocalDate, onOpenEntry: (String) -> Unit) {
    val paper: Entry = kept.entry
    ImornCard(modifier = Modifier.fillMaxWidth(), onClick = { onOpenEntry(paper.id) }) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
        ) {
            ImornImage(
                path = kept.thumbnail,
                contentDescription = null,
                modifier = Modifier.size(ImornTheme.spacing.xl * 2),
            )
            Column(verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.xs)) {
                ImornText(
                    text = paper.text ?: stringResource(R.string.paper),
                    style = ImornTheme.typography.bodyLarge,
                )
                ImornText(
                    text = dayText(paper.on, today),
                    style = ImornTheme.typography.labelLarge,
                    color = ImornTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StepCard(planned: PlannedStep, today: LocalDate, onOpenStep: (String) -> Unit) {
    // A local copy: PlannedStep comes from another module, so its property can't be smart cast.
    val date = planned.date
    val late = date != null && date < today
    ImornCard(modifier = Modifier.fillMaxWidth(), onClick = { onOpenStep(planned.step.id) }) {
        ImornText(
            text = if (late) {
                stringResource(R.string.chapter_late_since, dayText(date, today))
            } else {
                dayText(date, today)
            },
            style = ImornTheme.typography.labelLarge,
            color = if (late) ImornTheme.colors.secondary else ImornTheme.colors.onSurfaceVariant,
        )
        ImornText(text = planned.step.title, style = ImornTheme.typography.bodyLarge)
    }
}

@Composable
private fun PreviewScreen(view: ChapterView) {
    ImornTheme {
        ChapterScreen(
            state = ChapterUiState(PreviewData.moving, PreviewData.TODAY, view, PreviewData.kept.map(::KeptEntry)),
            chapter = PreviewData.moving,
            capture = Capture({}, {}, {}),
            onShow = {},
            onOpenStep = {},
            onOpenEntry = {},
            onSetAnchor = { _, _ -> },
            onAddStep = {},
            onAddNote = {},
            onSetState = {},
            onDelete = {},
            contentPadding = PaddingValues(),
        )
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun ChapterListPreview() {
    PreviewScreen(ChapterView.List)
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun ChapterTimelinePreview() {
    PreviewScreen(ChapterView.Timeline)
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun ChapterPapersPreview() {
    PreviewScreen(ChapterView.Papers)
}
