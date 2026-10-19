package se.imorn.coach

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssistantPromptTest {
    private val request = AssistantRequest(
        language = "English",
        goal = "Move to a new apartment in October",
        facts = listOf("Book the moving van", "Cancel the old utilities"),
    )

    @Test
    fun build_includesGoalAndFactsInTheLanguage() {
        val prompt = AssistantPrompt.build(request)
        assertTrue(prompt.contains("Move to a new apartment in October"))
        assertTrue(prompt.contains("Book the moving van"))
        assertTrue(prompt.contains("English"))
    }

    @Test
    fun clean_removesMarkdownAndEmojis() {
        val cleaned = AssistantPrompt.clean("**Great** start \uD83D\uDE0A — keep going!")
        assertFalse(cleaned.contains("*"))
        assertFalse(cleaned.contains("\uD83D\uDE0A"))
        assertEquals("Great start — keep going!", cleaned)
    }

    @Test
    fun clean_collapsesWhitespace() {
        assertEquals("one two", AssistantPrompt.clean("one\n\n  two"))
    }

    @Test
    fun clean_keepsWholeSentencesUpToTheLimit() {
        val long = ("A sentence. ").repeat(100)
        val cleaned = AssistantPrompt.clean(long)
        assertTrue(cleaned.length <= AssistantPrompt.MAX_LENGTH)
        assertTrue(cleaned.endsWith("."))
    }
}
