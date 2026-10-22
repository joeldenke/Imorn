package se.imorn.feature.today

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.model.PlannedStep
import se.imorn.model.tomorrowPlan
import se.imorn.ui.PreviewData

@RunWith(RobolectricTestRunner::class)
class TodayScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private fun show(
        hasChapters: Boolean = true,
        onDone: (PlannedStep) -> Unit = {},
        onTomorrow: (PlannedStep) -> Unit = {},
        onNewChapter: () -> Unit = {},
    ) {
        val chapters = if (hasChapters) listOf(PreviewData.moving) else emptyList()
        compose.setContent {
            ImornTheme {
                TodayScreen(
                    state = TodayUiState(
                        today = PreviewData.TODAY,
                        plan = tomorrowPlan(chapters, PreviewData.TODAY),
                        chapters = chapters,
                    ),
                    onDone = onDone,
                    onTomorrow = onTomorrow,
                    onNewChapter = onNewChapter,
                    onOpenStep = {},
                    onOpenChapter = {},
                    onAskAssistant = {},
                    onDownloadAssistant = {},
                    onTakePhoto = { _, _ -> },
                    contentPadding = PaddingValues(),
                )
            }
        }
    }

    @Test
    fun showsTheLateStepFirstAndTheNextOne() {
        show()
        compose.onNodeWithText("Get moving boxes, tape and a marker").assertIsDisplayed()
        compose.onNodeWithText("A little late").assertIsDisplayed()
        compose.onNodeWithText("Report your move").assertExists()
    }

    @Test
    fun doneAndTomorrowAnswerTheStepTheyBelongTo() {
        val done = mutableListOf<String>()
        val tomorrow = mutableListOf<String>()
        show(onDone = { done += it.step.id }, onTomorrow = { tomorrow += it.step.id })
        compose.onAllNodesWithText("Done").onFirst().performClick()
        compose.onAllNodesWithText("Tomorrow").onFirst().performClick()
        assertEquals(listOf("boxes"), done)
        assertEquals(listOf("boxes"), tomorrow)
    }

    @Test
    fun withoutChaptersItInvitesTheFirstOne() {
        var started = false
        show(hasChapters = false, onNewChapter = { started = true })
        compose.onNodeWithText("Nothing planned yet").assertIsDisplayed()
        compose.onNodeWithText("Start a chapter").performClick()
        assertEquals(true, started)
    }
}
