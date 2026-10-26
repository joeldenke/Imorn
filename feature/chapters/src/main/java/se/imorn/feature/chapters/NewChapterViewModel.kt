package se.imorn.feature.chapters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.imorn.content.Answer
import se.imorn.content.Question
import se.imorn.content.Template
import se.imorn.content.instantiate
import se.imorn.content.suggestedFirst
import se.imorn.data.BundledContent
import se.imorn.data.ProfileStore
import se.imorn.database.ChapterRepository
import se.imorn.model.Chapter
import se.imorn.model.Today
import java.time.LocalDate
import java.util.UUID

/**
 * Starting a chapter in three short stages: what is happening (a template), two to four questions that shape the
 * plan, and the anchor dates (which may stay unknown). Then the plan is built, deterministically, from the template.
 */
data class NewChapterUiState(
    /** Every template, those for the user's life stages first. */
    val templates: List<Template>,
    /** The templates made for the user's life stages; the gallery shows them first. */
    val suggestedTemplateIds: Set<String> = emptySet(),
    val today: LocalDate,
    val template: Template? = null,
    /** Starting from a blank project: a name first, then an empty chapter. */
    val blank: Boolean = false,
    /** The question being asked, an index into the template's questions; past the last one come the dates. */
    val question: Int = 0,
    val answers: Map<String, Answer> = emptyMap(),
    val anchorDates: Map<String, LocalDate?> = emptyMap(),
    val saving: Boolean = false,
    /** The chapter is saved; the screen closes. */
    val created: Boolean = false,
) {
    val stage: Stage
        get() = when {
            blank -> Stage.Blank
            template == null -> Stage.Template
            question < template.questions.size -> Stage.Question
            else -> Stage.Dates
        }

    val currentQuestion: Question? get() = template?.questions?.getOrNull(question)
}

enum class Stage { Template, Blank, Question, Dates }

@ViewModelKey
@ContributesIntoMap(AppScope::class)
@Inject
class NewChapterViewModel(
    private val content: BundledContent,
    private val chapters: ChapterRepository,
    private val profiles: ProfileStore,
    /** The day it is on the phone, so the plan follows the phone's own time zone. */
    private val today: Today,
) : ViewModel() {
    private val _state = MutableStateFlow<NewChapterUiState?>(null)

    /** Null until the templates are read. */
    val state: StateFlow<NewChapterUiState?> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // The templates for the user's life stages first; every template is always there.
            val stages = profiles.profile.filterNotNull().first().lifeStages
            val templates = content.pack().templates.suggestedFirst(stages)
            val stageIds = stages.map { it.id }.toSet()
            _state.value = NewChapterUiState(
                templates = templates,
                suggestedTemplateIds = templates
                    .filter { template -> template.lifestages.any { it in stageIds } }
                    .map { it.id }
                    .toSet(),
                today = today.now(),
            )
        }
    }

    fun chooseTemplate(template: Template) = _state.update {
        it?.copy(
            template = template,
            blank = false,
            question = 0,
            answers = emptyMap(),
            anchorDates = emptyMap(),
        )
    }

    /** Starts a blank chapter: just a name, then the user adds the steps. */
    fun startBlank() = _state.update {
        it?.copy(template = null, blank = true, question = 0, answers = emptyMap(), anchorDates = emptyMap())
    }

    /** Answers the current question and moves on; null skips it, which hides nothing. */
    fun answer(answer: Answer?) = _state.update { state ->
        val question = state?.currentQuestion ?: return@update state
        val answers = if (answer == null) state.answers - question.id else state.answers + (question.id to answer)
        state.copy(answers = answers, question = state.question + 1)
    }

    /** One stage back: the previous question, the template's first question, or the gallery. */
    fun back() = _state.update { state ->
        when {
            state == null -> null
            state.blank -> state.copy(blank = false)
            state.question > 0 -> state.copy(question = state.question - 1)
            else -> state.copy(template = null)
        }
    }

    fun setAnchor(id: String, date: LocalDate?) = _state.update {
        it?.copy(anchorDates = it.anchorDates + (id to date))
    }

    /** Builds the plan in [language] and saves it. */
    fun create(language: String) {
        val state = _state.value ?: return
        val template = state.template ?: return
        if (state.saving) return
        _state.value = state.copy(saving = true)
        viewModelScope.launch {
            val chapter = template.instantiate(
                chapterId = UUID.randomUUID().toString(),
                language = language,
                answers = state.answers,
                anchorDates = state.anchorDates,
                today = state.today,
            )
            chapters.save(chapter)
            _state.update { it?.copy(saving = false, created = true) }
        }
    }

    /** Saves an empty chapter with [title]; the user adds steps and dates from the chapter screen. */
    fun createBlank(title: String) {
        val state = _state.value ?: return
        val name = title.trim()
        if (name.isEmpty() || state.saving) return
        _state.value = state.copy(saving = true)
        viewModelScope.launch {
            chapters.save(
                Chapter(
                    id = UUID.randomUUID().toString(),
                    title = name,
                    createdOn = state.today,
                ),
            )
            _state.update { it?.copy(saving = false, created = true) }
        }
    }
}
