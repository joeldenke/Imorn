plugins {
    alias(libs.plugins.imorn.android.application)
    alias(libs.plugins.imorn.android.compose)
    alias(libs.plugins.imorn.android.unittest)
}

imorn {
    activity()
    navigation()
    // The Metro graph (AppGraph) and the ViewModel factory every entry uses.
    viewModels()
}

dependencies {
    implementation(projects.feature.today)

    // Everything that contributes to the graph must be on the app's classpath, so Metro can find it.
    implementation(projects.core.coach)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.core.navigation)
    implementation(projects.core.ui)

    implementation(libs.androidx.navigation3.ui)
}
