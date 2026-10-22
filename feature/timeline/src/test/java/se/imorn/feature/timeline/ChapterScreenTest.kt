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
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.ui.PreviewData

@RunWith(RobolectricTestRunner::class)
class ChapterScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private fun show(view: ChapterView, onOpenStep: (String) -> Unit = {}, onShow: (ChapterView) -> Unit = {}) {
        compose.setContent {
            ImornTheme {
                ChapterScreen(
                    state = ChapterUiState(PreviewData.moving, PreviewData.TODAY, view),
                    chapter = PreviewData.moving,
                    onShow = onShow,
                    onOpenStep = onOpenStep,
                    onSetAnchor = { _, _ -> },
                    onAddStep = {},
                    onSetState = {},
                    onDelete = {},
                    contentPadding = PaddingValues(),
                )
            }
        }
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
        compose.onNodeWithText("Report your move").performClick()
        assertEquals(listOf("address-change"), opened)
    }

    @Test
    fun theViewsSwitch() {
        val shown = mutableListOf<ChapterView>()
        show(ChapterView.List, onShow = { shown += it })
        compose.onNodeWithText("Timeline").performClick()
        assertEquals(listOf(ChapterView.Timeline), shown)
    }
}
