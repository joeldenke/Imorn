package se.imorn.model

import java.time.LocalDate

/**
 * A day counted from an anchor, as templates write them: `moving day − 6 weeks`, `handover + 3 months`, `next year,
 * April`. When the anchor moves, so does every step counted from it.
 */
sealed interface RelativeTime {
    /** The id of the anchor it counts from. */
    val anchor: String

    /** The day when the anchor is [anchorDate]. */
    fun resolve(anchorDate: LocalDate): LocalDate

    /** [offset] days from the anchor; negative is before. */
    data class Days(override val anchor: String, val offset: Int) : RelativeTime {
        override fun resolve(anchorDate: LocalDate): LocalDate = anchorDate.plusDays(offset.toLong())
    }

    /** [offset] months from the anchor, on the same day of the month or the month's last day. */
    data class Months(override val anchor: String, val offset: Int) : RelativeTime {
        override fun resolve(anchorDate: LocalDate): LocalDate = anchorDate.plusMonths(offset.toLong())
    }

    /** The first of [month] in the year after the anchor's: the tax return the spring after a sale. */
    data class NextYear(override val anchor: String, val month: Int) : RelativeTime {
        init {
            require(month in 1..12) { "month must be 1-12, was $month" }
        }

        override fun resolve(anchorDate: LocalDate): LocalDate = LocalDate.of(anchorDate.year + 1, month, 1)
    }
}
