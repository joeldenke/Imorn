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
import se.imorn.feature.timeline.navigation.EntryRoute
import java.time.LocalDate

/** Something kept, as its screen shows it. [kept] is null once it has been deleted, and the screen closes. */
data class EntryUiState(
    val chapterTitle: String,
    val kept: KeptEntry?,
    /** The title of the step it was kept with, if any. */
    val stepTitle: String? = null,
    val today: LocalDate,
)

/** A photo, a note or a paper on its own screen, and deleting it (its file with it). */
@AssistedInject
class EntryViewModel(
    @Assisted private val route: EntryRoute,
    private val chapters: ChapterRepository,
) : ViewModel() {
    private val today = MutableStateFlow(LocalDate.now())

    /** Null until read. */
    val state: StateFlow<EntryUiState?> = combine(
        chapters.chapter(route.chapterId),
        chapters.entry(route.entryId),
        today,
    ) { chapter, entry, day ->
        EntryUiState(
            chapterTitle = chapter?.title.orEmpty(),
            kept = entry?.let { chapters.kept(it) },
            stepTitle = entry?.stepId?.let { id -> chapter?.steps?.firstOrNull { it.id == id }?.title },
            today = day,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    fun refresh() {
        today.value = LocalDate.now()
    }

    fun delete() {
        viewModelScope.launch {
            val entry = chapters.entry(route.entryId).first() ?: return@launch
            chapters.delete(entry)
        }
    }

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(@Assisted route: EntryRoute): EntryViewModel
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
