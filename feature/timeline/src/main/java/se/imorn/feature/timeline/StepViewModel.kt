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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.imorn.content.Link
import se.imorn.data.BundledContent
import se.imorn.feature.timeline.navigation.StepRoute
import se.imorn.database.ChapterRepository
import se.imorn.model.Chapter
import se.imorn.model.Step
import se.imorn.model.complete
import se.imorn.model.dateOf
import se.imorn.model.deadlineOf
import se.imorn.model.notApplicable
import se.imorn.model.reopen
import se.imorn.model.withStepDate
import java.time.LocalDate

/** One step, with its day, hard deadline and the links it refers to from the registry. Null once it is gone. */
data class StepUiState(
    val chapterTitle: String,
    val step: Step,
    val date: LocalDate?,
    val deadline: LocalDate?,
    val links: List<Link>,
    val today: LocalDate,
)

/** The step detail: what, why, when and where, and the answers *Done*, *Tomorrow*, a date or *Doesn't apply*. */
@AssistedInject
class StepViewModel(
    @Assisted route: StepRoute,
    private val chapters: ChapterRepository,
    private val content: BundledContent,
) : ViewModel() {
    private val chapterId = route.chapterId
    private val stepId = route.stepId
    private val today = MutableStateFlow(LocalDate.now())

    /** Null until read, and null again if the step or its chapter is deleted. */
    val state: StateFlow<StepUiState?> = combine(
        chapters.chapter(chapterId),
        flow { emit(content.pack()) },
        today,
    ) { chapter, pack, day ->
        val step = chapter?.steps?.firstOrNull { it.id == stepId } ?: return@combine null
        StepUiState(
            chapterTitle = chapter.title,
            step = step,
            date = chapter.dateOf(step),
            deadline = chapter.deadlineOf(step),
            links = step.links.mapNotNull { pack.link(it.id) },
            today = day,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    fun refresh() {
        today.value = LocalDate.now()
    }

    fun done() = change { it.complete(stepId, today.value) }

    fun reopen() = change { it.reopen(stepId) }

    fun tomorrow() = change { it.withStepDate(stepId, today.value.plusDays(1)) }

    /** The user's own day, or null to count from the anchor again. */
    fun setDate(date: LocalDate?) = change { it.withStepDate(stepId, date) }

    fun notApplicable() = change { it.notApplicable(stepId) }

    fun saveNote(note: String) = change { chapter ->
        chapter.copy(
            steps = chapter.steps.map { if (it.id == stepId) it.copy(note = note.trim().ifEmpty { null }) else it },
        )
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
        fun create(@Assisted route: StepRoute): StepViewModel
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
