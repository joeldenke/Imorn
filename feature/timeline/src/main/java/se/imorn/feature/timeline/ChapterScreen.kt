package se.imorn.feature.timeline

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import se.imorn.designsystem.component.ImornBodyText
import se.imorn.designsystem.component.ImornCard
import se.imorn.designsystem.component.ImornChapterMark
import se.imorn.designsystem.component.ImornConfirmDialog
import se.imorn.designsystem.component.ImornDatePickerDialog
import se.imorn.designsystem.component.ImornFloatingButton
import se.imorn.designsystem.component.ImornIcon
import se.imorn.designsystem.component.ImornIcons
import se.imorn.designsystem.component.ImornImage
import se.imorn.designsystem.component.ImornMarkSize
import se.imorn.designsystem.component.ImornNowLine
import se.imorn.designsystem.component.ImornProgressBar
import se.imorn.designsystem.component.ImornRailMark
import se.imorn.designsystem.component.ImornScaffold
import se.imorn.designsystem.component.ImornSecondaryButton
import se.imorn.designsystem.component.ImornSheet
import se.imorn.designsystem.component.ImornSheetAction
import se.imorn.designsystem.component.ImornTabs
import se.imorn.designsystem.component.ImornTag
import se.imorn.designsystem.component.ImornTagTone
import se.imorn.designsystem.component.ImornText
import se.imorn.designsystem.component.ImornTextInputDialog
import se.imorn.designsystem.component.ImornTimelineRow
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.model.Anchor
import se.imorn.model.Chapter
import se.imorn.model.ChapterState
import se.imorn.model.Entry
import se.imorn.model.EntryKind
import se.imorn.model.PlannedStep
import se.imorn.model.StoryItem
import se.imorn.model.Subflow
import se.imorn.model.longThreads
import se.imorn.model.next
import se.imorn.model.papers
import se.imorn.model.progress
import se.imorn.model.story
import se.imorn.model.subflows
import se.imorn.model.timeline
import se.imorn.ui.Capture
import se.imorn.ui.PreviewData
import se.imorn.ui.dayText
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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
    var more by rememberSaveable { mutableStateOf(false) }
    ImornScaffold(
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
                .padding(horizontal = ImornTheme.spacing.md, vertical = ImornTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
        ) {
            ChapterHeader(chapter, state.icon, onMore = { more = true })
            if (chapter.state != ChapterState.Active) {
                ImornTag(text = stringResource(stateNote(chapter.state)), tone = ImornTagTone.Memory)
            }
            chapter.anchors.forEach { anchor ->
                AnchorCard(anchor, state.today, onClick = { picking = anchor.id })
            }
            ImornTabs(
                labels = ChapterView.entries.map { stringResource(viewLabel(it)) },
                selected = state.view.ordinal,
                onSelect = { onShow(ChapterView.entries[it]) },
            )
            when (state.view) {
                ChapterView.List -> ListView(chapter, state.today, onOpenStep)
                ChapterView.Timeline -> TimelineView(chapter, state, onOpenStep, onOpenEntry)
                ChapterView.Papers -> PapersView(chapter, state, capture.choosePaper, onOpenEntry)
            }
            if (state.view != ChapterView.Papers) {
                ImornSecondaryButton(text = stringResource(R.string.chapter_add_step), onClick = { adding = true })
            }
            // Room below the last button for the quick +.
            Spacer(Modifier.height(ImornTheme.spacing.xl * 2))
        }
    }
    if (more) {
        MoreSheet(
            state = chapter.state,
            onSetState = onSetState,
            onDelete = { deleting = true },
            onDismiss = { more = false },
        )
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

/** The chapter's mark with its progress ring, its title, how far along it is, and the way to rest or finish it. */
@Composable
private fun ChapterHeader(chapter: Chapter, icon: String?, onMore: () -> Unit) {
    val progress = chapter.progress()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
    ) {
        ImornChapterMark(icon = icon, progress = progress.share, size = ImornMarkSize.Large)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.xs),
        ) {
            ImornText(
                text = chapter.title,
                style = ImornTheme.typography.headlineSmall,
                color = ImornTheme.colors.onBackground,
                modifier = Modifier.semantics { heading() },
            )
            if (progress.total > 0) {
                ImornText(
                    text = stringResource(R.string.chapter_progress, progress.done, progress.total),
                    style = ImornTheme.typography.labelLarge,
                    color = ImornTheme.colors.secondary,
                )
            }
        }
        ImornIcon(
            icon = ImornIcons.More,
            contentDescription = stringResource(R.string.chapter_more),
            modifier = Modifier
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onMore)
                .padding(ImornTheme.spacing.sm),
        )
    }
}

/** An anchor date, such as the moving day: its day and how far off it is. Tapping it changes the date. */
@Composable
private fun AnchorCard(anchor: Anchor, today: LocalDate, onClick: () -> Unit) {
    val date = anchor.date
    ImornCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
        ) {
            ImornIcon(icon = ImornIcons.Calendar, contentDescription = null, tint = ImornTheme.colors.primary)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.xs),
            ) {
                ImornText(
                    text = anchor.label,
                    style = ImornTheme.typography.labelLarge,
                    color = ImornTheme.colors.onSurfaceVariant,
                )
                ImornText(
                    text = date?.let { dayText(it, today) } ?: stringResource(R.string.chapter_anchor_unknown),
                    style = ImornTheme.typography.titleMedium,
                )
            }
            if (date != null) {
                val days = ChronoUnit.DAYS.between(today, date)
                if (days > 0) {
                    ImornTag(
                        text = pluralStringResource(R.plurals.chapter_anchor_in_days, days.toInt(), days.toInt()),
                        tone = ImornTagTone.Ahead,
                    )
                }
            }
        }
    }
}

/** Resting, finishing, waking and deleting the chapter: out of the way, one tap behind the header. */
@Composable
private fun MoreSheet(
    state: ChapterState,
    onSetState: (ChapterState) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    fun choose(action: () -> Unit): () -> Unit = {
        onDismiss()
        action()
    }
    ImornSheet(onDismiss = onDismiss) {
        if (state == ChapterState.Active) {
            ImornSheetAction(
                ImornIcons.Calendar,
                stringResource(R.string.chapter_rest),
                choose { onSetState(ChapterState.Resting) },
            )
            ImornSheetAction(
                ImornIcons.Check,
                stringResource(R.string.chapter_close),
                choose { onSetState(ChapterState.Closed) },
            )
        } else {
            ImornSheetAction(
                ImornIcons.Forward,
                stringResource(R.string.chapter_wake),
                choose { onSetState(ChapterState.Active) },
            )
        }
        ImornSheetAction(ImornIcons.Delete, stringResource(R.string.chapter_delete), choose(onDelete))
        Spacer(Modifier.height(ImornTheme.spacing.lg))
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
    val subflows = chapter.subflows()
    if (subflows.all { it.finished }) {
        ImornBodyText(text = stringResource(R.string.chapter_nothing_left))
    }
    // The subflow with the chapter's next step starts open; the others show their progress and what is next.
    val first = chapter.next()?.step?.phaseId?.takeIf { id -> chapter.phases.any { it.id == id } } ?: OWN_STEPS
    var expanded by rememberSaveable(chapter.id) { mutableStateOf(listOf(first)) }
    subflows.forEach { subflow ->
        val key = subflow.phase?.id ?: OWN_STEPS
        SubflowCard(
            subflow = subflow,
            expanded = key in expanded,
            today = today,
            onToggle = { expanded = if (key in expanded) expanded - key else expanded + key },
            onOpenStep = onOpenStep,
        )
    }
    val later = chapter.longThreads()
    if (later.isNotEmpty()) {
        SectionTitle(stringResource(R.string.chapter_later))
        later.forEach { StepCard(it, today, onOpenStep) }
    }
}

/** The key of the user's own steps, which have no phase, among the expanded subflows. */
private const val OWN_STEPS = ""

/**
 * A subflow, such as *Mortgage and down payment*: its progress as a bar and in words, and what is next. Tapping it
 * shows or hides its open steps. A finished one says so, quietly.
 */
@Composable
private fun SubflowCard(
    subflow: Subflow,
    expanded: Boolean,
    today: LocalDate,
    onToggle: () -> Unit,
    onOpenStep: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm)) {
        ImornCard(modifier = Modifier.fillMaxWidth(), onClick = onToggle) {
            ImornText(
                text = subflow.phase?.title ?: stringResource(R.string.chapter_own_steps),
                style = ImornTheme.typography.titleMedium,
            )
            ImornProgressBar(progress = subflow.done.toFloat() / subflow.total)
            ImornText(
                text = if (subflow.finished) {
                    stringResource(R.string.chapter_subflow_finished, subflow.total)
                } else {
                    stringResource(R.string.chapter_subflow_progress, subflow.done, subflow.total)
                },
                style = ImornTheme.typography.labelLarge,
                color = ImornTheme.colors.onSurfaceVariant,
            )
            subflow.next?.takeIf { !expanded }?.let { next ->
                ImornText(
                    text = stringResource(R.string.chapter_subflow_next, next.step.title, dayText(next.date, today)),
                    style = ImornTheme.typography.bodyMedium,
                )
            }
            if (!subflow.finished) {
                val toggle = if (expanded) R.string.chapter_subflow_hide else R.string.chapter_subflow_show
                ImornText(
                    text = stringResource(toggle),
                    style = ImornTheme.typography.labelLarge,
                    color = ImornTheme.colors.primary,
                )
            }
        }
        if (expanded) {
            subflow.open.forEach { StepCard(it, today, onOpenStep) }
        }
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
    // One column without gaps, so the rail runs unbroken from the furthest step down to the first memory.
    Column {
        // Furthest ahead at the top, so the next step sits right above the now-line.
        val ahead = chapter.timeline().ahead.asReversed()
        ahead.forEachIndexed { index, planned ->
            ImornTimelineRow(
                mark = if (index == ahead.lastIndex) ImornRailMark.Next else ImornRailMark.Ahead,
                first = index == 0,
            ) {
                StepCard(planned, today, onOpenStep)
            }
        }
        ImornNowLine(label = stringResource(R.string.chapter_now))
        val story = chapter.story(state.kept.map { it.entry })
        if (story.isEmpty()) {
            ImornTimelineRow(mark = ImornRailMark.Memory, last = true) {
                ImornBodyText(text = stringResource(R.string.chapter_nothing_done))
            }
        }
        story.forEachIndexed { index, item ->
            val last = index == story.lastIndex
            when (item) {
                is StoryItem.Done -> ImornTimelineRow(mark = ImornRailMark.Done, last = last) {
                    DoneCard(item, today, kept, onOpenStep, onOpenEntry)
                }

                is StoryItem.Kept -> ImornTimelineRow(mark = ImornRailMark.Memory, last = last) {
                    KeptCard(kept[item.entry.id] ?: KeptEntry(item.entry), today, onOpenEntry)
                }
            }
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
        ImornTag(
            text = stringResource(R.string.chapter_done_on, dayText(step.doneOn, today)),
            tone = ImornTagTone.Memory,
        )
        ImornText(text = step.title, style = ImornTheme.typography.titleMedium)
        val photos = item.kept.filter { it.kind == EntryKind.Photo }
        if (photos.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm)) {
                photos.forEach { photo ->
                    ImornImage(
                        path = kept[photo.id]?.thumbnail,
                        contentDescription = stringResource(R.string.photo_of, step.title),
                        modifier = Modifier
                            .size(ImornTheme.spacing.xl * 3)
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
        ImornTag(text = dayText(entry.on, today), tone = ImornTagTone.Memory)
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
        ImornText(text = planned.step.title, style = ImornTheme.typography.bodyLarge)
        ImornTag(
            text = if (late) {
                stringResource(R.string.chapter_late_since, dayText(date, today))
            } else {
                dayText(date, today)
            },
            tone = if (late || date == today) ImornTagTone.Now else ImornTagTone.Quiet,
        )
    }
}

@Composable
private fun PreviewScreen(view: ChapterView) {
    ImornTheme {
        ChapterScreen(
            state = ChapterUiState(
                chapter = PreviewData.moving,
                today = PreviewData.TODAY,
                view = view,
                kept = PreviewData.kept.map(::KeptEntry),
                icon = PreviewData.templateIcons["move"],
            ),
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
