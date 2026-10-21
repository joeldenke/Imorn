plugins {
    alias(libs.plugins.imorn.jvm.library)
}

imorn {
    serialization(json = true)
    // The tests check the content pack in content/ (templates and the link registry).
    bundledContent()
}

dependencies {
    api(projects.core.model)
}
