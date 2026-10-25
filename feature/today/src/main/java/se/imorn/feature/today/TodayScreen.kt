package se.imorn.feature.today

import android.content.res.Configuration
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.imorn.coach.AssistantAvailability
import se.imorn.designsystem.component.ImornBodyText
import se.imorn.designsystem.component.ImornButton
import se.imorn.designsystem.component.ImornCard
import se.imorn.designsystem.component.ImornCheckCircle
import se.imorn.designsystem.component.ImornCountdown
import se.imorn.designsystem.component.ImornDateBadge
import se.imorn.designsystem.component.ImornDayTone
import se.imorn.designsystem.component.ImornNowCard
import se.imorn.designsystem.component.ImornScaffold
import se.imorn.designsystem.component.ImornSecondaryButton
import se.imorn.designsystem.component.ImornShelf
import se.imorn.designsystem.component.ImornSpine
import se.imorn.designsystem.component.ImornSpineTone
import se.imorn.designsystem.component.ImornText
import se.imorn.designsystem.component.ImornTextButton
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.model.ChapterState
import se.imorn.model.PlannedStep
import se.imorn.model.TomorrowItem
import se.imorn.model.progress
import se.imorn.model.tomorrowPlan
import se.imorn.ui.PhotoPromptDialog
import se.imorn.ui.PreviewData
import se.imorn.ui.dayNumber
import se.imorn.ui.longDayText
import se.imorn.ui.monthShort
import java.util.Locale

/**
 * *Imorgon*, stateless. The day and a greeting, the countdown to the anchor coming up, and the one thing to do now
 * in its apricot block with *Done* and *Tomorrow*. Then at most two more, each with a date and a box to tick, what
 * is getting close further ahead, the assistant, and the chapters as books on a shelf. With no chapters it invites
 * the first one; with nothing to do it says so, calmly. No streaks, no guilt, no red numbers: a late step is only
 * marked as late.
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
                .padding(scaffoldPadding)
                .padding(contentPadding)
                .padding(horizontal = ImornTheme.spacing.md + ImornTheme.spacing.xs, vertical = ImornTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.lg),
        ) {
            Header(state)
            val first = state.plan.items.firstOrNull()
            when {
                !state.hasChapters -> EmptyCard(onNewChapter)

                first == null -> ImornCard(modifier = Modifier.fillMaxWidth()) {
                    ImornText(text = stringResource(R.string.today_all_done), style = ImornTheme.typography.titleLarge)
                }

                else -> NowCard(first, done, onTomorrow, onOpenStep)
            }
            val rest = state.plan.items.drop(1)
            if (rest.isNotEmpty()) {
                Column {
                    rest.forEach { item -> NextRow(item.planned, state, onCheck = done, onOpenStep = onOpenStep) }
                }
            }
            if (state.plan.later.isNotEmpty()) {
                Column {
                    SectionTitle(stringResource(R.string.today_later))
                    state.plan.later.forEach { NextRow(it, state, onCheck = null, onOpenStep = onOpenStep) }
                }
            }
            if (state.plan.items.isNotEmpty()) {
                AssistantCard(state.assistant, onAskAssistant, onDownloadAssistant)
            }
            if (state.hasChapters) {
                Shelf(state, onOpenChapter, onNewChapter)
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

/** The day and a greeting for the hour, with the countdown to the anchor coming up beside them. */
@Composable
private fun Header(state: TodayUiState) {
    Row(verticalAlignment = Alignment.Top) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.xs),
        ) {
            ImornText(
                text = longDayText(state.today),
                style = ImornTheme.typography.bodyMedium,
                color = ImornTheme.colors.onSurfaceVariant,
            )
            ImornText(
                text = stringResource(greeting(state.hour)),
                style = ImornTheme.typography.headlineLarge,
                color = ImornTheme.colors.onBackground,
                modifier = Modifier.semantics { heading() },
            )
        }
        val milestone = state.milestone
        if (milestone != null) {
            val days = milestone.daysFrom(state.today).toInt()
            ImornCountdown(
                number = days.toString(),
                label = pluralStringResource(R.plurals.today_countdown, days, milestone.anchor.label),
            )
        }
    }
}

@Composable
private fun EmptyCard(onNewChapter: () -> Unit) {
    ImornCard(modifier = Modifier.fillMaxWidth()) {
        ImornText(text = stringResource(R.string.today_empty_title), style = ImornTheme.typography.headlineSmall)
        ImornBodyText(text = stringResource(R.string.today_empty_body))
        ImornButton(text = stringResource(R.string.today_start_chapter), onClick = onNewChapter)
    }
}

/** The one thing to do now, big, in apricot. */
@Composable
private fun NowCard(
    item: TomorrowItem,
    onDone: (PlannedStep) -> Unit,
    onTomorrow: (PlannedStep) -> Unit,
    onOpenStep: (PlannedStep) -> Unit,
) {
    val planned = item.planned
    ImornNowCard(
        chapter = planned.chapterTitle,
        meta = if (item.late) stringResource(R.string.today_late) else null,
        title = planned.step.title,
        why = planned.step.why,
        doneText = stringResource(R.string.today_done),
        tomorrowText = stringResource(R.string.today_tomorrow),
        onDone = { onDone(planned) },
        onTomorrow = { onTomorrow(planned) },
        onOpen = { onOpenStep(planned) },
        openLabel = stringResource(R.string.today_open_step),
    )
}

/** A step coming up: its day, its title and chapter, and with [onCheck] a box to tick it off. */
@Composable
private fun NextRow(
    planned: PlannedStep,
    state: TodayUiState,
    onCheck: ((PlannedStep) -> Unit)?,
    onOpenStep: (PlannedStep) -> Unit,
) {
    val date = planned.date
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenStep(planned) }
            .padding(vertical = ImornTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
    ) {
        ImornDateBadge(
            day = dayNumber(date),
            month = date?.let { monthShort(it) },
            tone = if (date != null && date < state.today) ImornDayTone.Now else ImornDayTone.Ahead,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            ImornText(text = planned.step.title, style = ImornTheme.typography.titleMedium)
            ImornText(
                text = planned.chapterTitle,
                style = ImornTheme.typography.bodySmall,
                color = ImornTheme.colors.onSurfaceVariant,
            )
        }
        if (onCheck != null) {
            ImornCheckCircle(
                contentDescription = stringResource(R.string.today_mark_done, planned.step.title),
                onClick = { onCheck(planned) },
            )
        }
    }
}

/** The chapters as books on a shelf, with the way to start another. */
@Composable
private fun Shelf(state: TodayUiState, onOpenChapter: (String) -> Unit, onNewChapter: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(stringResource(R.string.today_chapters), modifier = Modifier.weight(1f))
            ImornTextButton(text = stringResource(R.string.today_new_chapter), onClick = onNewChapter)
        }
        val fractionFormat = stringResource(R.string.today_chapter_fraction)
        val resting = stringResource(R.string.today_chapter_resting)
        val closed = stringResource(R.string.today_chapter_closed)
        ImornShelf(
            spines = state.chapters.mapIndexed { index, chapter ->
                val progress = chapter.progress()
                ImornSpine(
                    title = chapter.title,
                    meta = when (chapter.state) {
                        ChapterState.Resting -> resting
                        ChapterState.Closed -> closed
                        ChapterState.Active -> fraction(fractionFormat, progress.done, progress.total)
                    },
                    tone = SpineTones[index % SpineTones.size],
                    height = SpineHeights[index % SpineHeights.size],
                    onClick = { onOpenChapter(chapter.id) },
                )
            },
        )
    }
}

/** *2/5*, or nothing for a chapter without steps. */
private fun fraction(format: String, done: Int, total: Int): String? =
    if (total > 0) String.format(Locale.ROOT, format, done, total) else null

private val SpineTones = listOf(ImornSpineTone.Indigo, ImornSpineTone.Rose, ImornSpineTone.Sand, ImornSpineTone.Apricot)
private val SpineHeights = listOf(1f, 0.82f, 0.9f, 0.76f)

private fun greeting(hour: Int) = when (hour) {
    in MORNING until DAY -> R.string.today_greeting_morning
    in DAY until EVENING -> R.string.today_greeting_day
    in EVENING until NIGHT -> R.string.today_greeting_evening
    else -> R.string.today_greeting_night
}

private const val MORNING = 5
private const val DAY = 10
private const val EVENING = 17
private const val NIGHT = 22

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    ImornText(
        text = text,
        style = ImornTheme.typography.titleLarge,
        color = ImornTheme.colors.onBackground,
        modifier = modifier.semantics { heading() },
    )
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
private fun TodayScreenDarkPreview() {
    ImornTheme(darkTheme = true) {
        PreviewContent(previewState().copy(hour = 19))
    }
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
