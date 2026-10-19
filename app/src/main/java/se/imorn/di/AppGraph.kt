package se.imorn.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

/**
 * The app's Metro graph. Modules contribute to [AppScope]: the assistant's on-device language model
 * (`@ContributesBinding` in :core:data) and each feature's ViewModels (`@ViewModelKey @ContributesIntoMap`),
 * which [ViewModelGraph] collects into the factory that `metroViewModel()` uses.
 *
 * Data sources are chosen here and nowhere else. Nothing is chosen yet: as Imorn gains persistence and sources,
 * `@Provides` functions move here.
 */
@DependencyGraph(AppScope::class)
interface AppGraph : ViewModelGraph {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(): AppGraph
    }
}
