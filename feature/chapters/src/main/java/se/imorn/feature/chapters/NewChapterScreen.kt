package se.imorn.feature.chapters

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.runtime.remember
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
import se.imorn.content.TemplateCategory
import se.imorn.content.groupedByCategory
import se.imorn.content.inCategory
import se.imorn.content.matching
import se.imorn.content.text
import se.imorn.designsystem.component.ImornBodyText
import se.imorn.designsystem.component.ImornButton
import se.imorn.designsystem.component.ImornCard
import se.imorn.designsystem.component.ImornChoiceCard
import se.imorn.designsystem.component.ImornDatePickerDialog
import se.imorn.designsystem.component.ImornFilterChip
import se.imorn.designsystem.component.ImornScaffold
import se.imorn.designsystem.component.ImornSecondaryButton
import se.imorn.designsystem.component.ImornText
import se.imorn.designsystem.component.ImornTextButton
import se.imorn.designsystem.component.ImornTextField
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.ui.PreviewData
import se.imorn.ui.dayText
import java.time.LocalDate

/**
 * Starting a chapter, stateless: first the template gallery (or a blank project), then one question at a time and
 * the anchor dates. Big cards for the thumb, and always a way back. Template texts are shown in [language], the
 * app's own texts come from the strings.
 */
@Composable
fun NewChapterScreen(
    state: NewChapterUiState,
    language: String,
    onChooseTemplate: (Template) -> Unit,
    onStartBlank: () -> Unit,
    onCreateBlank: (String) -> Unit,
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
                state.blank -> BlankStage(state, onBack, onCreateBlank)
                template == null -> TemplateStage(state, language, onChooseTemplate, onStartBlank)
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
private fun SectionHeading(text: String) {
    ImornText(text = text, style = ImornTheme.typography.titleMedium, color = ImornTheme.colors.onBackground)
}

@Composable
private fun TemplateStage(
    state: NewChapterUiState,
    language: String,
    onChoose: (Template) -> Unit,
    onStartBlank: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<TemplateCategory?>(null) }

    Heading(stringResource(R.string.new_chapter_what))
    ImornCard(onClick = onStartBlank, modifier = Modifier.fillMaxWidth()) {
        ImornText(
            text = stringResource(R.string.template_blank),
            style = ImornTheme.typography.titleMedium,
        )
        ImornBodyText(text = stringResource(R.string.template_blank_body))
    }
    ImornTextField(
        value = query,
        onValueChange = { query = it },
        label = stringResource(R.string.template_search),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm),
    ) {
        ImornFilterChip(
            text = stringResource(R.string.template_category_all),
            selected = category == null,
            onClick = { category = null },
        )
        TemplateCategory.entries.forEach { entry ->
            ImornFilterChip(
                text = entry.label(),
                selected = category == entry,
                onClick = { category = entry },
            )
        }
    }

    val filtered = remember(state.templates, state.suggestedTemplateIds, query, category, language) {
        state.templates.inCategory(category).matching(query, language)
    }
    val browsingAll = query.isBlank() && category == null
    when {
        filtered.isEmpty() -> ImornBodyText(text = stringResource(R.string.template_none))

        browsingAll -> {
            val suggested = filtered.filter { it.id in state.suggestedTemplateIds }
            val rest = filtered.filterNot { it.id in state.suggestedTemplateIds }
            if (suggested.isNotEmpty()) {
                SectionHeading(stringResource(R.string.template_suggested))
                suggested.forEach { TemplateCard(it, language, onChoose) }
                if (rest.isNotEmpty()) SectionHeading(stringResource(R.string.template_all))
            }
            rest.groupedByCategory().forEach { (group, templates) ->
                SectionHeading(group.label())
                templates.forEach { TemplateCard(it, language, onChoose) }
            }
        }

        else -> filtered.forEach { TemplateCard(it, language, onChoose) }
    }
    ImornText(
        text = stringResource(R.string.template_gallery_note),
        style = ImornTheme.typography.bodySmall,
        color = ImornTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun TemplateCard(template: Template, language: String, onChoose: (Template) -> Unit) {
    ImornChoiceCard(
        selected = false,
        onClick = { onChoose(template) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        ImornText(text = template.title.text(language), style = ImornTheme.typography.titleMedium)
        val description = template.description.text(language)
        if (description.isNotBlank()) ImornBodyText(text = description)
        ImornText(
            text = template.category.label(),
            style = ImornTheme.typography.labelMedium,
            color = ImornTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun TemplateCategory.label(): String = stringResource(
    when (this) {
        TemplateCategory.Home -> R.string.template_category_home
        TemplateCategory.Family -> R.string.template_category_family
        TemplateCategory.WorkStudy -> R.string.template_category_work_study
        TemplateCategory.Health -> R.string.template_category_health
        TemplateCategory.LifeEvents -> R.string.template_category_life_events
        TemplateCategory.FinanceAdmin -> R.string.template_category_finance_admin
    },
)

@Composable
private fun TemplateIntro(template: Template, language: String) {
    val helps = template.helps.text(language)
    val note = template.note?.text(language)
    if (helps.isNotBlank() || !note.isNullOrBlank()) {
        ImornCard(modifier = Modifier.fillMaxWidth()) {
            if (helps.isNotBlank()) {
                ImornText(
                    text = stringResource(R.string.template_helps),
                    style = ImornTheme.typography.labelLarge,
                    color = ImornTheme.colors.onSurfaceVariant,
                )
                ImornBodyText(text = helps)
            }
            if (!note.isNullOrBlank()) ImornBodyText(text = note)
        }
    }
}

@Composable
private fun BlankStage(
    state: NewChapterUiState,
    onBack: () -> Unit,
    onCreateBlank: (String) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    Heading(stringResource(R.string.template_blank))
    ImornBodyText(text = stringResource(R.string.template_blank_help))
    ImornTextField(
        value = title,
        onValueChange = { title = it },
        label = stringResource(R.string.template_blank_name),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    ImornButton(
        text = stringResource(R.string.template_blank_create),
        onClick = { onCreateBlank(title) },
        enabled = title.isNotBlank() && !state.saving,
        modifier = Modifier.fillMaxWidth(),
    )
    ImornTextButton(text = stringResource(R.string.new_chapter_back), onClick = onBack)
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
    TemplateIntro(template, language)
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
    TemplateIntro(template, language)
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
    description = both(
        "Planera flytten från uppsägning till första natten.",
        "Plan the move from notice to first night.",
    ),
    helps = both(
        "Håll koll på papper, flyttfirma och allt som ska stängas av.",
        "Keep track of paperwork, movers and what to switch off.",
    ),
    category = TemplateCategory.Home,
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
            onStartBlank = {},
            onCreateBlank = {},
            onAnswer = {},
            onBack = {},
            onSetAnchor = { _, _ -> },
            onCreate = {},
            contentPadding = PaddingValues(),
        )
    }
}

private val previewState = NewChapterUiState(
    templates = listOf(previewTemplate),
    suggestedTemplateIds = setOf("move"),
    today = PreviewData.TODAY,
)

@Preview(showBackground = true)
@Composable
private fun NewChapterTemplatePreview() {
    PreviewScreen(previewState)
}

@Preview(showBackground = true)
@Composable
private fun NewChapterBlankPreview() {
    PreviewScreen(previewState.copy(blank = true))
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
