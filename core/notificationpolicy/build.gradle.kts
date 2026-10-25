plugins {
    alias(libs.plugins.imorn.jvm.library)
}

dependencies {
    // Chapters become reminders through Chapter.reminders().
    api(projects.core.model)
}
