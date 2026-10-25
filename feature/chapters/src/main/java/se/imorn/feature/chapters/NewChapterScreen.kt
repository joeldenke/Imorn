package se.imorn.feature.chapters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
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
import se.imorn.designsystem.component.ImornChapterMark
import se.imorn.designsystem.component.ImornChoiceCard
import se.imorn.designsystem.component.ImornDatePickerDialog
import se.imorn.designsystem.component.ImornIcon
import se.imorn.designsystem.component.ImornIcons
import se.imorn.designsystem.component.ImornScaffold
import se.imorn.designsystem.component.ImornTag
import se.imorn.designsystem.component.ImornTagTone
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
                template == null -> TemplateStage(state, language, onChooseTemplate)
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
private fun SectionTitle(text: String) {
    ImornText(
        text = text,
        style = ImornTheme.typography.titleMedium,
        color = ImornTheme.colors.onBackground,
        modifier = Modifier.semantics { heading() },
    )
}

/** The library: the templates for the user's life stages first, then the rest, two by two. */
@Composable
private fun TemplateStage(state: NewChapterUiState, language: String, onChoose: (Template) -> Unit) {
    Heading(stringResource(R.string.new_chapter_what))
    ImornBodyText(text = stringResource(R.string.new_chapter_what_body))
    val suggested = state.templates.take(state.suggested)
    val rest = state.templates.drop(state.suggested)
    if (suggested.isNotEmpty()) {
        SectionTitle(stringResource(R.string.new_chapter_suggested))
        TemplateGrid(suggested, language, onChoose)
        if (rest.isNotEmpty()) SectionTitle(stringResource(R.string.new_chapter_everything))
    }
    TemplateGrid(rest, language, onChoose)
}

@Composable
private fun TemplateGrid(templates: List<Template>, language: String, onChoose: (Template) -> Unit) {
    templates.chunked(COLUMNS).forEach { row ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
        ) {
            row.forEach { template ->
                TemplateTile(
                    template = template,
                    language = language,
                    onClick = { onChoose(template) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
            repeat(COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

private const val COLUMNS = 2

@Composable
private fun TemplateTile(template: Template, language: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ImornCard(modifier = modifier, onClick = onClick) {
        ImornChapterMark(icon = template.icon)
        ImornText(text = template.title.text(language), style = ImornTheme.typography.titleMedium)
        if (template.summary.isNotEmpty()) {
            ImornText(
                text = template.summary.text(language),
                style = ImornTheme.typography.bodySmall,
                color = ImornTheme.colors.onSurfaceVariant,
            )
        }
    }
}

/** The template being started, at the top of the questions and the dates, with how far along the user is. */
@Composable
private fun TemplateHeader(template: Template, language: String, step: Int, steps: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
    ) {
        ImornChapterMark(icon = template.icon, progress = step.toFloat() / steps)
        Column(verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.xs)) {
            ImornText(
                text = template.title.text(language),
                style = ImornTheme.typography.titleMedium,
                color = ImornTheme.colors.onBackground,
            )
            ImornText(
                text = stringResource(R.string.new_chapter_step_of, step, steps),
                style = ImornTheme.typography.labelLarge,
                color = ImornTheme.colors.onSurfaceVariant,
            )
        }
    }
}

/** The questions and then the dates: one stage more than there are questions. */
private val Template.stages: Int get() = questions.size + 1

@Composable
private fun QuestionStage(
    state: NewChapterUiState,
    template: Template,
    question: Question,
    language: String,
    onAnswer: (Answer?) -> Unit,
    onBack: () -> Unit,
) {
    TemplateHeader(template, language, step = state.question + 1, steps = template.stages)
    ImornText(
        text = stringResource(R.string.new_chapter_progress, state.question + 1, template.questions.size),
        style = ImornTheme.typography.labelLarge,
        color = ImornTheme.colors.secondary,
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
    TemplateHeader(template, language, step = template.stages, steps = template.stages)
    Heading(stringResource(R.string.new_chapter_dates_title))
    ImornBodyText(text = stringResource(R.string.new_chapter_dates_body))
    template.anchors.forEach { anchor ->
        val date = state.anchorDates[anchor.id]
        ImornCard(modifier = Modifier.fillMaxWidth(), onClick = { picking = anchor.id }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
            ) {
                ImornIcon(icon = ImornIcons.Calendar, contentDescription = null, tint = ImornTheme.colors.primary)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.xs),
                ) {
                    ImornText(text = anchor.label.text(language), style = ImornTheme.typography.titleMedium)
                    if (date == null) {
                        ImornTag(text = stringResource(R.string.new_chapter_date_unknown))
                    } else {
                        ImornTag(text = dayText(date, state.today), tone = ImornTagTone.Ahead)
                    }
                }
                if (date != null) {
                    ImornTextButton(
                        text = stringResource(R.string.new_chapter_clear_date),
                        onClick = { onSetAnchor(anchor.id, null) },
                    )
                } else {
                    ImornText(
                        text = stringResource(R.string.new_chapter_choose_date),
                        style = ImornTheme.typography.labelLarge,
                        color = ImornTheme.colors.primary,
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
    summary = both("Från uppsägningen till första frukosten", "From giving notice to the first breakfast"),
    icon = "home",
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

private fun previewLibraryTemplate(id: String, icon: String, title: String, summary: String) = Template(
    id = id,
    version = 1,
    title = both(title, title),
    summary = both(summary, summary),
    icon = icon,
)

private val previewLibrary = listOf(
    previewTemplate,
    previewLibraryTemplate("buy-home", "key", "Buying a home", "The pre-approval, the viewings and the keys"),
    previewLibraryTemplate("renovate", "hammer", "Renovating", "Before photos, quotes, certificates and the warranty"),
    previewLibraryTemplate("baby", "heart", "Expecting a baby", "From the midwife to the name and preschool"),
    previewLibraryTemplate("wedding", "rings", "Getting married", "The licence, the day and the name afterwards"),
    previewLibraryTemplate("study", "book", "Starting university", "The application, student finance and housing"),
    previewLibraryTemplate("retire", "sun", "Retiring", "The overview, the application and the last day"),
)

private val previewState = NewChapterUiState(templates = listOf(previewTemplate), today = PreviewData.TODAY)

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun NewChapterTemplatePreview() {
    PreviewScreen(previewState.copy(templates = previewLibrary, suggested = 3))
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
