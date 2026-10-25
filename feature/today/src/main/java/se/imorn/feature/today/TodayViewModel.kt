package se.imorn.feature.today

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.imorn.coach.AssistantAvailability
import se.imorn.coach.AssistantPrompt
import se.imorn.coach.AssistantRequest
import se.imorn.coach.LanguageModel
import se.imorn.data.BundledContent
import se.imorn.database.ChapterRepository
import se.imorn.model.Chapter
import se.imorn.model.Entry
import se.imorn.model.EntryKind
import se.imorn.model.Milestone
import se.imorn.model.PlannedStep
import se.imorn.model.Today
import se.imorn.model.TomorrowPlan
import se.imorn.model.complete
import se.imorn.model.nextMilestone
import se.imorn.model.tomorrowPlan
import se.imorn.model.withStepDate
import java.time.LocalDate
import java.util.UUID

/** The on-device assistant's part of the screen. */
data class AssistantUiState(
    val availability: AssistantAvailability = AssistantAvailability.Unknown,
    val writing: Boolean = false,
    val message: String? = null,
    val failed: Boolean = false,
)

/**
 * What *Imorgon* shows: the sky at this [hour] with the anchor coming up, at most three things to do, what is
 * getting close later, the chapters and the assistant.
 */
data class TodayUiState(
    val today: LocalDate,
    val plan: TomorrowPlan,
    /** Every chapter, for the quiet list under the plan. */
    val chapters: List<Chapter>,
    val assistant: AssistantUiState = AssistantUiState(),
    /** The hour on the phone, for the sky and the greeting only. */
    val hour: Int = Today.NOON,
    /** Each template's icon by template id, so a chapter shows the mark of the template it was made from. */
    val templateIcons: Map<String, String> = emptyMap(),
) {
    val hasChapters: Boolean get() = chapters.isNotEmpty()

    /** The anchor the sky counts down to, such as the moving day. */
    val milestone: Milestone? get() = nextMilestone(chapters, today)

    fun iconOf(chapter: Chapter): String? = chapter.templateId?.let(templateIcons::get)

    fun iconOf(chapterId: String): String? = chapters.firstOrNull { it.id == chapterId }?.let { iconOf(it) }
}

/**
 * The start screen's ViewModel: [tomorrowPlan] over every chapter in the database, re-read after each save, and
 * the two answers a step can get here, *Done* and *Tomorrow*. Also drives the on-device assistant (Gemini Nano),
 * which only explains the plan; it never makes one.
 */
@ViewModelKey
@ContributesIntoMap(AppScope::class)
@Inject
class TodayViewModel(
    private val chapters: ChapterRepository,
    /** The templates, for each chapter's icon. */
    private val content: BundledContent,
    /** Gemini Nano on the device. Bound in :core:data and chosen nowhere else. */
    private val languageModel: LanguageModel,
    /** The day it is on the phone, so the plan follows the phone's own time zone. */
    private val today: Today,
) : ViewModel() {
    private val currentDay = MutableStateFlow(today.now())
    private val currentHour = MutableStateFlow(today.hour())
    private val assistant = MutableStateFlow(AssistantUiState())
    private val templateIcons = MutableStateFlow<Map<String, String>>(emptyMap())

    init {
        viewModelScope.launch {
            templateIcons.value = content.pack().templates
                .mapNotNull { template -> template.icon?.let { template.id to it } }
                .toMap()
        }
    }

    /** Null until the chapters are read, so the screen never flashes an empty state. */
    val state: StateFlow<TodayUiState?> = combine(
        chapters.chapters,
        currentDay,
        currentHour,
        assistant,
        templateIcons,
    ) { all, day, hour, assistant, icons ->
        TodayUiState(
            today = day,
            plan = tomorrowPlan(all, day),
            chapters = all,
            assistant = assistant,
            hour = hour,
            templateIcons = icons,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    /** Reads "today" again and asks whether the assistant is there. Called when the screen is shown. */
    fun refresh() {
        currentDay.value = today.now()
        currentHour.value = today.hour()
        viewModelScope.launch {
            val availability = languageModel.availability()
            assistant.update { it.copy(availability = availability) }
        }
    }

    /** The step is done today: it falls through the now-line. */
    fun done(item: PlannedStep) = change(item.chapterId) { it.complete(item.step.id, currentDay.value) }

    /** "Tomorrow is fine": the step moves to tomorrow. Putting something off is a choice, not a failure. */
    fun tomorrow(item: PlannedStep) =
        change(item.chapterId) { it.withStepDate(item.step.id, currentDay.value.plusDays(1)) }

    /** The step a photo is being taken for, after its photo prompt: chapter id and step id. */
    private var photoFor: Pair<String, String>? = null

    /** The camera is about to take a photo for [stepId], as its photo prompt asked. */
    fun photoFor(chapterId: String, stepId: String) {
        photoFor = chapterId to stepId
    }

    /** Keeps the photo with the step it was taken for. */
    fun keepPhoto(source: Uri) {
        val (chapterId, stepId) = photoFor ?: return
        photoFor = null
        val photo = Entry(UUID.randomUUID().toString(), chapterId, EntryKind.Photo, currentDay.value, stepId = stepId)
        viewModelScope.launch { chapters.keepIfReadable(photo, source) }
    }

    private fun change(chapterId: String, change: (Chapter) -> Chapter) {
        viewModelScope.launch {
            val chapter = chapters.chapter(chapterId).first() ?: return@launch
            chapters.save(change(chapter))
        }
    }

    /** The user asked for Gemini Nano: AICore downloads it and keeps it up to date. */
    fun downloadAssistant() {
        assistant.update { it.copy(availability = AssistantAvailability.Downloading, failed = false) }
        viewModelScope.launch {
            val done = languageModel.download()
            val availability = if (done) AssistantAvailability.Available else languageModel.availability()
            assistant.update { it.copy(availability = availability, failed = !done) }
        }
    }

    /** Lets Gemini Nano explain what is next, from [request] in the user's language. */
    fun askAssistant(request: AssistantRequest) {
        assistant.update { it.copy(writing = true, message = null, failed = false) }
        viewModelScope.launch {
            val answer = languageModel.write(AssistantPrompt.build(request))
                .map(AssistantPrompt::clean)
                .getOrNull()
                ?.takeIf { it.isNotBlank() }
            assistant.update { it.copy(writing = false, message = answer, failed = answer == null) }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
