package se.imorn.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import se.imorn.model.Chapter
import se.imorn.model.dateOf
import se.imorn.model.deadlineOf
import se.imorn.model.next
import java.io.File
import java.time.LocalDate

/** The pack in content/, as the app will ship it: it parses, validates, and the templates plan sensibly. */
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
        val root = File(checkNotNull(System.getProperty("imorn.content")))
        val names = File(root, "templates").listFiles { file -> file.extension == "json" }.orEmpty()
            .map { it.nameWithoutExtension }
        assertEquals(names.sorted(), pack.templates.map { it.id }.sorted())
    }

    @Test fun `the library starts with moving and every template has its own place`() {
        assertEquals("move", pack.templates.first().id)
        assertEquals(pack.templates.size, pack.templates.map { it.order }.toSet().size)
    }

    @Test fun `every template has a summary, an icon and steps without answers`() {
        pack.templates.forEach { template ->
            assertTrue(template.id, template.summary.isNotEmpty() && template.icon != null)
            val chapter = template.instantiate(
                chapterId = template.id,
                language = SWEDISH,
                answers = emptyMap(),
                anchorDates = emptyMap(),
                today = LocalDate.of(2026, 10, 21),
            )
            assertTrue(template.id, chapter.steps.size >= 5)
        }
    }

    @Test fun `the baby's name has a hard deadline three months after the birth`() {
        val chapter = pack.template("baby")!!.instantiate(
            chapterId = "b",
            language = SWEDISH,
            answers = mapOf("married" to Answer.YesNo(true)),
            anchorDates = mapOf("due" to LocalDate.of(2027, 5, 10), "birth" to LocalDate.of(2027, 5, 14)),
            today = LocalDate.of(2026, 10, 21),
        )
        val ids = chapter.steps.map { it.id }
        assertFalse("parenthood" in ids)
        val name = chapter.steps.first { it.id == "name" }
        assertEquals(LocalDate.of(2027, 8, 14), chapter.deadlineOf(name))
    }

    @Test fun `a new job hides what to do when a job ends`() {
        val chapter = pack.template("job")!!.instantiate(
            chapterId = "j",
            language = ENGLISH,
            answers = mapOf("change" to Answer.Choice("new")),
            anchorDates = emptyMap(),
            today = LocalDate.of(2026, 10, 21),
        )
        assertEquals(listOf("new"), chapter.phases.map { it.id })
    }

    @Test fun `selling a flat waits for the association, not the municipality`() {
        val ids = pack.template("sell-home")!!.instantiate(
            chapterId = "s",
            language = SWEDISH,
            answers = mapOf("kind" to Answer.Choice("condo"), "buying" to Answer.YesNo(false)),
            anchorDates = emptyMap(),
            today = LocalDate.of(2026, 10, 21),
        ).steps.map { it.id }
        assertTrue("association-approval" in ids && "sale-tax-return" in ids)
        assertFalse("water-waste" in ids || "deferral" in ids || "hidden-defects" in ids)
    }

    @Test fun `moving from a rented flat without kids`() {
        val chapter = pack.template("move")!!.instantiate(
            chapterId = "c1",
            language = SWEDISH,
            answers = mapOf(
                "housing" to Answer.Choice("rental"),
                "newHome" to Answer.Choice("rental"),
                "selling" to Answer.YesNo(false),
                "kids" to Answer.YesNo(false),
            ),
            anchorDates = mapOf("moveDay" to LocalDate.of(2027, 3, 1)),
            today = LocalDate.of(2026, 10, 21),
        )
        val ids = chapter.steps.map { it.id }
        assertTrue("terminate-lease" in ids && "move-out-inspection" in ids && "deposit-back" in ids)
        assertFalse("school-place" in ids)
        // Renting to renting: no mortgage, no bank, no estate agent.
        assertTrue(chapter.steps.none { it.phaseId in setOf("mortgage", "bank", "agent") })
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

    private fun move(vararg answers: Pair<String, Answer>) = pack.template("move")!!.instantiate(
        chapterId = "c",
        language = SWEDISH,
        answers = answers.toMap(),
        anchorDates = mapOf("moveDay" to LocalDate.of(2027, 6, 1)),
        today = LocalDate.of(2026, 10, 21),
    )

    private fun Chapter.phaseOf(id: String) = steps.first { it.id == id }.phaseId

    @Test fun `buying a house brings the mortgage, the bank's steps and the title`() {
        val chapter = move("housing" to Answer.Choice("rental"), "newHome" to Answer.Choice("buyHouse"))
        assertEquals("mortgage", chapter.phaseOf("loan-promise"))
        assertEquals("bank", chapter.phaseOf("loan-documents"))
        val ids = chapter.steps.map { it.id }
        assertTrue("house-insurance" in ids && "water-waste-new" in ids && "title-deed" in ids)
        assertFalse("association-approval" in ids)
        // The pre-approval comes before the bank wants the contract, and the loan is paid out on the day.
        assertTrue(chapter.dateOf(chapter.steps.first { it.id == "loan-promise" })!! < LocalDate.of(2027, 3, 1))
        assertEquals(LocalDate.of(2027, 6, 1), chapter.dateOf(chapter.steps.first { it.id == "access-day" }))
    }

    @Test fun `buying a flat asks the association, not the municipality`() {
        val ids = move("newHome" to Answer.Choice("buyCondo")).steps.map { it.id }
        assertTrue("association-approval" in ids && "collateral" in ids)
        assertFalse("water-waste-new" in ids || "house-insurance" in ids || "title-deed" in ids)
    }

    @Test fun `selling brings the estate agent and the tax return`() {
        val chapter = move(
            "housing" to Answer.Choice("house"),
            "newHome" to Answer.Choice("rental"),
            "selling" to Answer.YesNo(true),
        )
        assertEquals("agent", chapter.phaseOf("choose-agent"))
        val ids = chapter.steps.map { it.id }
        assertTrue("sale-tax-return" in ids && "water-waste-old" in ids)
        assertFalse("loan-promise" in ids)
    }

    @Test fun `the subscriptions are a subflow of their own`() {
        val chapter = move("newHome" to Answer.Choice("rental"))
        listOf("electricity", "old-electricity", "broadband", "home-insurance", "other-subscriptions").forEach {
            assertEquals(it, "subscriptions", chapter.phaseOf(it))
        }
    }

    @Test fun `unanswered questions hide nothing`() {
        val ids = move().steps.map { it.id }
        assertTrue("loan-promise" in ids && "choose-agent" in ids && "terminate-lease" in ids)
    }
}
