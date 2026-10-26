package se.imorn.feature.timeline

import android.net.Uri
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.imorn.content.Link
import se.imorn.data.BundledContent
import se.imorn.database.ChapterRepository
import se.imorn.feature.timeline.navigation.StepRoute
import se.imorn.model.Chapter
import se.imorn.model.Entry
import se.imorn.model.EntryKind
import se.imorn.model.Step
import se.imorn.model.Today
import se.imorn.model.canMoveStep
import se.imorn.model.complete
import se.imorn.model.dateOf
import se.imorn.model.deadlineOf
import se.imorn.model.moveStep
import se.imorn.model.notApplicable
import se.imorn.model.reopen
import se.imorn.model.withStepDate
import se.imorn.model.withStepTitle
import se.imorn.model.withoutStep
import java.time.LocalDate
import java.util.UUID

/** One step, with its day, hard deadline and the links it refers to from the registry. Null once it is gone. */
data class StepUiState(
    val chapterTitle: String,
    val step: Step,
    val date: LocalDate?,
    val deadline: LocalDate?,
    val links: List<Link>,
    val today: LocalDate,
    /** The photos and notes kept with this step. */
    val kept: List<KeptEntry> = emptyList(),
    /** True when the step has a step before or after it in its own phase. */
    val canMoveEarlier: Boolean = false,
    val canMoveLater: Boolean = false,
)

/** The step detail: what, why, when and where, and the answers *Done*, *Tomorrow*, a date or *Doesn't apply*. */
@AssistedInject
class StepViewModel(
    @Assisted route: StepRoute,
    private val chapters: ChapterRepository,
    private val content: BundledContent,
    /** The day it is on the phone, so the plan follows the phone's own time zone. */
    private val today: Today,
) : ViewModel() {
    private val chapterId = route.chapterId
    private val stepId = route.stepId
    private val currentDay = MutableStateFlow(today.now())

    /** Null until read, and null again if the step or its chapter is deleted. */
    val state: StateFlow<StepUiState?> = combine(
        chapters.chapter(chapterId),
        flow { emit(content.pack()) },
        currentDay,
        chapters.entries(chapterId).map { entries -> entries.filter { it.stepId == stepId } },
    ) { chapter, pack, day, entries ->
        val step = chapter?.steps?.firstOrNull { it.id == stepId } ?: return@combine null
        StepUiState(
            chapterTitle = chapter.title,
            step = step,
            date = chapter.dateOf(step),
            deadline = chapter.deadlineOf(step),
            links = step.links.mapNotNull { pack.link(it.id) },
            today = day,
            kept = entries.sortedBy { it.on }.map { chapters.kept(it) },
            canMoveEarlier = chapter.canMoveStep(stepId, earlier = true),
            canMoveLater = chapter.canMoveStep(stepId, earlier = false),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    fun refresh() {
        currentDay.value = today.now()
    }

    fun done() = change { it.complete(stepId, currentDay.value) }

    fun reopen() = change { it.reopen(stepId) }

    fun tomorrow() = change { it.withStepDate(stepId, currentDay.value.plusDays(1)) }

    /** The user's own day, or null to count from the anchor again. */
    fun setDate(date: LocalDate?) = change { it.withStepDate(stepId, date) }

    fun notApplicable() = change { it.notApplicable(stepId) }

    /** The user's own name for the step; a blank name changes nothing. */
    fun rename(title: String) {
        if (title.isBlank()) return
        change { it.withStepTitle(stepId, title) }
    }

    fun moveEarlier() = change { it.moveStep(stepId, earlier = true) }

    fun moveLater() = change { it.moveStep(stepId, earlier = false) }

    /** Removes the step; what was kept with it stays in the chapter's story. */
    fun remove() = change { it.withoutStep(stepId) }

    fun saveNote(note: String) = change { chapter ->
        chapter.copy(
            steps = chapter.steps.map { if (it.id == stepId) it.copy(note = note.trim().ifEmpty { null }) else it },
        )
    }

    /** Keeps a photo with this step, such as the one its photo prompt asks for. */
    fun keepPhoto(source: Uri) {
        val photo = Entry(UUID.randomUUID().toString(), chapterId, EntryKind.Photo, currentDay.value, stepId = stepId)
        viewModelScope.launch { chapters.keepIfReadable(photo, source) }
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
