package se.imorn.model

import java.time.LocalDate

/** A small moving chapter for the tests: the moving day is [moveDay], null while it is unknown. */
internal fun moving(
    moveDay: LocalDate? = LocalDate.of(2027, 3, 1),
    id: String = "move",
    state: ChapterState = ChapterState.Active,
) = Chapter(
    id = id,
    title = "Moving",
    templateId = "move",
    templateVersion = 1,
    anchors = listOf(Anchor("moveDay", "Moving day", moveDay)),
    phases = listOf(Phase("plan", "Plan"), Phase("papers", "Paperwork")),
    steps = listOf(
        Step("book-van", "Book the van", phaseId = "plan", at = RelativeTime.Days("moveDay", -56)),
        Step("notice", "Give notice", phaseId = "plan", at = RelativeTime.Months("moveDay", -3)),
        Step(
            "address",
            "Report your move",
            phaseId = "papers",
            at = RelativeTime.Days("moveDay", -14),
            hardDeadline = RelativeTime.Days("moveDay", 7),
        ),
        Step("boxes", "Get boxes", phaseId = "plan"),
        Step("deposit", "Check the deposit", at = RelativeTime.Months("moveDay", 1), longThread = true),
    ),
    state = state,
    createdOn = LocalDate.of(2026, 10, 21),
)

internal val Iterable<PlannedStep>.ids get() = map { it.step.id }
