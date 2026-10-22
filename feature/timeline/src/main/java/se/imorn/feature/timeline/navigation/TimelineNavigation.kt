package se.imorn.feature.timeline.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.serialization.Serializable
import se.imorn.feature.timeline.ChapterScreen
import se.imorn.feature.timeline.ChapterViewModel
import se.imorn.feature.timeline.StepScreen
import se.imorn.feature.timeline.StepViewModel

/** A chapter: its plan as a list or a timeline around the now-line. */
@Serializable data class ChapterRoute(val chapterId: String) : NavKey

/** One step in a chapter. */
@Serializable data class StepRoute(val chapterId: String, val stepId: String) : NavKey

/**
 * The timeline feature's entries. Each gets its ViewModel with the route's ids. [onOpenStep] and [onBack] come from
 * the app's navigator; a deleted chapter or step closes its screen.
 */
fun EntryProviderScope<NavKey>.timelineEntries(
    contentPadding: PaddingValues,
    onOpenStep: (chapterId: String, stepId: String) -> Unit,
    onBack: () -> Unit,
) {
    entry<ChapterRoute> { route ->
        val viewModel = assistedMetroViewModel<ChapterViewModel, ChapterViewModel.Factory> { create(route.chapterId) }
        LifecycleResumeEffect(viewModel) {
            viewModel.refresh()
            onPauseOrDispose { }
        }
        val state = viewModel.state.collectAsStateWithLifecycle().value ?: return@entry
        val chapter = state.chapter
        LaunchedEffect(chapter == null) {
            if (chapter == null) onBack()
        }
        if (chapter == null) return@entry
        ChapterScreen(
            state = state,
            chapter = chapter,
            onShow = viewModel::show,
            onOpenStep = { onOpenStep(route.chapterId, it) },
            onSetAnchor = viewModel::setAnchor,
            onAddStep = viewModel::addStep,
            onSetState = viewModel::setState,
            onDelete = viewModel::delete,
            contentPadding = contentPadding,
        )
    }
    entry<StepRoute> { route ->
        val viewModel = assistedMetroViewModel<StepViewModel, StepViewModel.Factory> {
            create(route.chapterId, route.stepId)
        }
        LifecycleResumeEffect(viewModel) {
            viewModel.refresh()
            onPauseOrDispose { }
        }
        val state = viewModel.state.collectAsStateWithLifecycle().value ?: return@entry
        StepScreen(
            state = state,
            language = LocalConfiguration.current.locales[0].language,
            onDone = viewModel::done,
            onReopen = viewModel::reopen,
            onTomorrow = viewModel::tomorrow,
            onSetDate = viewModel::setDate,
            onNotApplicable = viewModel::notApplicable,
            onSaveNote = viewModel::saveNote,
            contentPadding = contentPadding,
        )
    }
}
