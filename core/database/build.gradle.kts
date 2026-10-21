plugins {
    alias(libs.plugins.imorn.android.library)
    alias(libs.plugins.imorn.android.unittest)
}

imorn {
    coroutines()
    metro()
    room()
}

dependencies {
    api(projects.core.model)
}
