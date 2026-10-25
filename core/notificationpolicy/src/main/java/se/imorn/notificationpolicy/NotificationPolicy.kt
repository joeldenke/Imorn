package se.imorn.notificationpolicy

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * How much Imorn notifies: at most [maxNotificationsPerDay] notifications a day, everything due merged into one,
 * never inside [quietHours], and an optional week preview that is off until the user turns it on.
 *
 * The policy only decides *what* to send and *when*; it builds no sentences, so the notifier renders every text from
 * `strings.xml`. Days are [LocalDate] and times are [LocalTime]; the scheduler owns the time zone, restarts and
 * daylight saving.
 */
data class NotificationPolicyConfig(
    /**
     * The most notifications one day may send. One is the calm default; two leaves room for a hard deadline that
     * stands on its own. The week preview counts as one of them, so due steps always come first.
     */
    val maxNotificationsPerDay: Int = 2,
    /** When the day's reminders go out, unless that falls in quiet hours. */
    val dailyAt: LocalTime = LocalTime.of(9, 0),
    val quietHours: QuietHours = QuietHours(),
    /** *Veckan som kommer*: an optional weekly notification looking one week ahead. Off until the user turns it on. */
    val weekPreviewEnabled: Boolean = false,
    val weekPreviewDay: DayOfWeek = DayOfWeek.SUNDAY,
    val weekPreviewAt: LocalTime = LocalTime.of(18, 0),
    /** The preview stays short: show at most this many steps and count the rest. */
    val weekPreviewMaxItems: Int = 5,
) {
    init {
        require(maxNotificationsPerDay in 1..2) {
            "A day sends one or two notifications, not $maxNotificationsPerDay"
        }
        require(weekPreviewMaxItems > 0) { "The week preview needs room for at least one step" }
    }
}

/**
 * The hours Imorn stays silent, by default 22:00 to 08:00. [end] is the first hour that is allowed again, so the
 * quiet hours never swallow the morning.
 */
data class QuietHours(
    val start: LocalTime = LocalTime.of(22, 0),
    val end: LocalTime = LocalTime.of(8, 0),
) {
    /** True for [time] inside the quiet hours, including the night when they span midnight. */
    fun contains(time: LocalTime): Boolean = when {
        start == end -> false
        start < end -> time >= start && time < end
        else -> time >= start || time < end
    }

    /** [time] itself when it is allowed, otherwise the first allowed time after it. */
    fun nextAllowed(time: LocalTime): LocalTime = if (contains(time)) end else time
}

/**
 * One open step on the day the plan gives it, ready for the policy. Titles are in the chapter's language; the policy
 * passes them along but never writes UI text of its own.
 */
data class Reminder(
    val chapterId: String,
    val chapterTitle: String,
    val stepId: String,
    val stepTitle: String,
    val day: LocalDate,
    /** The last day that really counts, when the template marks one and its anchor is known. */
    val hardDeadline: LocalDate? = null,
) {
    fun isDueBy(on: LocalDate): Boolean = day <= on

    fun isOverdue(on: LocalDate): Boolean = day < on

    fun hasHardDeadlineBy(on: LocalDate): Boolean = hardDeadline != null && hardDeadline <= on
}

enum class NotificationKind { Daily, Deadline, WeekPreview }

sealed interface Notification {
    val kind: NotificationKind

    /** The time it goes out, always outside quiet hours. */
    val at: LocalTime
}

/**
 * One notification for the day's due steps. Every step due on or before the day is merged into it, so a backlog never
 * becomes a stream of pings.
 */
data class ReminderNotification(
    override val at: LocalTime,
    val reminders: List<Reminder>,
    /** True when the notification holds only steps whose hard deadline has passed or is today. */
    val isUrgent: Boolean = false,
) : Notification {
    override val kind: NotificationKind
        get() = if (isUrgent) NotificationKind.Deadline else NotificationKind.Daily
}

data class WeekPreviewNotification(
    override val at: LocalTime,
    val preview: WeekPreview,
) : Notification {
    override val kind: NotificationKind get() = NotificationKind.WeekPreview
}

/** What the optional weekly notification holds: the steps of the coming week, soonest first. */
data class WeekPreview(
    val day: LocalDate,
    val items: List<WeekPreviewItem>,
    /** Steps in the coming week that did not fit in [items]. */
    val remaining: Int,
)

data class WeekPreviewItem(
    val chapterId: String,
    val chapterTitle: String,
    val stepId: String,
    val stepTitle: String,
    val day: LocalDate,
    val daysUntil: Long,
)

/** Everything the notifier should send on one day. Empty when nothing is due and no preview falls on it. */
data class DayPlan(
    val day: LocalDate,
    val notifications: List<Notification>,
) {
    val isEmpty: Boolean get() = notifications.isEmpty()
}

/**
 * Deterministic decisions about notification volume, testable without Android. The same reminders and the same day
 * always give the same plan; no model is involved.
 */
class NotificationPolicy(private val config: NotificationPolicyConfig = NotificationPolicyConfig()) {

    /**
     * What to send on [day], given every open step the scheduler knows about. Steps due on [day] and overdue ones
     * share one notification; a hard deadline may get a notification of its own when the day has room, and the
     * optional week preview takes a free slot.
     */
    fun planDay(day: LocalDate, reminders: List<Reminder>): DayPlan {
        val due = reminders
            .filter { it.isDueBy(day) }
            .sortedWith(compareBy({ it.day }, { it.chapterId }, { it.stepId }))
            .distinctBy { it.chapterId to it.stepId }
        val preview = weekPreview(day, reminders)
        val notifications = mutableListOf<Notification>()
        val at = config.quietHours.nextAllowed(config.dailyAt)

        if (due.isNotEmpty()) {
            val urgent = due.filter { it.hasHardDeadlineBy(day) }
            val rest = due.filterNot { it.hasHardDeadlineBy(day) }
            val splitForDeadline = config.maxNotificationsPerDay >= 2 &&
                preview == null &&
                urgent.isNotEmpty() &&
                rest.isNotEmpty()
            if (splitForDeadline) {
                notifications += ReminderNotification(at = at, reminders = rest)
                notifications += ReminderNotification(at = at, reminders = urgent, isUrgent = true)
            } else {
                notifications += ReminderNotification(
                    at = at,
                    reminders = due,
                    isUrgent = rest.isEmpty() && urgent.isNotEmpty(),
                )
            }
        }

        if (preview != null && notifications.size < config.maxNotificationsPerDay) {
            notifications += WeekPreviewNotification(
                at = config.quietHours.nextAllowed(config.weekPreviewAt),
                preview = preview,
            )
        }

        return DayPlan(day = day, notifications = notifications)
    }

    /**
     * The optional *Veckan som kommer* for [day]: null unless it is enabled, [day] is its weekday, and the coming
     * seven days hold at least one open step. The week starts the day after [day] and includes the same weekday next
     * week, so nothing due right after the weekend is missed.
     */
    fun weekPreview(day: LocalDate, reminders: List<Reminder>): WeekPreview? {
        if (!config.weekPreviewEnabled || day.dayOfWeek != config.weekPreviewDay) return null
        val weekStart = day.plusDays(1)
        val weekEnd = day.plusDays(7)
        val coming = reminders
            .filter { it.day in weekStart..weekEnd }
            .sortedWith(compareBy({ it.day }, { it.chapterTitle }, { it.stepTitle }))
            .distinctBy { it.chapterId to it.stepId }
        if (coming.isEmpty()) return null
        val shown = coming.take(config.weekPreviewMaxItems)
        return WeekPreview(
            day = day,
            items = shown.map { reminder ->
                WeekPreviewItem(
                    chapterId = reminder.chapterId,
                    chapterTitle = reminder.chapterTitle,
                    stepId = reminder.stepId,
                    stepTitle = reminder.stepTitle,
                    day = reminder.day,
                    daysUntil = reminder.day.toEpochDay() - day.toEpochDay(),
                )
            },
            remaining = coming.size - shown.size,
        )
    }

    /** True when [time] is inside the quiet hours, and no notification may go out then. */
    fun isQuiet(time: LocalTime): Boolean = config.quietHours.contains(time)

    /** [time] itself when it is allowed, otherwise the first allowed time after it. */
    fun nextAllowed(time: LocalTime): LocalTime = config.quietHours.nextAllowed(time)
}
