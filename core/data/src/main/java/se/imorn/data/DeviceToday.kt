package se.imorn.data

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import se.imorn.model.Today
import java.time.Clock
import java.time.LocalDate

/**
 * The phone's own day, read from the phone's clock in the phone's own time zone. It is read fresh every time, so a
 * restart, a flight or the clocks going back never leave a stale day behind. Bound as the app's [Today].
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DeviceToday internal constructor(private val clock: Clock) : Today {
    /** The phone's own clock, in the phone's own time zone. */
    @Inject
    constructor() : this(Clock.systemDefaultZone())

    override fun now(): LocalDate = LocalDate.now(clock)
}
