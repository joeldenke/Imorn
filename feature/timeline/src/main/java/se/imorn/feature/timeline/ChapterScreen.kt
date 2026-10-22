package se.imorn.feature.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import se.imorn.designsystem.component.ImornBodyText
import se.imorn.designsystem.component.ImornCard
import se.imorn.designsystem.component.ImornChoiceCard
import se.imorn.designsystem.component.ImornConfirmDialog
import se.imorn.designsystem.component.ImornDatePickerDialog
import se.imorn.designsystem.component.ImornNowLine
import se.imorn.designsystem.component.ImornScaffold
import se.imorn.designsystem.component.ImornSecondaryButton
import se.imorn.designsystem.component.ImornText
import se.imorn.designsystem.component.ImornTextButton
import se.imorn.designsystem.component.ImornTextInputDialog
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.model.Chapter
import se.imorn.model.ChapterState
import se.imorn.model.PlannedStep
import se.imorn.model.longThreads
import se.imorn.model.timeline
import se.imorn.model.upcomingByPhase
import se.imorn.ui.PreviewData
import se.imorn.ui.dayText
import java.time.LocalDate

/**
 * A chapter, stateless. The list and the timeline are the same steps: the list groups what waits by phase, the
 * timeline puts it above the now-line (soonest closest to it) and what was done below, latest first.
 */
@Composable
fun ChapterScreen(
    state: ChapterUiState,
    chapter: Chapter,
    onShow: (ChapterView) -> Unit,
    onOpenStep: (String) -> Unit,
    onSetAnchor: (String, LocalDate?) -> Unit,
    onAddStep: (String) -> Unit,
    onSetState: (ChapterState) -> Unit,
    onDelete: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    var picking by rememberSaveable { mutableStateOf<String?>(null) }
    var adding by rememberSaveable { mutableStateOf(false) }
    var deleting by rememberSaveable { mutableStateOf(false) }
    ImornScaffold(title = chapter.title, modifier = modifier) { scaffoldPadding ->
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
                ChapterView.Timeline -> TimelineView(chapter, state.today, onOpenStep)
            }
            ImornSecondaryButton(text = stringResource(R.string.chapter_add_step), onClick = { adding = true })
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
        }
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
}

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
private fun TimelineView(chapter: Chapter, today: LocalDate, onOpenStep: (String) -> Unit) {
    val timeline = chapter.timeline()
    // Furthest ahead at the top, so the next step sits right above the now-line.
    timeline.ahead.asReversed().forEach { StepCard(it, today, onOpenStep) }
    ImornNowLine(label = stringResource(R.string.chapter_now))
    if (timeline.behind.isEmpty()) {
        ImornBodyText(text = stringResource(R.string.chapter_nothing_done))
    }
    timeline.behind.forEach { planned ->
        ImornCard(modifier = Modifier.fillMaxWidth(), onClick = { onOpenStep(planned.step.id) }) {
            ImornText(
                text = stringResource(R.string.chapter_done_on, dayText(planned.step.doneOn, today)),
                style = ImornTheme.typography.labelLarge,
                color = ImornTheme.colors.tertiary,
            )
            ImornText(text = planned.step.title, style = ImornTheme.typography.bodyLarge)
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
            state = ChapterUiState(PreviewData.moving, PreviewData.TODAY, view),
            chapter = PreviewData.moving,
            onShow = {},
            onOpenStep = {},
            onSetAnchor = { _, _ -> },
            onAddStep = {},
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
