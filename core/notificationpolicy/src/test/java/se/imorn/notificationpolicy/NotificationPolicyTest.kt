package se.imorn.notificationpolicy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class NotificationPolicyTest {
    private val today = LocalDate.of(2027, 3, 3)

    /** 2027-02-28 is a Sunday. */
    private val sunday = LocalDate.of(2027, 2, 28)

    private fun reminder(
        id: String = "step-1",
        chapterId: String = "move",
        chapterTitle: String = "Moving",
        stepTitle: String = "Book the van",
        day: LocalDate = today,
        hardDeadline: LocalDate? = null,
    ) = Reminder(
        chapterId = chapterId,
        chapterTitle = chapterTitle,
        stepId = id,
        stepTitle = stepTitle,
        day = day,
        hardDeadline = hardDeadline,
    )

    private fun previewPolicy(
        maxPerDay: Int = 2,
        quietHours: QuietHours = QuietHours(),
        at: LocalTime = LocalTime.of(18, 0),
        day: DayOfWeek = DayOfWeek.SUNDAY,
    ) = NotificationPolicy(
        NotificationPolicyConfig(
            maxNotificationsPerDay = maxPerDay,
            quietHours = quietHours,
            weekPreviewEnabled = true,
            weekPreviewDay = day,
            weekPreviewAt = at,
        ),
    )

    @Test fun `several steps on the same day become one notification`() {
        val plan = NotificationPolicy().planDay(
            today,
            listOf(
                reminder(id = "van", stepTitle = "Book the van"),
                reminder(id = "notice", stepTitle = "Give notice"),
                reminder(id = "address", stepTitle = "Report your move"),
            ),
        )
        val notification = plan.notifications.single() as ReminderNotification
        assertEquals(3, notification.reminders.size)
        assertEquals(NotificationKind.Daily, notification.kind)
    }

    @Test fun `steps from different chapters share the day's notification`() {
        val plan = NotificationPolicy().planDay(
            today,
            listOf(
                reminder(id = "van"),
                reminder(id = "keys", chapterId = "home", chapterTitle = "The new home"),
            ),
        )
        val notification = plan.notifications.single() as ReminderNotification
        assertEquals(listOf("home", "move"), notification.reminders.map { it.chapterId }.distinct().sorted())
    }

    @Test fun `overdue steps join today's notification, oldest first`() {
        val plan = NotificationPolicy().planDay(
            today,
            listOf(
                reminder(id = "today"),
                reminder(id = "old", day = today.minusDays(5)),
                reminder(id = "newer", day = today.minusDays(1)),
            ),
        )
        val notification = plan.notifications.single() as ReminderNotification
        assertEquals(listOf("old", "newer", "today"), notification.reminders.map { it.stepId })
    }

    @Test fun `steps for another day are left to that day`() {
        val plan = NotificationPolicy().planDay(today, listOf(reminder(day = today.plusDays(1))))
        assertTrue(plan.isEmpty)
    }

    @Test fun `nothing due gives an empty plan`() {
        assertTrue(NotificationPolicy().planDay(today, emptyList()).isEmpty)
    }

    @Test fun `the same step is never notified twice`() {
        val step = reminder()
        val plan = NotificationPolicy().planDay(today, listOf(step, step))
        val notification = plan.notifications.single() as ReminderNotification
        assertEquals(1, notification.reminders.size)
    }

    @Test fun `the day's notification carries the day and the morning time`() {
        val plan = NotificationPolicy().planDay(today, listOf(reminder()))
        assertEquals(today, plan.day)
        assertEquals(LocalTime.of(9, 0), plan.notifications.single().at)
    }

    @Test fun `a hard deadline gets its own notification when the day has room`() {
        val plan = NotificationPolicy().planDay(
            today,
            listOf(
                reminder(id = "address", hardDeadline = today),
                reminder(id = "van"),
            ),
        )
        assertEquals(2, plan.notifications.size)
        val (daily, urgent) = plan.notifications.map { it as ReminderNotification }
        assertEquals(listOf("van"), daily.reminders.map { it.stepId })
        assertEquals(NotificationKind.Daily, daily.kind)
        assertEquals(listOf("address"), urgent.reminders.map { it.stepId })
        assertEquals(NotificationKind.Deadline, urgent.kind)
    }

    @Test fun `with a budget of one a hard deadline stays in the day's notification`() {
        val policy = NotificationPolicy(NotificationPolicyConfig(maxNotificationsPerDay = 1))
        val plan = policy.planDay(
            today,
            listOf(
                reminder(id = "address", hardDeadline = today),
                reminder(id = "van"),
            ),
        )
        val notification = plan.notifications.single() as ReminderNotification
        assertEquals(setOf("van", "address"), notification.reminders.map { it.stepId }.toSet())
        assertEquals(NotificationKind.Daily, notification.kind)
    }

    @Test fun `a day with only hard deadlines is marked urgent`() {
        val plan = NotificationPolicy().planDay(
            today,
            listOf(reminder(day = today.minusDays(1), hardDeadline = today)),
        )
        val notification = plan.notifications.single() as ReminderNotification
        assertTrue(notification.isUrgent)
        assertEquals(NotificationKind.Deadline, notification.kind)
    }

    @Test fun `quiet hours cover the night and end at eight`() {
        val policy = NotificationPolicy()
        assertTrue(policy.isQuiet(LocalTime.of(22, 0)))
        assertTrue(policy.isQuiet(LocalTime.of(23, 30)))
        assertTrue(policy.isQuiet(LocalTime.of(6, 0)))
        assertTrue(policy.isQuiet(LocalTime.of(7, 59)))
        assertFalse(policy.isQuiet(LocalTime.of(8, 0)))
        assertFalse(policy.isQuiet(LocalTime.of(21, 59)))
    }

    @Test fun `quiet hours that do not span midnight are handled too`() {
        val quiet = QuietHours(start = LocalTime.of(13, 0), end = LocalTime.of(15, 0))
        assertFalse(quiet.contains(LocalTime.of(12, 59)))
        assertTrue(quiet.contains(LocalTime.of(13, 0)))
        assertTrue(quiet.contains(LocalTime.of(14, 59)))
        assertFalse(quiet.contains(LocalTime.of(15, 0)))
        assertEquals(LocalTime.of(15, 0), quiet.nextAllowed(LocalTime.of(14, 0)))
    }

    @Test fun `equal start and end mean no quiet hours`() {
        val quiet = QuietHours(start = LocalTime.of(9, 0), end = LocalTime.of(9, 0))
        assertFalse(quiet.contains(LocalTime.of(9, 0)))
    }

    @Test fun `a notification time inside quiet hours moves to the first allowed time`() {
        val policy = NotificationPolicy(NotificationPolicyConfig(dailyAt = LocalTime.of(23, 0)))
        val plan = policy.planDay(today, listOf(reminder()))
        assertEquals(LocalTime.of(8, 0), plan.notifications.single().at)
    }

    @Test fun `the week preview is off until the user turns it on`() {
        val reminders = listOf(reminder(day = sunday.plusDays(3)))
        assertTrue(NotificationPolicy().planDay(sunday, reminders).isEmpty)
        assertNull(NotificationPolicy().weekPreview(sunday, reminders))
    }

    @Test fun `the week preview is added after the day's reminders`() {
        val plan = previewPolicy().planDay(
            sunday,
            listOf(reminder(day = sunday), reminder(id = "next", day = sunday.plusDays(3))),
        )
        assertEquals(
            listOf(NotificationKind.Daily, NotificationKind.WeekPreview),
            plan.notifications.map { it.kind },
        )
    }

    @Test fun `due steps win the only slot when the budget is one`() {
        val plan = previewPolicy(maxPerDay = 1).planDay(
            sunday,
            listOf(reminder(day = sunday), reminder(id = "next", day = sunday.plusDays(3))),
        )
        assertEquals(listOf(NotificationKind.Daily), plan.notifications.map { it.kind })
    }

    @Test fun `the preview still goes out on a Sunday with nothing due`() {
        val plan = previewPolicy(maxPerDay = 1).planDay(sunday, listOf(reminder(day = sunday.plusDays(3))))
        assertEquals(listOf(NotificationKind.WeekPreview), plan.notifications.map { it.kind })
    }

    @Test fun `the week preview shows the coming seven days only`() {
        val preview = previewPolicy().weekPreview(
            sunday,
            listOf(
                reminder(id = "today", day = sunday),
                reminder(id = "monday", day = sunday.plusDays(1)),
                reminder(id = "next-sunday", day = sunday.plusDays(7)),
                reminder(id = "after", day = sunday.plusDays(8)),
            ),
        )!!
        assertEquals(listOf("monday", "next-sunday"), preview.items.map { it.stepId })
    }

    @Test fun `the week preview stays short and counts the rest`() {
        val reminders = (1..7).map { day -> reminder(id = "step-$day", day = sunday.plusDays(day.toLong())) }
        val preview = previewPolicy().weekPreview(sunday, reminders)!!
        assertEquals(5, preview.items.size)
        assertEquals(2, preview.remaining)
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), preview.items.map { it.daysUntil })
    }

    @Test fun `the week preview only runs on its weekday by default`() {
        val monday = LocalDate.of(2027, 3, 1)
        assertNull(previewPolicy().weekPreview(monday, listOf(reminder(day = monday.plusDays(2)))))
    }

    @Test fun `the week preview can move to another day and time`() {
        val saturday = LocalDate.of(2027, 2, 27)
        val policy = previewPolicy(at = LocalTime.of(17, 0), day = DayOfWeek.SATURDAY)
        val plan = policy.planDay(saturday, listOf(reminder(day = saturday.plusDays(2))))
        val notification = plan.notifications.single() as WeekPreviewNotification
        assertEquals(LocalTime.of(17, 0), notification.at)
        assertEquals(saturday, notification.preview.day)
    }

    @Test fun `the week preview also stays out of quiet hours`() {
        val quiet = QuietHours(start = LocalTime.of(17, 0), end = LocalTime.of(19, 0))
        val plan = previewPolicy(quietHours = quiet).planDay(sunday, listOf(reminder(day = sunday.plusDays(3))))
        assertEquals(LocalTime.of(19, 0), plan.notifications.single().at)
    }

    @Test fun `an empty coming week sends no preview`() {
        assertNull(previewPolicy().weekPreview(sunday, listOf(reminder(day = sunday.plusDays(20)))))
    }

    @Test fun `the preview carries the chapter and step that are coming`() {
        val address = reminder(
            id = "address",
            chapterTitle = "Moving",
            stepTitle = "Report your move",
            day = sunday.plusDays(2),
        )
        val preview = previewPolicy().weekPreview(sunday, listOf(address))!!
        val item = preview.items.single()
        assertEquals("move", item.chapterId)
        assertEquals("Moving", item.chapterTitle)
        assertEquals("address", item.stepId)
        assertEquals("Report your move", item.stepTitle)
        assertEquals(sunday.plusDays(2), item.day)
        assertEquals(2L, item.daysUntil)
    }

    @Test fun `input order does not change the plan`() {
        val steps = listOf(
            reminder(id = "van", day = today.minusDays(1)),
            reminder(id = "address", chapterId = "home", day = today),
        )
        val first = NotificationPolicy().planDay(today, steps)
        val second = NotificationPolicy().planDay(today, steps.reversed())
        assertEquals(first, second)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `more than two notifications a day is rejected`() {
        NotificationPolicyConfig(maxNotificationsPerDay = 3)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a week preview needs room for at least one step`() {
        NotificationPolicyConfig(weekPreviewMaxItems = 0)
    }
}
