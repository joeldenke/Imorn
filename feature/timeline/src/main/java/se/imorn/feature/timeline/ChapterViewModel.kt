package se.imorn.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.imorn.database.ChapterRepository
import se.imorn.model.Chapter
import se.imorn.model.ChapterState
import se.imorn.model.Step
import se.imorn.model.complete
import se.imorn.model.withAnchor
import se.imorn.model.withOwnStep
import se.imorn.model.withState
import java.time.LocalDate
import java.util.UUID

/** How the chapter is shown: the checklist by phase, or the timeline around the now-line. */
enum class ChapterView { List, Timeline }

/** A chapter as its screen shows it. [chapter] is null once it has been deleted, and the screen closes. */
data class ChapterUiState(val chapter: Chapter?, val today: LocalDate, val view: ChapterView = ChapterView.List)

/**
 * One chapter: its plan as a list or a timeline, its anchor dates, the user's own steps, and resting, closing or
 * deleting it. Every change is a function from `:core:model` and one save.
 */
@AssistedInject
class ChapterViewModel(
    @Assisted private val chapterId: String,
    private val chapters: ChapterRepository,
) : ViewModel() {
    private val today = MutableStateFlow(LocalDate.now())
    private val view = MutableStateFlow(ChapterView.List)

    /** Null until the chapter is read. */
    val state: StateFlow<ChapterUiState?> = combine(chapters.chapter(chapterId), today, view) { chapter, day, view ->
        ChapterUiState(chapter, day, view)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    fun refresh() {
        today.value = LocalDate.now()
    }

    fun show(view: ChapterView) {
        this.view.value = view
    }

    fun done(stepId: String) = change { it.complete(stepId, today.value) }

    fun setAnchor(id: String, date: LocalDate?) = change { it.withAnchor(id, date) }

    fun addStep(title: String) = change { it.withOwnStep(Step(id = "own-${UUID.randomUUID()}", title = title)) }

    fun setState(state: ChapterState) = change { it.withState(state) }

    fun delete() {
        viewModelScope.launch { chapters.delete(chapterId) }
    }

    private fun change(change: (Chapter) -> Chapter) {
        viewModelScope.launch {
            val chapter = chapters.chapter(chapterId).first() ?: return@launch
            chapters.save(change(chapter))
        }
    }

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(@Assisted chapterId: String): ChapterViewModel
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
