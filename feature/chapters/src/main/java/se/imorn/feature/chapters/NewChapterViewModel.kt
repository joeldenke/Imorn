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
import java.time.LocalDate
import java.util.UUID

/**
 * Starting a chapter in three short stages: what is happening (a template), two to four questions that shape the
 * plan, and the anchor dates (which may stay unknown). Then the plan is built, deterministically, from the template.
 */
data class NewChapterUiState(
    val templates: List<Template>,
    val today: LocalDate,
    val template: Template? = null,
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
            template == null -> Stage.Template
            question < template.questions.size -> Stage.Question
            else -> Stage.Dates
        }

    val currentQuestion: Question? get() = template?.questions?.getOrNull(question)
}

enum class Stage { Template, Question, Dates }

@ViewModelKey
@ContributesIntoMap(AppScope::class)
@Inject
class NewChapterViewModel(
    private val content: BundledContent,
    private val chapters: ChapterRepository,
    private val profiles: ProfileStore,
) : ViewModel() {
    private val _state = MutableStateFlow<NewChapterUiState?>(null)

    /** Null until the templates are read. */
    val state: StateFlow<NewChapterUiState?> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // The templates for the user's life stages first; every template is always there.
            val stages = profiles.profile.filterNotNull().first().lifeStages
            _state.value = NewChapterUiState(
                templates = content.pack().templates.suggestedFirst(stages),
                today = LocalDate.now(),
            )
        }
    }

    fun chooseTemplate(template: Template) = _state.update {
        it?.copy(template = template, question = 0, answers = emptyMap(), anchorDates = emptyMap())
    }

    /** Answers the current question and moves on; null skips it, which hides nothing. */
    fun answer(answer: Answer?) = _state.update { state ->
        val question = state?.currentQuestion ?: return@update state
        val answers = if (answer == null) state.answers - question.id else state.answers + (question.id to answer)
        state.copy(answers = answers, question = state.question + 1)
    }

    /** One stage back: the previous question, or the list of templates. */
    fun back() = _state.update { state ->
        when {
            state == null -> null
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
}
