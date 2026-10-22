package se.imorn.feature.chapters.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.serialization.Serializable
import se.imorn.feature.chapters.NewChapterScreen
import se.imorn.feature.chapters.NewChapterViewModel

/** Starting a new chapter from a template. Opens on top of the current tab. */
@Serializable data object NewChapterRoute : NavKey

/**
 * The chapters feature's entries. [onBack] leaves the flow (the system back gesture does the same); [onCreated]
 * runs once the chapter is saved, and the app decides where to go next.
 */
fun EntryProviderScope<NavKey>.chaptersEntries(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onCreated: () -> Unit,
) {
    entry<NewChapterRoute> {
        val viewModel = metroViewModel<NewChapterViewModel>()
        val state = viewModel.state.collectAsStateWithLifecycle().value ?: return@entry
        // The template's texts follow the app's language; Swedish and English are in every template.
        val language = LocalConfiguration.current.locales[0].language
        LaunchedEffect(state.created) {
            if (state.created) onCreated()
        }
        NewChapterScreen(
            state = state,
            language = language,
            onChooseTemplate = viewModel::chooseTemplate,
            onAnswer = viewModel::answer,
            onBack = { if (state.template == null) onBack() else viewModel.back() },
            onSetAnchor = viewModel::setAnchor,
            onCreate = { viewModel.create(language) },
            contentPadding = contentPadding,
        )
    }
}
