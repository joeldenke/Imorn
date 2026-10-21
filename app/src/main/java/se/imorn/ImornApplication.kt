package se.imorn

import android.app.Application
import dev.zacsweers.metro.createGraphFactory
import se.imorn.di.AppGraph

/**
 * Holds the app's Metro graph, created on first use. Modules contribute their bindings to it (the stores and the
 * assistant's language model in :core:data, each screen's ViewModel), and `metroViewModel()` in an entry asks it for
 * one.
 */
class ImornApplication : Application() {
    val graph: AppGraph by lazy { createGraphFactory<AppGraph.Factory>().create(applicationContext) }
}
