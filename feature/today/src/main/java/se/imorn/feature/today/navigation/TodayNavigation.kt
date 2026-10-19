package se.imorn.feature.today.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.serialization.Serializable
import se.imorn.coach.AssistantRequest
import se.imorn.feature.today.R
import se.imorn.feature.today.TodayScreen
import se.imorn.feature.today.TodayViewModel

/** The Today tab: what to do next. */
@Serializable data object TodayRoute : NavKey

/**
 * Today's entry, with its own [TodayViewModel] scoped to the entry. The assistant request is built here, where the
 * localized strings and the user's language are available, and handed to the stateless screen's callbacks.
 */
fun EntryProviderScope<NavKey>.todayEntry(contentPadding: PaddingValues) {
    entry<TodayRoute> {
        val viewModel = metroViewModel<TodayViewModel>()
        // Read the model's state on resume, never in the background.
        LifecycleResumeEffect(viewModel) {
            viewModel.checkAssistant()
            onPauseOrDispose { }
        }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val locale = LocalConfiguration.current.locales[0]
        val goal = stringResource(R.string.project_goal)
        val nextStep = stringResource(R.string.today_next_step)
        TodayScreen(
            state = state,
            onAskAssistant = {
                viewModel.askAssistant(
                    AssistantRequest(
                        language = locale.getDisplayLanguage(locale),
                        goal = goal,
                        facts = listOf(nextStep),
                    ),
                )
            },
            onDownloadAssistant = viewModel::downloadAssistant,
            contentPadding = contentPadding,
        )
    }
}
