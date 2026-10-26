package se.imorn.feature.timeline

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
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

    private fun show(
        state: StepUiState,
        onDone: () -> Unit = {},
        onReopen: () -> Unit = {},
        onTakePhoto: () -> Unit = {},
        onRename: (String) -> Unit = {},
        onMoveEarlier: () -> Unit = {},
        onMoveLater: () -> Unit = {},
        onRemove: () -> Unit = {},
    ) {
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
                    onRename = onRename,
                    onMoveEarlier = onMoveEarlier,
                    onMoveLater = onMoveLater,
                    onRemove = onRemove,
                    onTakePhoto = onTakePhoto,
                    onOpenEntry = {},
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

    @Test
    fun aStepWithAPhotoPromptAsksForAPhotoOnceItIsDone() {
        var photos = 0
        val withPrompt = open.copy(step = step.copy(photoPrompt = "Photograph the meter readings"))
        show(withPrompt, onTakePhoto = { photos++ })
        compose.onNodeWithText("Photograph the meter readings").assertExists()
        compose.onNodeWithText("Done").performClick()
        compose.onNodeWithText("Done. Keep a photo?").assertExists()
        compose.onNodeWithText("Take a photo").performClick()
        assertEquals(1, photos)
        compose.onNodeWithText("Done. Keep a photo?").assertDoesNotExist()
    }

    @Test
    fun notNowClosesThePromptWithoutAPhoto() {
        var photos = 0
        show(open.copy(step = step.copy(photoPrompt = "Photograph the keys")), onTakePhoto = { photos++ })
        compose.onNodeWithText("Done").performClick()
        compose.onNodeWithText("Not now").performClick()
        compose.onNodeWithText("Done. Keep a photo?").assertDoesNotExist()
        assertEquals(0, photos)
    }

    @Test
    fun aStepCanBeRenamed() {
        val names = mutableListOf<String>()
        show(open, onRename = { names += it })
        compose.onNodeWithText("Change name").performScrollTo().performClick()
        compose.onNodeWithText("Name").performTextClearance()
        compose.onNodeWithText("Name").performTextInput("Tell the new landlord")
        compose.onNodeWithText("Save").performClick()
        assertEquals(listOf("Tell the new landlord"), names)
    }

    @Test
    fun aStepCanBeMovedAmongItsPeers() {
        var earlier = 0
        var later = 0
        show(
            open.copy(canMoveEarlier = true, canMoveLater = true),
            onMoveEarlier = { earlier++ },
            onMoveLater = { later++ },
        )
        compose.onNodeWithText("Move up").performScrollTo().performClick()
        compose.onNodeWithText("Move down").performScrollTo().performClick()
        assertEquals(1, earlier)
        assertEquals(1, later)
    }

    @Test
    fun aStepCanBeRemoved() {
        var removed = 0
        show(open, onRemove = { removed++ })
        compose.onNodeWithText("Remove the step").performScrollTo().performClick()
        compose.onNodeWithText("Remove").performClick()
        assertEquals(1, removed)
    }

    @Test
    fun aStepWithoutAPhotoPromptAsksNothing() {
        show(open)
        compose.onNodeWithText("Done").performClick()
        compose.onNodeWithText("Done. Keep a photo?").assertDoesNotExist()
    }
}
