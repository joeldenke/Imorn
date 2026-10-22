package se.imorn.feature.timeline

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.imorn.content.ENGLISH
import se.imorn.content.Link
import se.imorn.content.LinkKind
import se.imorn.content.SWEDISH
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.model.StepStatus
import se.imorn.ui.PreviewData
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class StepScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val step = PreviewData.moving.steps.first { it.id == "address-change" }
    private val link = Link(
        id = "skatteverket.flytt",
        org = "Skatteverket",
        kind = LinkKind.Authority,
        url = "https://www.skatteverket.se/",
        title = mapOf(SWEDISH to "Gör flyttanmälan", ENGLISH to "Report your move"),
    )

    private fun show(state: StepUiState, onDone: () -> Unit = {}, onReopen: () -> Unit = {}) {
        compose.setContent {
            ImornTheme {
                StepScreen(
                    state = state,
                    language = ENGLISH,
                    onDone = onDone,
                    onReopen = onReopen,
                    onTomorrow = {},
                    onSetDate = {},
                    onNotApplicable = {},
                    onSaveNote = {},
                    contentPadding = PaddingValues(),
                )
            }
        }
    }

    private val open = StepUiState(
        chapterTitle = PreviewData.moving.title,
        step = step,
        date = LocalDate.of(2027, 2, 15),
        deadline = LocalDate.of(2027, 3, 8),
        links = listOf(link),
        today = PreviewData.TODAY,
    )

    @Test
    fun aLinkShowsWhereItGoes() {
        show(open)
        compose.onNodeWithText("Skatteverket · skatteverket.se").assertExists()
    }

    @Test
    fun anOpenStepCanBeDone() {
        var done = false
        show(open, onDone = { done = true })
        compose.onNodeWithText("Done").performClick()
        assertEquals(true, done)
    }

    @Test
    fun aDoneStepCanBeReopened() {
        var reopened = false
        val done = open.copy(step = step.copy(status = StepStatus.Done, doneOn = PreviewData.TODAY))
        show(done, onReopen = { reopened = true })
        compose.onNodeWithText("Not done after all").performClick()
        assertEquals(true, reopened)
    }
}
