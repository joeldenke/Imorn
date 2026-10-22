package se.imorn.ui

import se.imorn.model.Anchor
import se.imorn.model.Chapter
import se.imorn.model.LinkRef
import se.imorn.model.Phase
import se.imorn.model.RelativeTime
import se.imorn.model.Step
import se.imorn.model.complete
import java.time.LocalDate

/**
 * The one person and chapter all previews show, so screenshots are comparable and reviewable (the same idea as
 * Novila's PreviewData in :core:ui). Feature previews read this instead of inventing their own sample content.
 */
object PreviewData {
    const val ASSISTANT_MESSAGE =
        "You are three steps in. Booking the van next keeps the rest of the week on track."

    /** The day every preview is drawn on. */
    val TODAY: LocalDate = LocalDate.of(2027, 2, 10)

    /** Moving on 1 March: some steps done, one late, the rest ahead. */
    val moving: Chapter = Chapter(
        id = "preview-move",
        title = "Moving to Gävle",
        templateId = "move",
        templateVersion = 1,
        anchors = listOf(Anchor("moveDay", "Moving day", LocalDate.of(2027, 3, 1))),
        phases = listOf(Phase("plan", "Plan"), Phase("papers", "Paperwork"), Phase("packing", "Packing")),
        steps = listOf(
            Step("book-movers", "Book movers or a trailer", "plan", at = RelativeTime.Days("moveDay", -56)),
            Step("day-off", "Book time off for moving day", "plan", at = RelativeTime.Days("moveDay", -42)),
            Step("boxes", "Get moving boxes, tape and a marker", "packing", at = RelativeTime.Days("moveDay", -28)),
            Step(
                "address-change",
                "Report your move",
                "papers",
                why = "At the earliest four weeks before and at the latest one week after the move.",
                at = RelativeTime.Days("moveDay", -14),
                hardDeadline = RelativeTime.Days("moveDay", 7),
                links = listOf(LinkRef("skatteverket.flytt")),
            ),
            Step("first-night-bag", "Pack a bag for the first night", "packing", at = RelativeTime.Days("moveDay", -2)),
            Step(
                "electricity-review",
                "Review the electricity contract after the first bill",
                at = RelativeTime.Months("moveDay", 2),
                longThread = true,
            ),
        ),
        createdOn = LocalDate.of(2026, 12, 1),
    ).complete("book-movers", LocalDate.of(2027, 1, 4)).complete("day-off", LocalDate.of(2027, 1, 18))
}
