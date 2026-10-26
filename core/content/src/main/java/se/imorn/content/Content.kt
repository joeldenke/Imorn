package se.imorn.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The content format, version 1: templates and the link registry as they are written in `content/` (see
 * docs/content-system.md). Every text is a [Localized] map with Swedish and English. Ids are forever: a step changes
 * its text, never its id, and a removed one is marked [TemplateStep.retired].
 */
typealias Localized = Map<String, String>

/** The text in [language], falling back to English, then Swedish, then whatever there is. */
fun Localized.text(language: String): String =
    get(language) ?: get(ENGLISH) ?: get(SWEDISH) ?: values.firstOrNull().orEmpty()

const val SWEDISH = "sv"
const val ENGLISH = "en"

/** The languages every text must have. */
val REQUIRED_LANGUAGES = listOf(SWEDISH, ENGLISH)

/** A link to an authority or service, referred to by [id] from the templates. */
@Serializable
data class Link(
    val id: String,
    val org: String,
    val kind: LinkKind,
    /** Null only for [municipality] links, whose address follows from the user's municipality. */
    val url: String? = null,
    /** The organisation's start page, used if [url] stops working. */
    val fallbackUrl: String? = null,
    val title: Localized,
    /** What to search for on the site, shown as "search for …". */
    val searchHint: Localized = emptyMap(),
    val municipality: Boolean = false,
    /** The day someone checked that the link works and says what it should; null until checked. */
    val verifiedAt: String? = null,
)

@Serializable
enum class LinkKind {
    /** A government agency. */
    @SerialName("authority")
    Authority,

    /** A municipality, region or other public body. */
    @SerialName("public")
    Public,

    /** Impartial consumer guidance. */
    @SerialName("consumer")
    Consumer,

    /** A company. Used sparingly, never paid for, never the only way. */
    @SerialName("service")
    Service,
}

@Serializable
data class LinkRegistry(val links: List<Link>)

/**
 * The shelf a template belongs to in the gallery. The categories are fixed in the app so their names can be
 * translated; a new one needs a string as well as an entry here.
 */
@Serializable
enum class TemplateCategory {
    @SerialName("home")
    Home,

    @SerialName("family")
    Family,

    @SerialName("work-study")
    WorkStudy,

    @SerialName("health")
    Health,

    @SerialName("life-events")
    LifeEvents,

    @SerialName("finance-admin")
    FinanceAdmin,
}

@Serializable
data class Template(
    val id: String,
    val version: Int,
    /** The day the facts were last checked against the sources; null until reviewed. */
    val reviewedAt: String? = null,
    val title: Localized,
    val category: TemplateCategory,
    /** One or two sentences for the gallery: what this project is. */
    val description: Localized = emptyMap(),
    /** A short summary of what the template helps with, shown once it is chosen. */
    val helps: Localized = emptyMap(),
    /** A gentle extra note for templates whose facts carry legal, medical or money weight. */
    val note: Localized? = null,
    val lifestages: List<String> = emptyList(),
    val anchors: List<TemplateAnchor> = emptyList(),
    val questions: List<Question> = emptyList(),
    val phases: List<TemplatePhase> = emptyList(),
    /** Steps far ahead that outlive the chapter, such as the tax return next spring. */
    val longThreads: List<TemplateStep> = emptyList(),
)

@Serializable
data class TemplateAnchor(val id: String, val label: Localized, val required: Boolean = false)

/** A question that shapes the plan: [options] to choose from, or yes or no when [type] is [QuestionType.YesNo]. */
@Serializable
data class Question(
    val id: String,
    val text: Localized,
    val type: QuestionType = QuestionType.Choice,
    val options: List<Option> = emptyList(),
)

@Serializable
enum class QuestionType {
    @SerialName("choice")
    Choice,

    @SerialName("bool")
    YesNo,
}

@Serializable
data class Option(val id: String, val label: Localized)

@Serializable
data class TemplatePhase(val id: String, val title: Localized, val steps: List<TemplateStep>)

@Serializable
data class TemplateStep(
    val id: String,
    val title: Localized,
    val why: Localized? = null,
    val at: TemplateTime? = null,
    val hardDeadline: TemplateTime? = null,
    val links: List<String> = emptyList(),
    val photoPrompt: Localized? = null,
    /** Hides the step when the answers say it doesn't apply. */
    @SerialName("when") val condition: Condition? = null,
    val retired: Boolean = false,
)

/** `{anchor, offsetDays}`, `{anchor, offsetMonths}` or `{anchor, nextYear: {month}}`: exactly one of the three. */
@Serializable
data class TemplateTime(
    val anchor: String,
    val offsetDays: Int? = null,
    val offsetMonths: Int? = null,
    val nextYear: MonthOfYear? = null,
)

@Serializable
data class MonthOfYear(val month: Int)

/**
 * When a step applies, from the answer to [question]: it [equals] an option, is [isIn] a set of options, or is
 * yes or no ([isTrue]). Deliberately simple, so a template can be read and reviewed like text.
 */
@Serializable
data class Condition(
    val question: String,
    val equals: String? = null,
    @SerialName("in") val isIn: List<String>? = null,
    val isTrue: Boolean? = null,
)
