package se.imorn.content

import se.imorn.model.Anchor
import se.imorn.model.Chapter
import se.imorn.model.LinkRef
import se.imorn.model.Phase
import se.imorn.model.RelativeTime
import se.imorn.model.Step
import java.time.LocalDate

/** An answer to a template's question. Unanswered questions hide nothing. */
sealed interface Answer {
    data class Choice(val option: String) : Answer

    data class YesNo(val value: Boolean) : Answer
}

/**
 * Whether a step applies given [answers]. A question without an answer keeps the step: it is better to show one
 * step too many than to hide one that was needed.
 */
fun Condition?.appliesTo(answers: Map<String, Answer>): Boolean {
    if (this == null) return true
    return when (val answer = answers[question]) {
        null -> true

        is Answer.Choice -> when {
            equals != null -> answer.option == equals
            isIn != null -> answer.option in isIn
            else -> true
        }

        is Answer.YesNo -> isTrue == null || answer.value == isTrue
    }
}

/** The template time as the model's [RelativeTime]. The validator makes sure exactly one kind is set. */
fun TemplateTime.toRelativeTime(): RelativeTime = when {
    offsetDays != null -> RelativeTime.Days(anchor, offsetDays)
    offsetMonths != null -> RelativeTime.Months(anchor, offsetMonths)
    nextYear != null -> RelativeTime.NextYear(anchor, nextYear.month)
    else -> error("Time from $anchor has no offset")
}

/**
 * Makes a chapter from the template: the steps that apply to [answers], in [language], with the [anchorDates] known
 * so far (missing or null ones stay unknown). Phases left without steps are dropped, so the user never sees an empty
 * group. The chapter remembers the template and its version, and each step its template id.
 */
fun Template.instantiate(
    chapterId: String,
    language: String,
    answers: Map<String, Answer>,
    anchorDates: Map<String, LocalDate?>,
    today: LocalDate,
    title: String? = null,
): Chapter {
    fun TemplateStep.toStep(phaseId: String?, longThread: Boolean) = Step(
        id = id,
        title = this.title.text(language),
        phaseId = phaseId,
        why = why?.text(language),
        at = at?.toRelativeTime(),
        hardDeadline = hardDeadline?.toRelativeTime(),
        links = links.map(::LinkRef),
        photoPrompt = photoPrompt?.text(language),
        longThread = longThread,
    )

    fun TemplateStep.applies() = !retired && condition.appliesTo(answers)

    val phaseSteps = phases.associate { phase -> phase.id to phase.steps.filter { it.applies() } }
    return Chapter(
        id = chapterId,
        title = title ?: this.title.text(language),
        templateId = id,
        templateVersion = version,
        anchors = anchors.map { Anchor(it.id, it.label.text(language), anchorDates[it.id]) },
        phases = phases
            .filter { phaseSteps[it.id].orEmpty().isNotEmpty() }
            .map { Phase(it.id, it.title.text(language)) },
        steps = phases.flatMap { phase ->
            phaseSteps[phase.id].orEmpty().map { it.toStep(phase.id, longThread = false) }
        } + longThreads.filter { it.applies() }.map { it.toStep(phaseId = null, longThread = true) },
        createdOn = today,
    )
}
