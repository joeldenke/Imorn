package se.imorn.navigation

import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import se.imorn.R
import se.imorn.feature.today.navigation.TodayRoute

/** Tabs in the bottom navigation bar, in display order. Each tab keeps its own back stack. */
enum class TopLevelDestination(
    val route: NavKey,
    @param:StringRes val label: Int,
) {
    Today(TodayRoute, R.string.nav_today),
    ;

    companion object {
        val start = Today
        val routes: Set<NavKey> = entries.map { it.route }.toSet()
    }
}
