package se.imorn.feature.chapters

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
import se.imorn.content.Answer
import se.imorn.content.ENGLISH
import se.imorn.content.Option
import se.imorn.content.Question
import se.imorn.content.QuestionType
import se.imorn.content.SWEDISH
import se.imorn.content.Template
import se.imorn.content.TemplateAnchor
import se.imorn.content.text
import se.imorn.designsystem.component.ImornBodyText
import se.imorn.designsystem.component.ImornButton
import se.imorn.designsystem.component.ImornCard
import se.imorn.designsystem.component.ImornChoiceCard
import se.imorn.designsystem.component.ImornDatePickerDialog
import se.imorn.designsystem.component.ImornScaffold
import se.imorn.designsystem.component.ImornSecondaryButton
import se.imorn.designsystem.component.ImornText
import se.imorn.designsystem.component.ImornTextButton
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.ui.PreviewData
import se.imorn.ui.dayText
import java.time.LocalDate

/**
 * Starting a chapter, stateless: one stage at a time (template, questions, dates), big cards for the thumb, and
 * always a way back. Template texts are shown in [language], the app's own texts come from the strings.
 */
@Composable
fun NewChapterScreen(
    state: NewChapterUiState,
    language: String,
    onChooseTemplate: (Template) -> Unit,
    onAnswer: (Answer?) -> Unit,
    onBack: () -> Unit,
    onSetAnchor: (String, LocalDate?) -> Unit,
    onCreate: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    ImornScaffold(title = stringResource(R.string.new_chapter_title), modifier = modifier) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(scaffoldPadding)
                .padding(contentPadding)
                .padding(horizontal = ImornTheme.spacing.md, vertical = ImornTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
        ) {
            val template = state.template
            val question = state.currentQuestion
            when {
                template == null -> TemplateStage(state.templates, language, onChooseTemplate)
                question != null -> QuestionStage(state, template, question, language, onAnswer, onBack)
                else -> DatesStage(state, template, language, onSetAnchor, onCreate, onBack)
            }
        }
    }
}

@Composable
private fun Heading(text: String) {
    ImornText(text = text, style = ImornTheme.typography.headlineSmall, color = ImornTheme.colors.onBackground)
}

@Composable
private fun TemplateStage(templates: List<Template>, language: String, onChoose: (Template) -> Unit) {
    Heading(stringResource(R.string.new_chapter_what))
    templates.forEach { template ->
        ImornChoiceCard(selected = false, onClick = { onChoose(template) }, modifier = Modifier.fillMaxWidth()) {
            ImornText(text = template.title.text(language), style = ImornTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun QuestionStage(
    state: NewChapterUiState,
    template: Template,
    question: Question,
    language: String,
    onAnswer: (Answer?) -> Unit,
    onBack: () -> Unit,
) {
    ImornText(
        text = stringResource(R.string.new_chapter_progress, state.question + 1, template.questions.size),
        style = ImornTheme.typography.labelLarge,
        color = ImornTheme.colors.onSurfaceVariant,
    )
    Heading(question.text.text(language))
    val current = state.answers[question.id]
    val choices: List<Pair<String, Answer>> = when (question.type) {
        QuestionType.Choice -> question.options.map { it.label.text(language) to Answer.Choice(it.id) }

        QuestionType.YesNo -> listOf(
            stringResource(R.string.new_chapter_yes) to Answer.YesNo(true),
            stringResource(R.string.new_chapter_no) to Answer.YesNo(false),
        )
    }
    choices.forEach { (label, answer) ->
        ImornChoiceCard(
            selected = current == answer,
            onClick = { onAnswer(answer) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            ImornText(text = label, style = ImornTheme.typography.titleMedium)
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm)) {
        ImornTextButton(text = stringResource(R.string.new_chapter_back), onClick = onBack)
        ImornTextButton(text = stringResource(R.string.new_chapter_skip), onClick = { onAnswer(null) })
    }
}

@Composable
private fun DatesStage(
    state: NewChapterUiState,
    template: Template,
    language: String,
    onSetAnchor: (String, LocalDate?) -> Unit,
    onCreate: () -> Unit,
    onBack: () -> Unit,
) {
    var picking by rememberSaveable { mutableStateOf<String?>(null) }
    Heading(stringResource(R.string.new_chapter_dates_title))
    ImornBodyText(text = stringResource(R.string.new_chapter_dates_body))
    template.anchors.forEach { anchor ->
        val date = state.anchorDates[anchor.id]
        ImornCard(modifier = Modifier.fillMaxWidth()) {
            ImornText(text = anchor.label.text(language), style = ImornTheme.typography.titleMedium)
            ImornBodyText(
                text = if (date == null) {
                    stringResource(R.string.new_chapter_date_unknown)
                } else {
                    dayText(date, state.today)
                },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm)) {
                ImornSecondaryButton(
                    text = stringResource(R.string.new_chapter_choose_date),
                    onClick = { picking = anchor.id },
                )
                if (date != null) {
                    ImornTextButton(
                        text = stringResource(R.string.new_chapter_clear_date),
                        onClick = { onSetAnchor(anchor.id, null) },
                    )
                }
            }
        }
    }
    ImornButton(
        text = stringResource(R.string.new_chapter_create),
        onClick = onCreate,
        enabled = !state.saving,
        modifier = Modifier.fillMaxWidth(),
    )
    ImornTextButton(text = stringResource(R.string.new_chapter_back), onClick = onBack)
    ImornText(
        text = stringResource(R.string.new_chapter_note),
        style = ImornTheme.typography.bodySmall,
        color = ImornTheme.colors.onSurfaceVariant,
    )
    picking?.let { id ->
        ImornDatePickerDialog(
            initial = state.anchorDates[id] ?: state.today,
            confirmText = stringResource(R.string.new_chapter_date_ok),
            dismissText = stringResource(R.string.new_chapter_date_cancel),
            onPick = {
                onSetAnchor(id, it)
                picking = null
            },
            onDismiss = { picking = null },
        )
    }
}

private fun both(sv: String, en: String) = mapOf(SWEDISH to sv, ENGLISH to en)

private val previewTemplate = Template(
    id = "move",
    version = 1,
    title = both("Flytta", "Moving"),
    anchors = listOf(TemplateAnchor("moveDay", both("Flyttdag", "Moving day"))),
    questions = listOf(
        Question(
            id = "housing",
            text = both("Vad flyttar du från?", "What are you moving from?"),
            options = listOf(
                Option("rental", both("Hyresrätt", "Rented flat")),
                Option("condo", both("Bostadsrätt", "Owned flat")),
                Option("house", both("Hus", "House")),
            ),
        ),
        Question(
            id = "kids",
            text = both("Byter barnen skola eller förskola?", "Are the children changing school or preschool?"),
            type = QuestionType.YesNo,
        ),
    ),
)

@Composable
private fun PreviewScreen(state: NewChapterUiState) {
    ImornTheme {
        NewChapterScreen(
            state = state,
            language = ENGLISH,
            onChooseTemplate = {},
            onAnswer = {},
            onBack = {},
            onSetAnchor = { _, _ -> },
            onCreate = {},
            contentPadding = PaddingValues(),
        )
    }
}

private val previewState = NewChapterUiState(templates = listOf(previewTemplate), today = PreviewData.TODAY)

@Preview(showBackground = true)
@Composable
private fun NewChapterTemplatePreview() {
    PreviewScreen(previewState)
}

@Preview(showBackground = true)
@Composable
private fun NewChapterQuestionPreview() {
    PreviewScreen(
        previewState.copy(template = previewTemplate, answers = mapOf("housing" to Answer.Choice("rental"))),
    )
}

@Preview(showBackground = true)
@Composable
private fun NewChapterDatesPreview() {
    PreviewScreen(
        previewState.copy(
            template = previewTemplate,
            question = previewTemplate.questions.size,
            anchorDates = mapOf("moveDay" to LocalDate.of(2027, 3, 1)),
        ),
    )
}
