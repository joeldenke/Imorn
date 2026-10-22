plugins {
    alias(libs.plugins.imorn.android.feature)
}

imorn {
    viewModels()
}

dependencies {
    implementation(projects.core.data)
}
