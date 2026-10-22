package se.imorn.feature.today.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.serialization.Serializable
import se.imorn.coach.AssistantRequest
import se.imorn.feature.today.TodayScreen
import se.imorn.feature.today.TodayViewModel

/** The start tab, *Imorgon*: what to do next. */
@Serializable data object TodayRoute : NavKey

/**
 * Today's entry, with its own [TodayViewModel] scoped to the entry. Starting a chapter and opening a step or a
 * chapter open other features, so those callbacks come from the app. The assistant request is built here, where
 * the user's language is known.
 */
fun EntryProviderScope<NavKey>.todayEntry(
    contentPadding: PaddingValues,
    onNewChapter: () -> Unit,
    onOpenStep: (chapterId: String, stepId: String) -> Unit,
    onOpenChapter: (chapterId: String) -> Unit,
) {
    entry<TodayRoute> {
        val viewModel = metroViewModel<TodayViewModel>()
        // Read "today" and the model's state on resume, never in the background.
        LifecycleResumeEffect(viewModel) {
            viewModel.refresh()
            onPauseOrDispose { }
        }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val locale = LocalConfiguration.current.locales[0]
        // Nothing to draw until the chapters are read, a moment at start.
        val current = state ?: return@entry
        TodayScreen(
            state = current,
            onDone = viewModel::done,
            onTomorrow = viewModel::tomorrow,
            onNewChapter = onNewChapter,
            onOpenStep = { onOpenStep(it.chapterId, it.step.id) },
            onOpenChapter = onOpenChapter,
            onAskAssistant = {
                val items = current.plan.items.map { it.planned }
                viewModel.askAssistant(
                    AssistantRequest(
                        language = locale.getDisplayLanguage(locale),
                        goal = items.firstOrNull()?.chapterTitle.orEmpty(),
                        facts = items.map { it.step.title },
                    ),
                )
            },
            onDownloadAssistant = viewModel::downloadAssistant,
            contentPadding = contentPadding,
        )
    }
}
