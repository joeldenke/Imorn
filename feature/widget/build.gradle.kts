plugins {
    alias(libs.plugins.imorn.android.library)
    alias(libs.plugins.imorn.android.compose)
    alias(libs.plugins.imorn.android.unittest)
}

imorn {
    metro()
    activity()
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.designsystem)
    implementation(projects.core.model)
    implementation(projects.core.ui)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)
}
