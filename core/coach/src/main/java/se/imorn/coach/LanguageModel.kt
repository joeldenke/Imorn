package se.imorn.coach

/** Whether the on-device language model can be used, as the system reports it. */
enum class AssistantAvailability { Unknown, Unavailable, Downloadable, Downloading, Available }

/**
 * A language model that runs on the device (Gemini Nano through AICore). Imorn's plan never comes from it: the
 * model only puts the facts it is given into a few personal sentences. Implementations never throw, apart from
 * cancellation.
 */
interface LanguageModel {
    suspend fun availability(): AssistantAvailability

    /** Asks the system to download the model. True once it is there. */
    suspend fun download(): Boolean

    suspend fun write(prompt: String): Result<String>
}

/**
 * What a prompt needs that only the UI knows: the user's [language], the [goal] of the current project and the
 * [facts] (the steps and their state) the model may talk about, all already localized.
 */
data class AssistantRequest(val language: String, val goal: String, val facts: List<String>)

/**
 * Builds the prompt for the assistant's message and cleans the answer. Plain Kotlin, so it is unit tested. The
 * prompt holds the plan as facts and asks for a short, calm explanation in the user's language, without changing
 * the plan, giving advice outside the app or using emojis (Imorn shows none).
 */
object AssistantPrompt {
    const val MAX_LENGTH = 700

    fun build(request: AssistantRequest): String = """
        |You are a calm, encouraging assistant in the personal project app Imorn.
        |The user's goal: ${request.goal}.
        |What is already known (do not invent anything else):
        |${request.facts.joinToString("\n") { "- $it" }}
        |Write 3 or 4 short sentences in ${request.language} to the user: what the project is about, what the next
        |step is and why, and one practical tip. Only use the facts above; do not add or change anything.
        |No lists, headings, markdown or emojis.
    """.trimMargin()

    /** The model's answer as plain text: no emojis or markdown, at most [MAX_LENGTH] characters, whole sentences. */
    fun clean(answer: String): String {
        val plain = buildString {
            var i = 0
            while (i < answer.length) {
                val codePoint = answer.codePointAt(i)
                if (!isEmoji(codePoint)) appendCodePoint(codePoint)
                i += Character.charCount(codePoint)
            }
        }
            .replace(Regex("[*_#`]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
        if (plain.length <= MAX_LENGTH) return plain
        val cut = plain.take(MAX_LENGTH)
        val end = cut.lastIndexOfAny(charArrayOf('.', '!', '?'))
        return if (end > 0) cut.take(end + 1) else cut.trimEnd() + "…"
    }

    private fun isEmoji(codePoint: Int): Boolean =
        codePoint in 0x1F000..0x1FAFF ||
            codePoint in 0x2600..0x27BF ||
            codePoint in 0xFE00..0xFE0F ||
            codePoint == 0x200D
}
