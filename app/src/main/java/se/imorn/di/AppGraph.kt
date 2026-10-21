package se.imorn.di

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import se.imorn.data.SettingsStore

/**
 * The app's Metro graph. Modules contribute to [AppScope]: the stores and the assistant's on-device language model
 * (`@Inject` and `@ContributesBinding` in :core:data) and each feature's ViewModels (`@ViewModelKey
 * @ContributesIntoMap`), which [ViewModelGraph] collects into the factory that `metroViewModel()` uses.
 *
 * Data sources are chosen here and nowhere else, with `@Provides` functions, as Imorn gains them.
 */
@DependencyGraph(AppScope::class)
interface AppGraph : ViewModelGraph {
    /** The theme, read by [se.imorn.MainActivity] before it draws anything. */
    val settingsStore: SettingsStore

    @DependencyGraph.Factory
    fun interface Factory {
        /** [context] is the application context. */
        fun create(@Provides context: Context): AppGraph
    }
}
