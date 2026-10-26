package se.imorn.notificationpolicy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import se.imorn.model.Anchor
import se.imorn.model.Chapter
import se.imorn.model.RelativeTime
import se.imorn.model.Step
import se.imorn.model.StepStatus
import se.imorn.model.withStepDate
import java.time.LocalDate

class ChapterRemindersTest {
    private val moveDay = LocalDate.of(2027, 3, 1)

    private fun moving(anchor: LocalDate? = moveDay) = Chapter(
        id = "move",
        title = "Moving",
        anchors = listOf(Anchor("moveDay", "Moving day", anchor)),
        steps = listOf(
            Step("van", "Book the van", at = RelativeTime.Days("moveDay", -1)),
            Step(
                "address",
                "Report your move",
                date = LocalDate.of(2027, 3, 6),
                hardDeadline = RelativeTime.Days("moveDay", 7),
            ),
            Step("boxes", "Get boxes"),
            Step("deposit", "Check the deposit", at = RelativeTime.Days("moveDay", 30), longThread = true),
            Step("done", "Change the locks", date = LocalDate.of(2027, 2, 20), status = StepStatus.Done),
            Step("nope", "Cancel the gym", date = LocalDate.of(2027, 2, 21), status = StepStatus.NotApplicable),
        ),
        createdOn = LocalDate.of(2026, 10, 21),
    )

    @Test fun `open steps with a day become reminders on that day`() {
        val reminders = moving().reminders()
        assertEquals(listOf("van", "address"), reminders.map { it.stepId })
        assertEquals(
            listOf(LocalDate.of(2027, 2, 28), LocalDate.of(2027, 3, 6)),
            reminders.map { it.day },
        )
    }

    @Test fun `a hard deadline comes along`() {
        val address = moving().reminders().first { it.stepId == "address" }
        assertEquals(LocalDate.of(2027, 3, 8), address.hardDeadline)
    }

    @Test fun `steps without a day are left out`() {
        assertTrue(moving().reminders().none { it.stepId == "boxes" })
    }

    @Test fun `an unknown anchor leaves only steps with their own date`() {
        val reminders = moving(anchor = null).reminders()
        assertEquals(listOf("address"), reminders.map { it.stepId })
        assertNull(reminders.single().hardDeadline)
    }

    @Test fun `long threads, done and not applicable steps wait`() {
        val ids = moving().reminders().map { it.stepId }
        assertTrue("deposit" !in ids)
        assertTrue("done" !in ids)
        assertTrue("nope" !in ids)
    }

    @Test fun `chapter and step titles follow the reminder`() {
        val address = moving().reminders().first { it.stepId == "address" }
        assertEquals("move", address.chapterId)
        assertEquals("Moving", address.chapterTitle)
        assertEquals("Report your move", address.stepTitle)
    }

    @Test fun `a user's own date wins over the anchor`() {
        val own = LocalDate.of(2027, 2, 1)
        val van = moving().withStepDate("van", own).reminders().first { it.stepId == "van" }
        assertEquals(own, van.day)
    }

    @Test fun `a moving chapter's due steps become one notification`() {
        val plan = NotificationPolicy().planDay(LocalDate.of(2027, 2, 28), moving().reminders())
        val notification = plan.notifications.single() as ReminderNotification
        assertEquals(listOf("van"), notification.reminders.map { it.stepId })
    }
}
