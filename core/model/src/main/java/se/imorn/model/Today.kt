package se.imorn.model

import java.time.LocalDate

/**
 * The day it is on the phone. Imorn plans days, not times: a day has no time of day and no time zone, so a plan never
 * moves when the phone travels or the clocks change. Only *today* follows the phone.
 */
fun interface Today {
    /** The day it is now, in the phone's own time zone. */
    fun now(): LocalDate

    /**
     * The hour it is now, 0 to 23, for what only follows the light: the start screen's sky and its greeting. Never
     * for the plan, which has no time of day.
     */
    fun hour(): Int = NOON

    companion object {
        const val NOON = 12
    }
}
