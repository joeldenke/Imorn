package se.imorn

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import se.imorn.feature.chapters.navigation.NewChapterRoute
import se.imorn.feature.chapters.navigation.chaptersEntries
import se.imorn.feature.timeline.navigation.ChapterRoute
import se.imorn.feature.timeline.navigation.StepRoute
import se.imorn.feature.timeline.navigation.timelineEntries
import se.imorn.feature.today.navigation.todayEntry
import se.imorn.navigation.Navigator
import se.imorn.navigation.TopLevelDestination
import se.imorn.navigation.rememberNavigationState
import se.imorn.navigation.toEntries

/**
 * The app shell. Imorn starts with a single tab (Imorgon); each entry brings its own routes and ViewModel from Metro,
 * scoped to the entry. Adding a tab means adding a [TopLevelDestination] and a feature's `…Entry` function here.
 */
@Composable
fun ImornApp() {
    val navigationState = rememberNavigationState(
        startRoute = TopLevelDestination.start.route,
        topLevelRoutes = TopLevelDestination.routes,
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }
    val entries = navigationState.toEntries(
        entryProvider<NavKey> {
            val openStep = { chapterId: String, stepId: String -> navigator.navigate(StepRoute(chapterId, stepId)) }
            todayEntry(
                contentPadding = PaddingValues(),
                onNewChapter = { navigator.navigate(NewChapterRoute) },
                onOpenStep = openStep,
                onOpenChapter = { navigator.navigate(ChapterRoute(it)) },
            )
            timelineEntries(
                contentPadding = PaddingValues(),
                onOpenStep = openStep,
                onBack = navigator::goBack,
            )
            chaptersEntries(
                contentPadding = PaddingValues(),
                onBack = navigator::goBack,
                // Back to Imorgon, where the new chapter's first steps are.
                onCreated = navigator::goBack,
            )
        },
    )
    NavDisplay(
        entries = entries,
        // Popping an entry clears its ViewModel: leaving a screen without finishing discards it.
        onBack = navigator::goBack,
    )
}
