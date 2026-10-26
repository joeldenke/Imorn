package se.imorn.feature.timeline

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.imorn.designsystem.component.ImornBodyText
import se.imorn.designsystem.component.ImornCalendarRow
import se.imorn.designsystem.component.ImornCard
import se.imorn.designsystem.component.ImornConfirmDialog
import se.imorn.designsystem.component.ImornCover
import se.imorn.designsystem.component.ImornCoverButton
import se.imorn.designsystem.component.ImornCoverChip
import se.imorn.designsystem.component.ImornCoverTitle
import se.imorn.designsystem.component.ImornDateBadge
import se.imorn.designsystem.component.ImornDatePickerDialog
import se.imorn.designsystem.component.ImornDayTone
import se.imorn.designsystem.component.ImornFloatingButton
import se.imorn.designsystem.component.ImornIcons
import se.imorn.designsystem.component.ImornImage
import se.imorn.designsystem.component.ImornMonthHeading
import se.imorn.designsystem.component.ImornNowBand
import se.imorn.designsystem.component.ImornPolaroid
import se.imorn.designsystem.component.ImornProgressBar
import se.imorn.designsystem.component.ImornScaffold
import se.imorn.designsystem.component.ImornSecondaryButton
import se.imorn.designsystem.component.ImornSegments
import se.imorn.designsystem.component.ImornSheet
import se.imorn.designsystem.component.ImornSheetAction
import se.imorn.designsystem.component.ImornStat
import se.imorn.designsystem.component.ImornTabs
import se.imorn.designsystem.component.ImornTag
import se.imorn.designsystem.component.ImornTagTone
import se.imorn.designsystem.component.ImornText
import se.imorn.designsystem.component.ImornTextInputDialog
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.model.Anchor
import se.imorn.model.Chapter
import se.imorn.model.ChapterState
import se.imorn.model.Entry
import se.imorn.model.EntryKind
import se.imorn.model.PlannedStep
import se.imorn.model.StoryItem
import se.imorn.model.Subflow
import se.imorn.model.farAhead
import se.imorn.model.longThreads
import se.imorn.model.next
import se.imorn.model.papers
import se.imorn.model.progress
import se.imorn.model.story
import se.imorn.model.subflows
import se.imorn.model.timeline
import se.imorn.ui.Capture
import se.imorn.ui.PreviewData
import se.imorn.ui.dayNumber
import se.imorn.ui.dayText
import se.imorn.ui.longDayText
import se.imorn.ui.monthHeading
import se.imorn.ui.monthShort
import java.time.LocalDate
import java.time.YearMonth
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
    onFinishKeeping: () -> Unit,
    onFinishClosing: () -> Unit,
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
    var finishing by rememberSaveable { mutableStateOf(false) }
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
                // The cover reaches up behind the status bar and keeps its text clear of it itself.
                .padding(bottom = scaffoldPadding.calculateBottomPadding())
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.lg),
        ) {
            Cover(chapter, state, onPickAnchor = { picking = it }, onMore = { more = true })
            Column(
                modifier = Modifier.padding(horizontal = ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
            ) {
                if (chapter.state != ChapterState.Active) {
                    ImornTag(text = stringResource(stateNote(chapter.state)), tone = ImornTagTone.Memory)
                }
                ImornTabs(
                    labels = ChapterView.entries.map { stringResource(viewLabel(it)) },
                    selected = state.view.ordinal,
                    onSelect = { onShow(ChapterView.entries[it]) },
                )
                when (state.view) {
                    ChapterView.List -> ListView(chapter, state.today, onOpenStep)
                    ChapterView.Timeline -> TimelineView(chapter, state, onOpenStep, onOpenEntry) { picking = it }
                    ChapterView.Papers -> PapersView(chapter, state, capture.choosePaper, onOpenEntry)
                }
                if (state.view != ChapterView.Papers) {
                    ImornSecondaryButton(
                        text = stringResource(R.string.chapter_add_step),
                        onClick = { adding = true },
                    )
                }
                // Room below the last button for the quick +.
                Spacer(Modifier.height(ImornTheme.spacing.xl * 2))
            }
        }
    }
    if (more) {
        MoreSheet(
            state = chapter.state,
            onSetState = onSetState,
            onFinish = { finishing = true },
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
    if (finishing) {
        FinishSheet(
            farAhead = chapter.farAhead(),
            onKeep = {
                finishing = false
                onFinishKeeping()
            },
            onClose = {
                finishing = false
                onFinishClosing()
            },
            onDismiss = { finishing = false },
        )
    }
}

/**
 * Finishing a chapter. When steps lie far ahead (the tax return next spring, the guarantee) the question is
 * whether to keep an eye on them: yes rests the chapter with its threads in view, no lets it become a memory.
 * With nothing ahead the chapter simply closes.
 */
@Composable
private fun FinishSheet(
    farAhead: List<PlannedStep>,
    onKeep: () -> Unit,
    onClose: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (farAhead.isEmpty()) {
        ImornConfirmDialog(
            title = stringResource(R.string.chapter_finish_title),
            text = stringResource(R.string.chapter_finish_text),
            confirmText = stringResource(R.string.chapter_close),
            dismissText = stringResource(R.string.dialog_cancel),
            onConfirm = onClose,
            onDismiss = onDismiss,
        )
        return
    }
    ImornSheet(onDismiss = onDismiss) {
        SectionTitle(stringResource(R.string.chapter_finish_title))
        ImornBodyText(text = pluralStringResource(R.plurals.chapter_finish_keep_text, farAhead.size, farAhead.size))
        ImornSheetAction(ImornIcons.Keep, stringResource(R.string.chapter_finish_keep), onKeep)
        ImornSheetAction(ImornIcons.Close, stringResource(R.string.chapter_finish_close), onClose)
        Spacer(Modifier.height(ImornTheme.spacing.lg))
    }
}

/** The side padding of the chapter's content, wide enough for the calendar's day column to breathe. */
private val ScreenPadding: Dp
    @Composable get() = ImornTheme.spacing.md + ImornTheme.spacing.xs

/**
 * The chapter's cover: its title, the anchor dates as pills (the next one in apricot; tapping one changes it), how
 * far along it is in segments, and three numbers: days to the next anchor, steps done and memories kept.
 */
@Composable
private fun Cover(chapter: Chapter, state: ChapterUiState, onPickAnchor: (String) -> Unit, onMore: () -> Unit) {
    val today = state.today
    val progress = chapter.progress()
    val next = chapter.anchors.filter { it.date?.let { date -> date >= today } == true }.minByOrNull { it.date!! }
    ImornCover {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            ImornCoverButton(
                icon = ImornIcons.More,
                contentDescription = stringResource(R.string.chapter_more),
                onClick = onMore,
            )
        }
        ImornCoverTitle(text = chapter.title)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm),
        ) {
            chapter.anchors.forEach { anchor ->
                val date = anchor.date
                ImornCoverChip(
                    text = if (date == null) {
                        stringResource(R.string.chapter_anchor_choose, anchor.label)
                    } else {
                        stringResource(R.string.chapter_anchor_chip, anchor.label, dayText(date, today))
                    },
                    onClick = { onPickAnchor(anchor.id) },
                    strong = anchor == next,
                )
            }
        }
        if (progress.total > 0) {
            ImornSegments(done = progress.done, total = progress.total, onCover = true)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm)) {
            val days = next?.date?.let { ChronoUnit.DAYS.between(today, it).toInt() }
            ImornStat(
                value = days?.toString() ?: stringResource(R.string.chapter_stat_none),
                label = pluralStringResource(R.plurals.chapter_stat_days, days ?: 0),
                modifier = Modifier.weight(1f),
            )
            ImornStat(
                value = stringResource(R.string.chapter_stat_fraction, progress.done, progress.total),
                label = stringResource(R.string.chapter_stat_done),
                modifier = Modifier.weight(1f),
            )
            val memories = state.kept.count { it.entry.kind != EntryKind.Paper }
            ImornStat(
                value = memories.toString(),
                label = pluralStringResource(R.plurals.chapter_stat_memories, memories),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** Resting, finishing, waking and deleting the chapter: out of the way, one tap behind the header. */
@Composable
private fun MoreSheet(
    state: ChapterState,
    onSetState: (ChapterState) -> Unit,
    onFinish: () -> Unit,
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
                choose(onFinish),
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

@Composable
private fun SectionTitle(text: String) {
    ImornText(text = text, style = ImornTheme.typography.titleLarge, color = ImornTheme.colors.onBackground)
}

@Composable
private fun ListView(chapter: Chapter, today: LocalDate, onOpenStep: (String) -> Unit) {
    if (chapter.steps.isEmpty()) {
        ImornBodyText(text = stringResource(R.string.chapter_no_steps))
        return
    }
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

/** Something on the calendar ahead of the now-band: a step, or an anchor such as the moving day. */
private class Ahead(val date: LocalDate?, val step: PlannedStep? = null, val anchor: Anchor? = null)

/**
 * The chapter as a calendar: what waits furthest ahead at the top (steps without a date above it all), the anchors
 * among the steps, the apricot band of now, and below it what was done and kept, latest first, in rose. A month's
 * name heads its days.
 */
@Composable
private fun TimelineView(
    chapter: Chapter,
    state: ChapterUiState,
    onOpenStep: (String) -> Unit,
    onOpenEntry: (String) -> Unit,
    onPickAnchor: (String) -> Unit,
) {
    val today = state.today
    val kept = state.kept.associateBy { it.entry.id }
    val steps = chapter.timeline().ahead.map { Ahead(it.date, step = it) }
    val anchors = chapter.anchors
        .filter { it.date?.let { date -> date >= today } == true }
        .map { Ahead(it.date, anchor = it) }
    val undated = steps.filter { it.date == null }
    val dated = (steps + anchors).filter { it.date != null }.sortedByDescending { it.date }
    Column {
        if (undated.isNotEmpty()) {
            ImornMonthHeading(text = stringResource(R.string.chapter_undated))
            undated.forEach { AheadRow(it, today, onOpenStep, onPickAnchor) }
        }
        var month: YearMonth? = null
        dated.forEach { item ->
            val date = item.date!!
            if (YearMonth.from(date) != month) {
                month = YearMonth.from(date)
                ImornMonthHeading(text = monthHeading(date, today))
            }
            AheadRow(item, today, onOpenStep, onPickAnchor)
        }
        ImornNowBand(
            label = stringResource(R.string.chapter_now),
            text = longDayText(today),
            bleed = ScreenPadding,
            modifier = Modifier.padding(vertical = ImornTheme.spacing.sm),
        )
        val story = chapter.story(state.kept.map { it.entry })
        if (story.isEmpty()) {
            ImornCalendarRow(day = dayNumber(today), tone = ImornDayTone.Quiet) {
                ImornBodyText(text = stringResource(R.string.chapter_nothing_done))
            }
        }
        var storyMonth: YearMonth? = YearMonth.from(today)
        story.forEachIndexed { index, item ->
            val date = when (item) {
                is StoryItem.Done -> item.step.step.doneOn
                is StoryItem.Kept -> item.entry.on
            }
            if (date != null && YearMonth.from(date) != storyMonth) {
                storyMonth = YearMonth.from(date)
                ImornMonthHeading(text = monthHeading(date, today))
            }
            when (item) {
                is StoryItem.Done -> DoneRow(item, kept, index, onOpenStep, onOpenEntry)
                is StoryItem.Kept -> KeptRow(kept[item.entry.id] ?: KeptEntry(item.entry), index, onOpenEntry)
            }
        }
    }
}

@Composable
private fun AheadRow(item: Ahead, today: LocalDate, onOpenStep: (String) -> Unit, onPickAnchor: (String) -> Unit) {
    val date = item.date
    val anchor = item.anchor
    if (anchor != null) {
        ImornCalendarRow(day = dayNumber(date), tone = ImornDayTone.Anchor, onClick = { onPickAnchor(anchor.id) }) {
            ImornText(text = anchor.label, style = ImornTheme.typography.titleMedium, color = ImornTheme.colors.primary)
        }
        return
    }
    val planned = item.step ?: return
    val late = date != null && date < today
    ImornCalendarRow(
        day = dayNumber(date),
        tone = when {
            date == null -> ImornDayTone.Quiet
            late || date == today -> ImornDayTone.Now
            planned.step.longThread -> ImornDayTone.Quiet
            else -> ImornDayTone.Ahead
        },
        onClick = { onOpenStep(planned.step.id) },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            ImornText(text = planned.step.title, style = ImornTheme.typography.bodyLarge)
            if (late) {
                ImornText(
                    text = stringResource(R.string.chapter_late),
                    style = ImornTheme.typography.labelMedium,
                    color = ImornTheme.colors.secondary,
                )
            }
        }
    }
}

/** Every other print leans the other way, like photos pinned to a board. */
private fun tilt(index: Int) = if (index % 2 == 0) -TILT else TILT

private const val TILT = 2.5f

/** A step that was done, on its day, with the photos (as prints) and notes kept with it. */
@Composable
private fun DoneRow(
    item: StoryItem.Done,
    kept: Map<String, KeptEntry>,
    index: Int,
    onOpenStep: (String) -> Unit,
    onOpenEntry: (String) -> Unit,
) {
    val step = item.step.step
    ImornCalendarRow(day = dayNumber(step.doneOn), tone = ImornDayTone.Memory, onClick = { onOpenStep(step.id) }) {
        Column(verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm)) {
            ImornText(text = step.title, style = ImornTheme.typography.titleMedium)
            val photos = item.kept.filter { it.kind == EntryKind.Photo }
            if (photos.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = ImornTheme.spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
                ) {
                    photos.forEachIndexed { photoIndex, photo ->
                        ImornPolaroid(
                            path = kept[photo.id]?.thumbnail,
                            contentDescription = stringResource(R.string.photo_of, step.title),
                            tilt = tilt(index + photoIndex),
                            modifier = Modifier.clickableEntry(photo.id, onOpenEntry),
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
}

/** A photo (as a print) or a note kept on its own, on the day it was kept. */
@Composable
private fun KeptRow(kept: KeptEntry, index: Int, onOpenEntry: (String) -> Unit) {
    val entry = kept.entry
    ImornCalendarRow(day = dayNumber(entry.on), tone = ImornDayTone.Memory, onClick = { onOpenEntry(entry.id) }) {
        if (entry.kind == EntryKind.Photo) {
            ImornPolaroid(
                path = kept.thumbnail,
                contentDescription = entry.text ?: stringResource(R.string.photo),
                caption = entry.text,
                tilt = tilt(index),
                modifier = Modifier.padding(vertical = ImornTheme.spacing.sm),
            )
        } else {
            ImornText(text = entry.text.orEmpty(), style = ImornTheme.typography.bodyLarge)
        }
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
        ) {
            ImornDateBadge(
                day = dayNumber(date),
                month = date?.let { monthShort(it) },
                tone = if (late || date == today) ImornDayTone.Now else ImornDayTone.Ahead,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                ImornText(text = planned.step.title, style = ImornTheme.typography.bodyLarge)
                if (late) {
                    ImornText(
                        text = stringResource(R.string.chapter_late),
                        style = ImornTheme.typography.labelMedium,
                        color = ImornTheme.colors.secondary,
                    )
                }
            }
        }
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
            onFinishKeeping = {},
            onFinishClosing = {},
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

@Preview(showBackground = true, heightDp = 1600)
@Composable
private fun ChapterTimelinePreview() {
    PreviewScreen(ChapterView.Timeline)
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun ChapterPapersPreview() {
    PreviewScreen(ChapterView.Papers)
}
