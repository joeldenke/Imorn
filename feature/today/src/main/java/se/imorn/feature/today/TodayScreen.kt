package se.imorn.feature.today

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
import se.imorn.coach.AssistantAvailability
import se.imorn.designsystem.component.ImornBodyText
import se.imorn.designsystem.component.ImornButton
import se.imorn.designsystem.component.ImornCard
import se.imorn.designsystem.component.ImornScaffold
import se.imorn.designsystem.component.ImornSecondaryButton
import se.imorn.designsystem.component.ImornText
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.model.Chapter
import se.imorn.model.ChapterState
import se.imorn.model.PlannedStep
import se.imorn.model.TomorrowItem
import se.imorn.model.next
import se.imorn.model.tomorrowPlan
import se.imorn.ui.PhotoPromptDialog
import se.imorn.ui.PreviewData
import se.imorn.ui.dayText

/**
 * *Imorgon*, stateless: never more than three things, each with *Done* and *Tomorrow*, then what is getting close
 * further ahead. With no chapters it invites the first one; with nothing to do it says so, calmly. No streaks, no
 * guilt, no red numbers: a late step is only marked as late.
 */
@Composable
fun TodayScreen(
    state: TodayUiState,
    onDone: (PlannedStep) -> Unit,
    onTomorrow: (PlannedStep) -> Unit,
    onNewChapter: () -> Unit,
    onOpenStep: (PlannedStep) -> Unit,
    onOpenChapter: (String) -> Unit,
    onAskAssistant: () -> Unit,
    onDownloadAssistant: () -> Unit,
    onTakePhoto: (chapterId: String, stepId: String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    // The photo prompt of a step just done, asked once: chapter id, step id and the prompt.
    var prompting by rememberSaveable { mutableStateOf<List<String>?>(null) }
    val done = { planned: PlannedStep ->
        onDone(planned)
        planned.step.photoPrompt?.let { prompting = listOf(planned.chapterId, planned.step.id, it) }
    }
    val title = state.name?.let { stringResource(R.string.today_greeting, it) }
        ?: stringResource(R.string.today_screen_title)
    ImornScaffold(title = title, modifier = modifier) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(scaffoldPadding)
                .padding(contentPadding)
                .padding(horizontal = ImornTheme.spacing.md, vertical = ImornTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
        ) {
            when {
                !state.hasChapters -> EmptyCard(onNewChapter)

                state.plan.items.isEmpty() -> ImornCard(modifier = Modifier.fillMaxWidth()) {
                    ImornBodyText(text = stringResource(R.string.today_all_done))
                }

                else -> state.plan.items.forEach { item ->
                    ItemCard(item, state, done, onTomorrow, onOpenStep)
                }
            }
            if (state.plan.later.isNotEmpty()) {
                ImornText(
                    text = stringResource(R.string.today_later),
                    style = ImornTheme.typography.titleMedium,
                    color = ImornTheme.colors.onBackground,
                )
                state.plan.later.forEach { LaterRow(it, state) }
            }
            if (state.plan.items.isNotEmpty()) {
                AssistantCard(state.assistant, onAskAssistant, onDownloadAssistant)
            }
            if (state.hasChapters) {
                ImornText(
                    text = stringResource(R.string.today_chapters),
                    style = ImornTheme.typography.titleMedium,
                    color = ImornTheme.colors.onBackground,
                )
                state.chapters.forEach { ChapterRow(it, onOpenChapter) }
                ImornSecondaryButton(text = stringResource(R.string.today_new_chapter), onClick = onNewChapter)
            }
        }
    }
    prompting?.let { (chapterId, stepId, prompt) ->
        PhotoPromptDialog(
            prompt = prompt,
            onTakePhoto = {
                prompting = null
                onTakePhoto(chapterId, stepId)
            },
            onDismiss = { prompting = null },
        )
    }
}

@Composable
private fun EmptyCard(onNewChapter: () -> Unit) {
    ImornCard(modifier = Modifier.fillMaxWidth()) {
        ImornText(text = stringResource(R.string.today_empty_title), style = ImornTheme.typography.titleMedium)
        ImornBodyText(text = stringResource(R.string.today_empty_body))
        ImornButton(text = stringResource(R.string.today_start_chapter), onClick = onNewChapter)
    }
}

@Composable
private fun ItemCard(
    item: TomorrowItem,
    state: TodayUiState,
    onDone: (PlannedStep) -> Unit,
    onTomorrow: (PlannedStep) -> Unit,
    onOpenStep: (PlannedStep) -> Unit,
) {
    val planned = item.planned
    ImornCard(modifier = Modifier.fillMaxWidth(), onClick = { onOpenStep(planned) }) {
        ImornText(
            text = stringResource(R.string.today_item_meta, planned.chapterTitle, dayText(planned.date, state.today)),
            style = ImornTheme.typography.labelLarge,
            color = ImornTheme.colors.onSurfaceVariant,
        )
        ImornText(text = planned.step.title, style = ImornTheme.typography.titleMedium)
        if (item.late) {
            ImornText(
                text = stringResource(R.string.today_late),
                style = ImornTheme.typography.labelLarge,
                color = ImornTheme.colors.secondary,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm)) {
            ImornButton(text = stringResource(R.string.today_done), onClick = { onDone(planned) })
            ImornSecondaryButton(text = stringResource(R.string.today_tomorrow), onClick = { onTomorrow(planned) })
        }
    }
}

/** A chapter in the quiet list: its title and what is next, or that it rests or is finished. */
@Composable
private fun ChapterRow(chapter: Chapter, onOpenChapter: (String) -> Unit) {
    ImornCard(modifier = Modifier.fillMaxWidth(), onClick = { onOpenChapter(chapter.id) }) {
        ImornText(text = chapter.title, style = ImornTheme.typography.bodyLarge)
        val next = chapter.next()
        ImornText(
            text = when {
                chapter.state == ChapterState.Resting -> stringResource(R.string.today_chapter_resting)
                chapter.state == ChapterState.Closed -> stringResource(R.string.today_chapter_closed)
                next != null -> stringResource(R.string.today_chapter_next, next.step.title)
                else -> stringResource(R.string.today_chapter_nothing_left)
            },
            style = ImornTheme.typography.bodyMedium,
            color = ImornTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun LaterRow(planned: PlannedStep, state: TodayUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.xs)) {
        ImornText(text = planned.step.title, style = ImornTheme.typography.bodyLarge)
        ImornText(
            text = stringResource(R.string.today_item_meta, planned.chapterTitle, dayText(planned.date, state.today)),
            style = ImornTheme.typography.bodyMedium,
            color = ImornTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun AssistantCard(
    assistant: AssistantUiState,
    onAskAssistant: () -> Unit,
    onDownloadAssistant: () -> Unit,
) {
    ImornCard(modifier = Modifier.fillMaxWidth()) {
        ImornText(text = stringResource(R.string.assistant_title), style = ImornTheme.typography.titleMedium)
        ImornBodyText(text = availabilityText(assistant.availability))
        assistant.message?.let {
            ImornBodyText(text = it)
            ImornText(
                text = stringResource(R.string.assistant_ai_note),
                style = ImornTheme.typography.bodySmall,
                color = ImornTheme.colors.onSurfaceVariant,
            )
        }
        if (assistant.writing) {
            ImornBodyText(text = stringResource(R.string.assistant_writing))
        }
        if (assistant.failed) {
            ImornText(
                text = stringResource(R.string.assistant_failed),
                style = ImornTheme.typography.bodyMedium,
                color = ImornTheme.colors.error,
            )
        }
        when (assistant.availability) {
            AssistantAvailability.Downloadable, AssistantAvailability.Unavailable ->
                ImornSecondaryButton(
                    text = stringResource(R.string.assistant_download),
                    onClick = onDownloadAssistant,
                )

            AssistantAvailability.Available ->
                ImornSecondaryButton(
                    text = stringResource(R.string.assistant_ask),
                    onClick = onAskAssistant,
                    enabled = !assistant.writing,
                )

            AssistantAvailability.Downloading, AssistantAvailability.Unknown -> Unit
        }
    }
}

@Composable
private fun availabilityText(availability: AssistantAvailability): String = when (availability) {
    AssistantAvailability.Unknown -> stringResource(R.string.assistant_checking)
    AssistantAvailability.Unavailable -> stringResource(R.string.assistant_unavailable)
    AssistantAvailability.Downloadable -> stringResource(R.string.assistant_downloadable)
    AssistantAvailability.Downloading -> stringResource(R.string.assistant_downloading)
    AssistantAvailability.Available -> stringResource(R.string.assistant_available)
}

private fun previewState(hasChapters: Boolean = true): TodayUiState {
    val chapters = if (hasChapters) listOf(PreviewData.moving) else emptyList()
    return TodayUiState(
        today = PreviewData.TODAY,
        plan = tomorrowPlan(chapters, PreviewData.TODAY),
        chapters = chapters,
        assistant = AssistantUiState(availability = AssistantAvailability.Available),
        name = PreviewData.NAME.takeIf { hasChapters },
    )
}

@Composable
private fun PreviewScreen(state: TodayUiState) {
    ImornTheme {
        TodayScreen(
            state = state,
            onDone = {},
            onTomorrow = {},
            onNewChapter = {},
            onOpenStep = {},
            onOpenChapter = {},
            onAskAssistant = {},
            onDownloadAssistant = {},
            onTakePhoto = { _, _ -> },
            contentPadding = PaddingValues(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodayScreenPreview() {
    PreviewScreen(previewState())
}

@Preview(showBackground = true)
@Composable
private fun TodayScreenEmptyPreview() {
    PreviewScreen(previewState(hasChapters = false))
}

@Preview(showBackground = true)
@Composable
private fun TodayScreenAssistantPreview() {
    PreviewScreen(
        previewState().copy(
            assistant = AssistantUiState(
                availability = AssistantAvailability.Available,
                message = PreviewData.ASSISTANT_MESSAGE,
            ),
        ),
    )
}
