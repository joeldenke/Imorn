package se.imorn.feature.today

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.imorn.coach.AssistantAvailability
import se.imorn.designsystem.theme.ImornTheme

@RunWith(RobolectricTestRunner::class)
class TodayScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun showsNextStepAndAssistantMessage() {
        compose.setContent {
            ImornTheme {
                TodayScreen(
                    state = TodayUiState(
                        availability = AssistantAvailability.Available,
                        message = "Book the van first, it is the longest lead time.",
                    ),
                    onAskAssistant = {},
                    onDownloadAssistant = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        compose.onNodeWithText("Book the moving van").assertIsDisplayed()
        compose.onNodeWithText("Book the van first, it is the longest lead time.").assertIsDisplayed()
        compose.onNodeWithText("Explain the next step").assertIsDisplayed()
    }

    @Test
    fun offersDownloadWhenTheModelIsNotThereYet() {
        compose.setContent {
            ImornTheme {
                TodayScreen(
                    state = TodayUiState(availability = AssistantAvailability.Downloadable),
                    onAskAssistant = {},
                    onDownloadAssistant = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        compose.onNodeWithText("Download the assistant").assertIsDisplayed()
    }
}
