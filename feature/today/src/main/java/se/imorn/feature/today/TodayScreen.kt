package se.imorn.feature.today

import android.content.res.Configuration
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import se.imorn.coach.AssistantAvailability
import se.imorn.designsystem.component.ImornBodyText
import se.imorn.designsystem.component.ImornButton
import se.imorn.designsystem.component.ImornCard
import se.imorn.designsystem.component.ImornChapterMark
import se.imorn.designsystem.component.ImornGlassPanel
import se.imorn.designsystem.component.ImornIcon
import se.imorn.designsystem.component.ImornIcons
import se.imorn.designsystem.component.ImornScaffold
import se.imorn.designsystem.component.ImornSceneButton
import se.imorn.designsystem.component.ImornSceneText
import se.imorn.designsystem.component.ImornSceneTextButton
import se.imorn.designsystem.component.ImornSecondaryButton
import se.imorn.designsystem.component.ImornSkyHero
import se.imorn.designsystem.component.ImornSkyTime
import se.imorn.designsystem.component.ImornTag
import se.imorn.designsystem.component.ImornTagTone
import se.imorn.designsystem.component.ImornText
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.model.Chapter
import se.imorn.model.ChapterState
import se.imorn.model.PlannedStep
import se.imorn.model.TomorrowItem
import se.imorn.model.next
import se.imorn.model.progress
import se.imorn.model.tomorrowPlan
import se.imorn.ui.PhotoPromptDialog
import se.imorn.ui.PreviewData
import se.imorn.ui.dayText

/**
 * *Imorgon*, stateless. The sky at this hour opens the screen, counting down to the anchor coming up, with the one
 * thing to do now and its *Done* and *Tomorrow*. Then at most two more, what is getting close further ahead, the
 * chapters with how far along they are, and the assistant. With no chapters it invites the first one; with nothing
 * to do it says so, calmly. No streaks, no guilt, no red numbers: a late step is only marked as late.
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
    val done: (PlannedStep) -> Unit = { planned ->
        onDone(planned)
        val prompt = planned.step.photoPrompt
        if (prompt != null) prompting = listOf(planned.chapterId, planned.step.id, prompt)
    }
    ImornScaffold(modifier = modifier) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                // The sky reaches up behind the status bar and keeps its text clear of it itself.
                .padding(bottom = scaffoldPadding.calculateBottomPadding())
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.lg),
        ) {
            Hero(state, done, onTomorrow, onOpenStep, onNewChapter)
            Column(
                modifier = Modifier.padding(horizontal = ImornTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
            ) {
                val rest = state.plan.items.drop(1)
                if (rest.isNotEmpty()) {
                    SectionTitle(stringResource(R.string.today_after_that))
                    rest.forEach { item -> ItemCard(item, state, done, onTomorrow, onOpenStep) }
                }
                if (state.plan.later.isNotEmpty()) {
                    SectionTitle(stringResource(R.string.today_later))
                    LaterCard(state.plan.later, state, onOpenStep)
                }
                if (state.plan.items.isNotEmpty()) {
                    AssistantCard(state.assistant, onAskAssistant, onDownloadAssistant)
                }
                if (state.hasChapters) {
                    SectionTitle(stringResource(R.string.today_chapters))
                    state.chapters.forEach { ChapterCard(it, state, onOpenChapter) }
                    NewChapterCard(onNewChapter)
                }
                Spacer(Modifier.height(ImornTheme.spacing.lg))
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

/** The sky: a greeting for the hour, the countdown, and the one thing to do now (or the invitation to start). */
@Composable
private fun Hero(
    state: TodayUiState,
    onDone: (PlannedStep) -> Unit,
    onTomorrow: (PlannedStep) -> Unit,
    onOpenStep: (PlannedStep) -> Unit,
    onNewChapter: () -> Unit,
) {
    ImornSkyHero(hour = state.hour) {
        Column(verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.xs)) {
            ImornSceneText(text = stringResource(greeting(state.hour)), style = ImornTheme.typography.headlineMedium)
            ImornSceneText(text = subtitle(state), style = ImornTheme.typography.bodyLarge, muted = true)
        }
        val first = state.plan.items.firstOrNull()
        when {
            !state.hasChapters -> ImornGlassPanel(modifier = Modifier.fillMaxWidth()) {
                ImornSceneText(
                    text = stringResource(R.string.today_empty_title),
                    style = ImornTheme.typography.titleLarge,
                )
                ImornSceneText(
                    text = stringResource(R.string.today_empty_body),
                    style = ImornTheme.typography.bodyLarge,
                    muted = true,
                )
                Spacer(Modifier.height(ImornTheme.spacing.sm))
                ImornSceneButton(text = stringResource(R.string.today_start_chapter), onClick = onNewChapter)
            }

            first == null -> ImornGlassPanel(modifier = Modifier.fillMaxWidth()) {
                ImornSceneText(
                    text = stringResource(R.string.today_all_done),
                    style = ImornTheme.typography.titleMedium,
                )
            }

            else -> NowPanel(first, state, onDone, onTomorrow, onOpenStep)
        }
    }
}

/** The one thing to do now, on glass in the sky: big, with why it matters and the two answers. */
@Composable
private fun NowPanel(
    item: TomorrowItem,
    state: TodayUiState,
    onDone: (PlannedStep) -> Unit,
    onTomorrow: (PlannedStep) -> Unit,
    onOpenStep: (PlannedStep) -> Unit,
) {
    val planned = item.planned
    ImornGlassPanel(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClickLabel = stringResource(R.string.today_open_step)) { onOpenStep(planned) },
            verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.xs),
        ) {
            ImornSceneText(
                text = stringResource(
                    R.string.today_item_meta,
                    planned.chapterTitle,
                    dayText(planned.date, state.today),
                ),
                style = ImornTheme.typography.labelLarge,
                muted = true,
            )
            ImornSceneText(text = planned.step.title, style = ImornTheme.typography.headlineSmall)
            planned.step.why?.let { ImornSceneText(text = it, style = ImornTheme.typography.bodyMedium, muted = true) }
        }
        if (item.late) {
            ImornTag(text = stringResource(R.string.today_late), tone = ImornTagTone.Now)
        }
        Spacer(Modifier.height(ImornTheme.spacing.xs))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm),
        ) {
            ImornSceneButton(text = stringResource(R.string.today_done), onClick = { onDone(planned) })
            ImornSceneTextButton(text = stringResource(R.string.today_tomorrow), onClick = { onTomorrow(planned) })
        }
    }
}

private fun greeting(hour: Int) = when (ImornSkyTime.at(hour)) {
    ImornSkyTime.Morning -> R.string.today_greeting_morning
    ImornSkyTime.Day -> R.string.today_greeting_day
    ImornSkyTime.Evening -> R.string.today_greeting_evening
    ImornSkyTime.Night -> R.string.today_greeting_night
}

/** Under the greeting: the countdown to the anchor coming up, or a calm word. */
@Composable
private fun subtitle(state: TodayUiState): String {
    val milestone = state.milestone
    return when {
        milestone != null -> {
            val days = milestone.daysFrom(state.today)
            val label = milestone.anchor.label
            when (days) {
                0L -> stringResource(R.string.today_milestone_today, label)
                1L -> stringResource(R.string.today_milestone_tomorrow, label)
                else -> pluralStringResource(R.plurals.today_milestone_days, days.toInt(), days.toInt(), label)
            }
        }

        state.hasChapters -> stringResource(R.string.today_one_thing)

        else -> stringResource(R.string.today_welcome)
    }
}

@Composable
private fun SectionTitle(text: String) {
    ImornText(
        text = text,
        style = ImornTheme.typography.titleMedium,
        color = ImornTheme.colors.onBackground,
        modifier = Modifier.semantics { heading() },
    )
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
        Row(horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md)) {
            ImornChapterMark(icon = state.iconOf(planned.chapterId))
            Column(verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm)) {
                ImornText(
                    text = stringResource(
                        R.string.today_item_meta,
                        planned.chapterTitle,
                        dayText(planned.date, state.today),
                    ),
                    style = ImornTheme.typography.labelLarge,
                    color = ImornTheme.colors.onSurfaceVariant,
                )
                ImornText(text = planned.step.title, style = ImornTheme.typography.titleMedium)
                if (item.late) {
                    ImornTag(text = stringResource(R.string.today_late), tone = ImornTagTone.Now)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm)) {
                    ImornButton(text = stringResource(R.string.today_done), onClick = { onDone(planned) })
                    ImornSecondaryButton(
                        text = stringResource(R.string.today_tomorrow),
                        onClick = { onTomorrow(planned) },
                    )
                }
            }
        }
    }
}

/** A chapter: its mark with a ring for how far along it is, what is next, and the share done. */
@Composable
private fun ChapterCard(chapter: Chapter, state: TodayUiState, onOpenChapter: (String) -> Unit) {
    val progress = chapter.progress()
    ImornCard(modifier = Modifier.fillMaxWidth(), onClick = { onOpenChapter(chapter.id) }) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
        ) {
            ImornChapterMark(icon = state.iconOf(chapter), progress = progress.share)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.xs),
            ) {
                ImornText(text = chapter.title, style = ImornTheme.typography.titleMedium)
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
                if (progress.total > 0) {
                    ImornText(
                        text = stringResource(R.string.today_chapter_progress, progress.done, progress.total),
                        style = ImornTheme.typography.labelMedium,
                        color = ImornTheme.colors.secondary,
                    )
                }
            }
            ImornIcon(icon = ImornIcons.Forward, contentDescription = null)
        }
    }
}

/** Starting another chapter, as a card among the chapters. */
@Composable
private fun NewChapterCard(onNewChapter: () -> Unit) {
    ImornCard(modifier = Modifier.fillMaxWidth(), onClick = onNewChapter) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
        ) {
            ImornIcon(
                icon = ImornIcons.Add,
                contentDescription = null,
                tint = ImornTheme.colors.primary,
                modifier = Modifier.size(ImornTheme.spacing.xl),
            )
            Column(verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.xs)) {
                ImornText(
                    text = stringResource(R.string.today_new_chapter),
                    style = ImornTheme.typography.titleMedium,
                    color = ImornTheme.colors.primary,
                )
                ImornText(
                    text = stringResource(R.string.today_new_chapter_body),
                    style = ImornTheme.typography.bodyMedium,
                    color = ImornTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

/** The long threads getting close, in one quiet card. */
@Composable
private fun LaterCard(later: List<PlannedStep>, state: TodayUiState, onOpenStep: (PlannedStep) -> Unit) {
    ImornCard(modifier = Modifier.fillMaxWidth()) {
        later.forEach { planned ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenStep(planned) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
            ) {
                ImornIcon(icon = ImornIcons.Calendar, contentDescription = null, tint = ImornTheme.colors.primary)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.xs),
                ) {
                    ImornText(text = planned.step.title, style = ImornTheme.typography.bodyLarge)
                    ImornText(
                        text = stringResource(
                            R.string.today_item_meta,
                            planned.chapterTitle,
                            dayText(planned.date, state.today),
                        ),
                        style = ImornTheme.typography.bodyMedium,
                        color = ImornTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
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
        hour = PreviewData.HOUR,
        templateIcons = PreviewData.templateIcons,
    )
}

@Composable
private fun PreviewScreen(state: TodayUiState) {
    ImornTheme {
        PreviewContent(state)
    }
}

@Composable
private fun PreviewContent(state: TodayUiState) {
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

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun TodayScreenPreview() {
    PreviewScreen(previewState())
}

@Preview(showBackground = true, heightDp = 1400, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TodayScreenEveningDarkPreview() {
    ImornTheme(darkTheme = true) {
        PreviewContent(previewState().copy(hour = 19))
    }
}

@Preview(showBackground = true)
@Composable
private fun TodayScreenNightPreview() {
    PreviewScreen(previewState().copy(hour = 23))
}

@Preview(showBackground = true)
@Composable
private fun TodayScreenEmptyPreview() {
    PreviewScreen(previewState(hasChapters = false))
}

@Preview(showBackground = true, heightDp = 1400)
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
