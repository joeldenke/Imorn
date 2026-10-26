package se.imorn.content

import kotlinx.serialization.json.Json
import se.imorn.model.LifeStage

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

/**
 * The templates in the order to offer them: those made for one of [lifeStages] first, then the rest, each part in
 * the pack's order. Every template stays in the list; the stages only decide what comes first.
 */
fun List<Template>.suggestedFirst(lifeStages: Set<LifeStage>): List<Template> {
    val ids = lifeStages.map { it.id }.toSet()
    val (suggested, rest) = partition { template -> template.lifestages.any { it in ids } }
    return suggested + rest
}

/** The templates in [category], or all of them when it is null, in the pack's order. */
fun List<Template>.inCategory(category: TemplateCategory?): List<Template> =
    if (category == null) this else filter { it.category == category }

/**
 * Templates whose title, description or helps contain [query] in [language], ignoring case and surrounding space.
 * An empty query keeps everything. The order stays the same, so the gallery can keep suggestions first.
 */
fun List<Template>.matching(query: String, language: String): List<Template> {
    val needle = query.trim().lowercase()
    if (needle.isEmpty()) return this
    return filter { template ->
        listOf(template.title, template.description, template.helps).any { text ->
            text.text(language).lowercase().contains(needle)
        }
    }
}

/** The templates grouped by category, in the enum's order; empty categories are left out. */
fun List<Template>.groupedByCategory(): List<Pair<TemplateCategory, List<Template>>> =
    TemplateCategory.entries.mapNotNull { category ->
        filter { it.category == category }.takeIf { it.isNotEmpty() }?.let { category to it }
    }
