package se.imorn.data

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import se.imorn.content.ContentPack

/**
 * The content pack baked into the app: `content/` in the repository, packaged as assets (`imorn { bundledContent() }`),
 * so templates and links work offline from the first start. Read once, off the main thread. The pack fetched in
 * phase 4 will take its place when it is newer and signed.
 */
@SingleIn(AppScope::class)
@Inject
class BundledContent(private val context: Context) {
    private val pack by lazy { read() }

    suspend fun pack(): ContentPack = withContext(Dispatchers.IO) { pack }

    private fun read(): ContentPack {
        val assets = context.assets
        fun text(path: String) = assets.open(path).bufferedReader().use { it.readText() }
        return ContentPack.parse(
            linksJson = text("links.json"),
            templateJsons = assets.list(TEMPLATES).orEmpty()
                .filter { it.endsWith(".json") }
                .sorted()
                .map { text("$TEMPLATES/$it") },
        )
    }

    private companion object {
        const val TEMPLATES = "templates"
    }
}
