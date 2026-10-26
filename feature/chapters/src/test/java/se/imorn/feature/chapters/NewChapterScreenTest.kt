package se.imorn.feature.chapters

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.imorn.content.Answer
import se.imorn.content.ENGLISH
import se.imorn.content.Option
import se.imorn.content.Question
import se.imorn.content.QuestionType
import se.imorn.content.SWEDISH
import se.imorn.content.Template
import se.imorn.content.TemplateAnchor
import se.imorn.content.TemplateCategory
import se.imorn.designsystem.theme.ImornTheme
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class NewChapterScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private fun both(text: String) = mapOf(SWEDISH to text, ENGLISH to text)

    private val template = Template(
        id = "move",
        version = 1,
        title = both("Moving"),
        summary = both("Plan the move."),
        helps = both("Keep track of the paperwork."),
        category = TemplateCategory.Home,
        anchors = listOf(TemplateAnchor("moveDay", both("Moving day"))),
        questions = listOf(
            Question("housing", both("From what?"), options = listOf(Option("rental", both("Rented flat")))),
            Question("kids", both("Kids changing school?"), type = QuestionType.YesNo),
        ),
    )

    private fun show(
        state: NewChapterUiState,
        onAnswer: (Answer?) -> Unit = {},
        onTemplate: (Template) -> Unit = {},
        onStartBlank: () -> Unit = {},
        onCreateBlank: (String) -> Unit = {},
    ) {
        compose.setContent {
            ImornTheme {
                NewChapterScreen(
                    state = state,
                    language = ENGLISH,
                    onChooseTemplate = onTemplate,
                    onStartBlank = onStartBlank,
                    onCreateBlank = onCreateBlank,
                    onAnswer = onAnswer,
                    onBack = {},
                    onSetAnchor = { _, _ -> },
                    onCreate = {},
                    contentPadding = PaddingValues(),
                )
            }
        }
    }

    private val start = NewChapterUiState(templates = listOf(template), today = LocalDate.of(2027, 2, 10))

    @Test
    fun choosingATemplateStartsThePlan() {
        var chosen: Template? = null
        show(start, onTemplate = { chosen = it })
        compose.onNodeWithText("Moving").performScrollTo().performClick()
        assertEquals("move", chosen?.id)
    }

    @Test
    fun searchingNarrowsTheGallery() {
        val baby = template.copy(
            id = "baby",
            title = both("A baby"),
            summary = both("The first year."),
            category = TemplateCategory.Family,
        )
        show(start.copy(templates = listOf(template, baby), suggestedTemplateIds = emptySet()))
        compose.onNode(hasSetTextAction()).performTextInput("baby")
        compose.onNodeWithText("A baby").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Moving").assertDoesNotExist()
    }

    @Test
    fun aBlankProjectStartsFromTheGallery() {
        var started = false
        show(start, onStartBlank = { started = true })
        compose.onNodeWithText("Start from a blank project").performClick()
        assertEquals(true, started)
    }

    @Test
    fun aBlankProjectAsksForAName() {
        var created: String? = null
        show(start.copy(blank = true), onCreateBlank = { created = it })
        compose.onNode(hasSetTextAction()).performTextInput("My garden")
        compose.onNodeWithText("Create project").performClick()
        assertEquals("My garden", created)
    }

    @Test
    fun aYesOrNoQuestionOffersYesNoAndSkip() {
        val answers = mutableListOf<Answer?>()
        show(start.copy(template = template, question = 1), onAnswer = { answers += it })
        compose.onNodeWithText("Question 2 of 2").assertIsDisplayed()
        compose.onNodeWithText("Yes").performClick()
        compose.onNodeWithText("Skip").performClick()
        assertEquals(listOf(Answer.YesNo(true), null), answers)
    }

    @Test
    fun theDatesStageSaysAnUnknownDateIsFine() {
        show(start.copy(template = template, question = template.questions.size))
        compose.onNodeWithText("Not known yet").assertIsDisplayed()
        compose.onNodeWithText("Make the plan").assertExists()
    }
}
