package se.imorn.model

import java.time.LocalDate

/**
 * The day it is on the phone. Imorn plans days, not times: a day has no time of day and no time zone, so a plan never
 * moves when the phone travels or the clocks change. Only *today* follows the phone.
 */
fun interface Today {
    /** The day it is now, in the phone's own time zone. */
    fun now(): LocalDate
}
