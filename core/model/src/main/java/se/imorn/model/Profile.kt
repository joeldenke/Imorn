package se.imorn.model

/**
 * Who uses Imorn, as far as the user chose to tell: the life stages that fit right now. They only decide which
 * templates come first; every template is always there. Nothing else is asked. Saved by `ProfileStore`.
 */
data class Profile(val lifeStages: Set<LifeStage> = emptySet())

/**
 * "What fits you right now?", asked instead of age. [id] is how templates name the stage in `content/` (their
 * `lifestages`), and never changes.
 */
enum class LifeStage(val id: String) {
    /** On your own (about 18–25): the first flat, studies, the first job. */
    OnMyOwn("first-home"),

    /** Building up (about 25–40): buying a home, moving in together, children, renovating. */
    BuildingUp("building"),

    /** Midlife (about 40–60): a bigger home, children moving out, helping parents. */
    Midlife("midlife"),

    /** The next chapter (about 60+): retiring, moving somewhere smaller, a will. */
    NextChapter("next-chapter"),

    /** New in Sweden: registration, ID, bank, healthcare. */
    NewInSweden("new-in-sweden"),

    /** Something hard has happened: a death, a separation, a lost job. Templates for it take a gentler tone. */
    SomethingHard("hard-times"),
    ;

    companion object {
        /** The stage with [id], or null for an unknown one, so an old or broken file never crashes. */
        fun fromId(id: String): LifeStage? = entries.firstOrNull { it.id == id }
    }
}
