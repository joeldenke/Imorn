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
    implementation(projects.feature.chapters)
    implementation(projects.feature.onboarding)
    implementation(projects.feature.timeline)
    implementation(projects.feature.today)
    implementation(projects.feature.widget)

    // Everything that contributes to the graph must be on the app's classpath, so Metro can find it.
    implementation(projects.core.coach)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.designsystem)
    implementation(projects.core.model)
    implementation(projects.core.navigation)
    implementation(projects.core.ui)

    implementation(libs.androidx.navigation3.ui)

    // Firebase platform BoM: pins the versions of the Firebase SDKs added under it. No service SDK is declared
    // yet — Imorn only uses Firebase App Distribution from CI, which needs none. There is deliberately no
    // google-services.json or google-services plugin; see docs/firebase.md before adding a service SDK.
    implementation(platform(libs.firebase.bom))
}
