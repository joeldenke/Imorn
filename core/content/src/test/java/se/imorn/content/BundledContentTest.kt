package se.imorn.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import se.imorn.model.dateOf
import se.imorn.model.next
import java.io.File
import java.time.LocalDate

/** The pack in content/, as the app will ship it: it parses, validates, and the moving template plans sensibly. */
class BundledContentTest {
    private val pack: ContentPack by lazy {
        val root = File(checkNotNull(System.getProperty("imorn.content")) { "imorn.content is not set" })
        ContentPack.parse(
            linksJson = File(root, "links.json").readText(),
            templateJsons = File(root, "templates").listFiles { file -> file.extension == "json" }.orEmpty()
                .sortedBy { it.name }
                .map { it.readText() },
        )
    }

    @Test fun `the pack is valid`() {
        assertEquals(emptyList<String>(), ContentValidator.validate(pack))
    }

    @Test fun `every template is in the pack under its file name`() {
        assertNotNull(pack.template("move"))
    }

    @Test fun `moving from a rented flat without kids`() {
        val chapter = pack.template("move")!!.instantiate(
            chapterId = "c1",
            language = SWEDISH,
            answers = mapOf("housing" to Answer.Choice("rental"), "kids" to Answer.YesNo(false)),
            anchorDates = mapOf("moveDay" to LocalDate.of(2027, 3, 1)),
            today = LocalDate.of(2026, 10, 21),
        )
        val ids = chapter.steps.map { it.id }
        assertTrue("terminate-lease" in ids && "move-out-inspection" in ids && "deposit-back" in ids)
        assertFalse("school-place" in ids)
        assertEquals("Flytta", chapter.title)
        // Three months before the moving day comes first.
        assertEquals("terminate-lease", chapter.next()?.step?.id)
        val address = chapter.steps.first { it.id == "address-change" }
        assertEquals("Gör flyttanmälan", address.title)
        assertEquals(LocalDate.of(2027, 2, 15), chapter.dateOf(address))
    }

    @Test fun `moving from a house in English hides the rental steps`() {
        val chapter = pack.template("move")!!.instantiate(
            chapterId = "c2",
            language = ENGLISH,
            answers = mapOf("housing" to Answer.Choice("house")),
            anchorDates = emptyMap(),
            today = LocalDate.of(2026, 10, 21),
        )
        val ids = chapter.steps.map { it.id }
        assertFalse("terminate-lease" in ids || "move-out-inspection" in ids || "deposit-back" in ids)
        assertEquals("Moving", chapter.title)
        assertEquals("Moving day", chapter.anchors.single().label)
        assertTrue(chapter.steps.all { chapter.dateOf(it) == null })
    }
}
