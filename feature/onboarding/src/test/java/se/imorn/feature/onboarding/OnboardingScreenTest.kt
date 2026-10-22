package se.imorn.feature.onboarding

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
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

    private fun show(
        state: OnboardingUiState = OnboardingUiState(),
        onToggle: (LifeStage) -> Unit = {},
        onStart: () -> Unit = {},
        onSkip: () -> Unit = {},
    ) {
        compose.setContent {
            ImornTheme {
                OnboardingScreen(
                    state = state,
                    onToggle = onToggle,
                    onStart = onStart,
                    onSkip = onSkip,
                    contentPadding = PaddingValues(),
                )
            }
        }
    }

    @Test
    fun theWelcomeLeadsThroughWhyToTheLifeStages() {
        val toggled = mutableListOf<LifeStage>()
        var started = false
        show(
            state = OnboardingUiState(setOf(LifeStage.BuildingUp)),
            onToggle = { toggled += it },
            onStart = { started = true },
        )
        compose.onNodeWithText("Life's big projects, one step at a time").assertIsDisplayed()
        compose.onNodeWithText("Continue").performClick()
        compose.onNodeWithText("Never more than three things a day").assertExists()
        compose.onNodeWithText("Continue").performClick()
        compose.onNodeWithText("Building up").assertIsSelected()
        compose.onNodeWithText("New in Sweden").performScrollTo().performClick()
        compose.onNodeWithText("Get started").performClick()
        assertEquals(listOf(LifeStage.NewInSweden), toggled)
        assertEquals(true, started)
    }

    @Test
    fun skipIsAlwaysThere() {
        var skipped = false
        show(onSkip = { skipped = true })
        compose.onNodeWithText("Skip").performClick()
        assertEquals(true, skipped)
    }

    @Test
    fun noNameIsAsked() {
        show()
        compose.onNodeWithText("What should we call you?", substring = true).assertDoesNotExist()
    }
}
