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
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.imorn.data.BundledContent
import se.imorn.database.ChapterRepository
import se.imorn.model.Chapter
import se.imorn.model.ChapterState
import se.imorn.model.Entry
import se.imorn.model.EntryKind
import se.imorn.model.Step
import se.imorn.model.Today
import se.imorn.model.complete
import se.imorn.model.withAnchor
import se.imorn.model.withOwnStep
import se.imorn.model.withState
import java.time.LocalDate
import java.util.UUID

/** How the chapter is shown: the checklist by phase, the timeline around the now-line, or the private papers. */
enum class ChapterView { List, Timeline, Papers }

/** A chapter as its screen shows it. [chapter] is null once it has been deleted, and the screen closes. */
data class ChapterUiState(
    val chapter: Chapter?,
    val today: LocalDate,
    val view: ChapterView = ChapterView.List,
    /** Photos, notes and papers kept in the chapter, latest first. */
    val kept: List<KeptEntry> = emptyList(),
    /** The icon of the template the chapter was made from, for its mark; null for an own chapter. */
    val icon: String? = null,
)

/**
 * One chapter: its plan as a list or a timeline, its story and papers, its anchor dates, the user's own steps, and
 * resting, closing or deleting it. Every change to the plan is a function from `:core:model` and one save; photos
 * and papers are copied into the app's private storage by [ChapterRepository.keep].
 */
@AssistedInject
class ChapterViewModel(
    @Assisted private val chapterId: String,
    private val chapters: ChapterRepository,
    /** The templates, for the chapter's icon. */
    private val content: BundledContent,
    /** The day it is on the phone, so the plan follows the phone's own time zone. */
    private val today: Today,
) : ViewModel() {
    private val currentDay = MutableStateFlow(today.now())
    private val view = MutableStateFlow(ChapterView.List)
    private val templateIcons = flow {
        emit(content.pack().templates.mapNotNull { template -> template.icon?.let { template.id to it } }.toMap())
    }.onStart { emit(emptyMap()) }

    /** Null until the chapter is read. */
    val state: StateFlow<ChapterUiState?> = combine(
        chapters.chapter(chapterId),
        chapters.entries(chapterId).map { entries -> entries.map { chapters.kept(it) } },
        currentDay,
        view,
        templateIcons,
    ) { chapter, kept, day, view, icons ->
        ChapterUiState(chapter, day, view, kept, icon = chapter?.templateId?.let(icons::get))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    fun refresh() {
        currentDay.value = today.now()
    }

    fun show(view: ChapterView) {
        this.view.value = view
    }

    fun done(stepId: String) = change { it.complete(stepId, currentDay.value) }

    fun setAnchor(id: String, date: LocalDate?) = change { it.withAnchor(id, date) }

    fun addStep(title: String) = change { it.withOwnStep(Step(id = "own-${UUID.randomUUID()}", title = title)) }

    fun setState(state: ChapterState) = change { it.withState(state) }

    fun addNote(text: String) {
        viewModelScope.launch { chapters.save(newEntry(EntryKind.Note).copy(text = text.trim())) }
    }

    /** Keeps a photo from the camera or the picker, on its own in the story. */
    fun keepPhoto(source: Uri) = keep(newEntry(EntryKind.Photo), source)

    /** Keeps a paper, which shows under *Papers* and is private. */
    fun keepPaper(source: Uri) {
        view.value = ChapterView.Papers
        keep(newEntry(EntryKind.Paper), source)
    }

    private fun newEntry(kind: EntryKind) = Entry(UUID.randomUUID().toString(), chapterId, kind, currentDay.value)

    private fun keep(entry: Entry, source: Uri) {
        viewModelScope.launch { chapters.keepIfReadable(entry, source) }
    }

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
