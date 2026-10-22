package se.imorn

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import se.imorn.feature.chapters.navigation.NewChapterRoute
import se.imorn.feature.chapters.navigation.chaptersEntries
import se.imorn.feature.onboarding.navigation.OnboardingRoute
import se.imorn.feature.onboarding.navigation.onboardingEntry
import se.imorn.feature.timeline.navigation.ChapterRoute
import se.imorn.feature.timeline.navigation.StepRoute
import se.imorn.feature.timeline.navigation.timelineEntries
import se.imorn.feature.today.navigation.todayEntry
import se.imorn.navigation.Navigator
import se.imorn.navigation.TopLevelDestination
import se.imorn.navigation.rememberNavigationState
import se.imorn.navigation.toEntries

/**
 * The app shell: the welcome until it is done, then Imorn's single tab (Imorgon). Each entry brings its own routes
 * and ViewModel from Metro, scoped to the entry. Adding a tab means adding a [TopLevelDestination] and a feature's
 * `…Entry` function here.
 */
@Composable
fun ImornApp(onboarded: Boolean) {
    if (!onboarded) {
        Welcome()
        return
    }
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

/**
 * The welcome at first start, on its own without tabs. It marks itself done in `SettingsStore`, and [ImornApp] then
 * shows Imorgon instead; the system back gesture leaves the app as from any start screen.
 */
@Composable
private fun Welcome() {
    val state = rememberNavigationState(startRoute = OnboardingRoute, topLevelRoutes = setOf(OnboardingRoute))
    val entries = state.toEntries(
        entryProvider<NavKey> {
            onboardingEntry(contentPadding = PaddingValues())
        },
    )
    NavDisplay(entries = entries, onBack = {})
}
