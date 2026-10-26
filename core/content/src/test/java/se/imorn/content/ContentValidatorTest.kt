package se.imorn.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentValidatorTest {
    private val both = mapOf(SWEDISH to "Text", ENGLISH to "Text")
    private val link = Link(
        id = "skatteverket.flytt",
        org = "Skatteverket",
        kind = LinkKind.Authority,
        url = "https://www.skatteverket.se/",
        title = both,
    )
    private val step = TemplateStep(
        id = "address",
        title = both,
        at = TemplateTime("moveDay", offsetDays = -14),
        links = listOf("skatteverket.flytt"),
    )
    private val template = Template(
        id = "move",
        version = 1,
        title = both,
        summary = both,
        category = TemplateCategory.Home,
        helps = both,
        anchors = listOf(TemplateAnchor("moveDay", both)),
        questions = listOf(
            Question("housing", both, options = listOf(Option("rental", both), Option("house", both))),
            Question("kids", both, type = QuestionType.YesNo),
        ),
        phases = listOf(TemplatePhase("papers", both, listOf(step))),
    )

    private fun problems(template: Template = this.template, links: List<Link> = listOf(link)) =
        ContentValidator.validate(ContentPack(links, listOf(template)))

    private fun withStep(step: TemplateStep) =
        template.copy(phases = listOf(TemplatePhase("papers", both, listOf(step))))

    @Test fun `a good pack has no problems`() {
        assertEquals(emptyList<String>(), problems())
    }

    @Test fun `a missing language is named`() {
        val found = problems(withStep(step.copy(title = mapOf(SWEDISH to "Gör flyttanmälan"))))
        assertEquals(listOf("Template move step address title has no en text"), found)
    }

    @Test fun `the gallery texts are required`() {
        val found = problems(template.copy(summary = emptyMap(), helps = emptyMap()))
        assertEquals(
            listOf(
                "Template move summary has no sv text",
                "Template move summary has no en text",
                "Template move helps has no sv text",
                "Template move helps has no en text",
            ),
            found,
        )
    }

    @Test fun `a note must be in both languages`() {
        assertEquals(
            listOf("Template move note has no en text"),
            problems(template.copy(note = mapOf(SWEDISH to "Bara en anteckning"))),
        )
    }

    @Test fun `links must be in the registry`() {
        assertEquals(
            listOf("Template move step address links to skatteverket.flytt, which is not in the registry"),
            problems(links = emptyList()),
        )
    }

    @Test fun `times count from a real anchor with exactly one offset`() {
        val found = problems(withStep(step.copy(at = TemplateTime("handover", offsetDays = 1, offsetMonths = 1))))
        assertEquals(2, found.size)
        assertTrue(found.any { "handover, which is not an anchor" in it })
        assertTrue(found.any { "exactly one of offsetDays" in it })
    }

    @Test fun `conditions must match their question`() {
        val wrongOption = problems(withStep(step.copy(condition = Condition("housing", equals = "castle"))))
        assertEquals(listOf("Template move step address when: castle is not an option of housing"), wrongOption)
        val wrongKind = problems(withStep(step.copy(condition = Condition("kids", equals = "yes"))))
        assertEquals(listOf("Template move step address when: use isTrue on a yes-or-no question"), wrongKind)
        val unknown = problems(withStep(step.copy(condition = Condition("pets", isTrue = true))))
        assertEquals(listOf("Template move step address when asks about pets, which is not a question"), unknown)
    }

    @Test fun `ids are unique`() {
        val twice = template.copy(phases = listOf(TemplatePhase("papers", both, listOf(step, step))))
        assertEquals(listOf("Template move: step address appears more than once"), problems(twice))
    }

    @Test fun `links are https`() {
        assertEquals(
            listOf("Link skatteverket.flytt: http://skatteverket.se is not https"),
            problems(links = listOf(link.copy(url = "http://skatteverket.se"))),
        )
    }
}
