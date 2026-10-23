package se.imorn.database

import android.content.Context

/**
 * Where the app's one [ChapterRepository] is reachable from code that runs outside a navigation entry: the home
 * screen widget's receiver and actions, which Android starts on their own, without going through an entry that
 * could ask the Metro graph for one.
 *
 * [se.imorn.ImornApplication] publishes the repository here right after it creates its graph; anything that reads
 * before that (or in a test with no app) gets null, and the widget simply shows nothing. This is the only
 * shortcut into the graph outside an entry, and it holds a value the graph already made `@SingleIn(AppScope)`.
 */
object ChapterArchive {
    @Volatile
    private var published: ChapterRepository? = null

    /** The repository the app published, or null when nothing has (yet). */
    fun repository(): ChapterRepository? = published

    /** For the app to publish its repository once the graph exists. */
    fun publish(repository: ChapterRepository) {
        published = repository
    }
}
