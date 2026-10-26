package se.imorn.content

/**
 * Checks a content pack before it ships: what the JSON format alone cannot say. Every problem is a line a person
 * can act on, naming the template, step or link. An empty list means the pack is good.
 */
object ContentValidator {
    fun validate(pack: ContentPack): List<String> = buildList {
        duplicates(pack.links.map { it.id }).forEach { add("Link $it appears more than once") }
        pack.links.forEach { link -> addAll(link.problems()) }
        duplicates(pack.templates.map { it.id }).forEach { add("Template $it appears more than once") }
        val linkIds = pack.links.map { it.id }.toSet()
        pack.templates.forEach { template -> addAll(template.problems(linkIds)) }
    }

    private fun Link.problems(): List<String> = buildList {
        val where = "Link $id"
        if (url == null && !municipality) add("$where has no url")
        listOfNotNull(url, fallbackUrl).filterNot { it.startsWith("https://") }.forEach {
            add("$where: $it is not https")
        }
        addAll(title.missing("$where title"))
        if (searchHint.isNotEmpty()) addAll(searchHint.missing("$where searchHint"))
    }

    private fun Template.problems(linkIds: Set<String>): List<String> = buildList {
        val where = "Template $id"
        if (version < 1) add("$where: version must be 1 or more")
        addAll(title.missing("$where title"))
        addAll(description.missing("$where description"))
        addAll(helps.missing("$where helps"))
        note?.let { addAll(it.missing("$where note")) }
        duplicates(anchors.map { it.id }).forEach { add("$where: anchor $it appears more than once") }
        anchors.forEach { addAll(it.label.missing("$where anchor ${it.id}")) }
        duplicates(questions.map { it.id }).forEach { add("$where: question $it appears more than once") }
        questions.forEach { addAll(it.problems(where)) }
        duplicates(phases.map { it.id }).forEach { add("$where: phase $it appears more than once") }
        phases.forEach { phase ->
            addAll(phase.title.missing("$where phase ${phase.id}"))
            if (phase.steps.isEmpty()) add("$where: phase ${phase.id} has no steps")
        }
        val steps = phases.flatMap { it.steps } + longThreads
        duplicates(steps.map { it.id }).forEach { add("$where: step $it appears more than once") }
        val anchorIds = anchors.map { it.id }.toSet()
        steps.forEach { addAll(it.problems("$where step ${it.id}", anchorIds, linkIds, questions)) }
        longThreads.filter { it.at == null }.forEach { add("$where: long thread ${it.id} has no time") }
    }

    private fun Question.problems(template: String): List<String> = buildList {
        val where = "$template question $id"
        addAll(text.missing(where))
        when (type) {
            QuestionType.Choice -> {
                if (options.size < 2) add("$where needs at least two options")
                duplicates(options.map { it.id }).forEach { add("$where: option $it appears more than once") }
                options.forEach { addAll(it.label.missing("$where option ${it.id}")) }
            }

            QuestionType.YesNo -> if (options.isNotEmpty()) add("$where is yes or no, so it has no options")
        }
    }

    private fun TemplateStep.problems(
        where: String,
        anchorIds: Set<String>,
        linkIds: Set<String>,
        questions: List<Question>,
    ): List<String> = buildList {
        addAll(title.missing("$where title"))
        why?.let { addAll(it.missing("$where why")) }
        photoPrompt?.let { addAll(it.missing("$where photoPrompt")) }
        listOfNotNull(at?.let { "at" to it }, hardDeadline?.let { "hardDeadline" to it }).forEach { (name, time) ->
            addAll(time.problems("$where $name", anchorIds))
        }
        links.filterNot { it in linkIds }.forEach { add("$where links to $it, which is not in the registry") }
        condition?.let { addAll(it.problems("$where when", questions)) }
    }

    private fun TemplateTime.problems(where: String, anchorIds: Set<String>): List<String> = buildList {
        if (anchor !in anchorIds) add("$where counts from $anchor, which is not an anchor")
        val kinds = listOfNotNull(offsetDays, offsetMonths, nextYear).size
        if (kinds != 1) add("$where needs exactly one of offsetDays, offsetMonths and nextYear")
        nextYear?.let { if (it.month !in 1..12) add("$where: month ${it.month} is not 1-12") }
    }

    private fun Condition.problems(where: String, questions: List<Question>): List<String> = buildList {
        val question = questions.firstOrNull { it.id == this@problems.question }
        if (question == null) {
            add("$where asks about ${this@problems.question}, which is not a question")
            return@buildList
        }
        val tests = listOfNotNull(equals, isIn, isTrue).size
        if (tests != 1) add("$where needs exactly one of equals, in and isTrue")
        when (question.type) {
            QuestionType.Choice -> {
                if (isTrue != null) add("$where uses isTrue on a choice")
                val options = question.options.map { it.id }.toSet()
                (listOfNotNull(equals) + isIn.orEmpty()).filterNot { it in options }.forEach {
                    add("$where: $it is not an option of ${question.id}")
                }
            }

            QuestionType.YesNo -> if (equals != null || isIn != null) add("$where: use isTrue on a yes-or-no question")
        }
    }

    private fun Localized.missing(where: String): List<String> =
        REQUIRED_LANGUAGES.filter { get(it).isNullOrBlank() }.map { "$where has no $it text" }

    private fun duplicates(ids: List<String>): Set<String> =
        ids.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
}
