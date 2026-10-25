package se.imorn

import android.app.Application
import dev.zacsweers.metro.createGraphFactory
import se.imorn.database.ChapterArchive
import se.imorn.di.AppGraph

/**
 * Holds the app's Metro graph, created on first use. Modules contribute their bindings to it (the stores and the
 * assistant's language model in :core:data, each screen's ViewModel), and `metroViewModel()` in an entry asks it for
 * one.
 */
class ImornApplication : Application() {
    val graph: AppGraph by lazy { createGraphFactory<AppGraph.Factory>().create(applicationContext) }

    /**
     * The home screen widget has no entry to ask for a repository, so it finds the one graph made through
     * [ChapterArchive]. This runs for every process the widget can wake, once, before the receiver reads it.
     */
    override fun onCreate() {
        super.onCreate()
        ChapterArchive.publish(graph.chapterRepository)
    }
}
