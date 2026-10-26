package se.imorn.feature.timeline

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.model.Chapter
import se.imorn.ui.Capture
import se.imorn.ui.PreviewData

@RunWith(RobolectricTestRunner::class)
class ChapterScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private fun show(
        view: ChapterView,
        onOpenStep: (String) -> Unit = {},
        onShow: (ChapterView) -> Unit = {},
        onOpenEntry: (String) -> Unit = {},
        onAddNote: (String) -> Unit = {},
        capture: Capture = Capture({}, {}, {}),
        chapter: Chapter = PreviewData.moving,
    ) {
        compose.setContent {
            ImornTheme {
                ChapterScreen(
                    state = ChapterUiState(
                        chapter,
                        PreviewData.TODAY,
                        view,
                        PreviewData.kept.map(::KeptEntry),
                    ),
                    chapter = chapter,
                    capture = capture,
                    onShow = onShow,
                    onOpenStep = onOpenStep,
                    onOpenEntry = onOpenEntry,
                    onSetAnchor = { _, _ -> },
                    onAddStep = {},
                    onAddNote = onAddNote,
                    onSetState = {},
                    onDelete = {},
                    contentPadding = PaddingValues(),
                )
            }
        }
    }

    @Test
    fun aBlankChapterInvitesTheFirstStep() {
        show(ChapterView.List, chapter = PreviewData.moving.copy(steps = emptyList(), phases = emptyList()))
        compose.onNodeWithText("No steps yet. Add the first thing you need to do.").assertExists()
    }

    @Test
    fun theListGroupsWhatWaitsByPhase() {
        show(ChapterView.List)
        compose.onNodeWithText("Paperwork").assertExists()
        compose.onNodeWithText("Packing").assertExists()
        // Done steps are not in the list, only on the timeline.
        compose.onNodeWithText("Book movers or a trailer").assertDoesNotExist()
    }

    @Test
    fun theTimelineShowsWhatWasDoneBelowTheNowLine() {
        show(ChapterView.Timeline)
        compose.onNodeWithText("Now").assertExists()
        compose.onNodeWithText("Book movers or a trailer").assertExists()
    }

    @Test
    fun aStepOpensFromTheList() {
        val opened = mutableListOf<String>()
        show(ChapterView.List, onOpenStep = { opened += it })
        // Paperwork starts closed (the next step is in Packing): open it, then the step.
        compose.onNodeWithText("Paperwork").performScrollTo().performClick()
        compose.onNodeWithText("Report your move").performScrollTo().performClick()
        assertEquals(listOf("address-change"), opened)
    }

    @Test
    fun theViewsSwitch() {
        val shown = mutableListOf<ChapterView>()
        show(ChapterView.List, onShow = { shown += it })
        compose.onNodeWithText("Timeline").performClick()
        assertEquals(listOf(ChapterView.Timeline), shown)
    }

    @Test
    fun theTimelineTellsTheStoryWithWhatWasKept() {
        val opened = mutableListOf<String>()
        show(ChapterView.Timeline, onOpenEntry = { opened += it })
        compose.onNodeWithContentDescription("Photo: Book movers or a trailer").assertExists()
        compose.onNodeWithText("The new kitchen gets the morning sun.").performScrollTo().performClick()
        assertEquals(listOf("preview-note"), opened)
        // Papers are private: never part of the story.
        compose.onNodeWithText("Lease for Norra Kungsgatan 4").assertDoesNotExist()
    }

    @Test
    fun papersHaveTheirOwnViewAndArePrivate() {
        show(ChapterView.Papers)
        compose.onNodeWithText("Lease for Norra Kungsgatan 4").assertExists()
        compose.onNodeWithText("Only on this phone").assertExists()
    }

    @Test
    fun theQuickAddTakesAPhoto() {
        var photos = 0
        show(ChapterView.List, capture = Capture(takePhoto = { photos++ }, choosePhoto = {}, choosePaper = {}))
        compose.onNodeWithContentDescription("Add to the chapter").performClick()
        compose.onNodeWithText("Take a photo").performClick()
        assertEquals(1, photos)
    }

    @Test
    fun theQuickAddWritesANote() {
        val notes = mutableListOf<String>()
        show(ChapterView.List, onAddNote = { notes += it })
        compose.onNodeWithContentDescription("Add to the chapter").performClick()
        compose.onNodeWithText("Write a note").performClick()
        compose.onNodeWithText("What do you want to remember?").performTextInput("Keys in the blue box")
        compose.onNodeWithText("Save").performClick()
        assertEquals(listOf("Keys in the blue box"), notes)
    }

    @Test
    fun eachSubflowShowsItsProgressAndWhatIsNext() {
        show(ChapterView.List)
        // Plan: both steps done.
        compose.onNodeWithText("All 2 done").assertExists()
        // Paperwork is closed, so it says what is next instead of listing it.
        compose.onNodeWithText("Next: Report your move", substring = true).assertExists()
        compose.onNodeWithText("Paperwork").performScrollTo().performClick()
        compose.onNodeWithText("Next: Report your move", substring = true).assertDoesNotExist()
    }
}
