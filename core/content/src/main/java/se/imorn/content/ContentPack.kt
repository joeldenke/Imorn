package se.imorn.content

import kotlinx.serialization.json.Json

/** The templates and links the app plans with: the pack baked into the app, later the one fetched (phase 4). */
data class ContentPack(val links: List<Link>, val templates: List<Template>) {
    fun link(id: String): Link? = links.firstOrNull { it.id == id }

    fun template(id: String): Template? = templates.firstOrNull { it.id == id }

    companion object {
        /** Unknown keys are skipped, so a pack written for a newer app still reads. */
        private val json = Json { ignoreUnknownKeys = true }

        /** Reads `links.json` and the files in `templates/`. Throws on a file that isn't valid JSON for the format. */
        fun parse(linksJson: String, templateJsons: List<String>): ContentPack = ContentPack(
            links = json.decodeFromString<LinkRegistry>(linksJson).links,
            templates = templateJsons.map { json.decodeFromString<Template>(it) },
        )
    }
}
