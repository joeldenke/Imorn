package se.imorn.feature.onboarding

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.model.LifeStage

@RunWith(RobolectricTestRunner::class)
class OnboardingScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun choosingAStageAndSkippingReachTheirCallbacks() {
        val toggled = mutableListOf<LifeStage>()
        var skipped = false
        compose.setContent {
            ImornTheme {
                OnboardingScreen(
                    state = OnboardingUiState(),
                    onNameChange = {},
                    onToggle = { toggled += it },
                    onStart = {},
                    onSkip = { skipped = true },
                    contentPadding = PaddingValues(),
                )
            }
        }
        compose.onNodeWithText("Welcome to Imorn").assertIsDisplayed()
        compose.onNodeWithText("New in Sweden").performScrollTo().performClick()
        compose.onNodeWithText("Skip for now").performScrollTo().performClick()
        assertEquals(listOf(LifeStage.NewInSweden), toggled)
        assertEquals(true, skipped)
    }
}
