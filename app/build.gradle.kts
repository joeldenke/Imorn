plugins {
    alias(libs.plugins.imorn.android.application)
    alias(libs.plugins.imorn.android.compose)
    alias(libs.plugins.imorn.android.unittest)
}

// The Google Services plugin reads google-services.json and generates the Firebase resources (google_app_id …)
// from it. That file is per Firebase project and is never committed (see .gitignore and docs/firebase.md), so the
// plugin is applied only when it exists: a fresh clone, or CI before the GOOGLE_SERVICES_JSON secret is set, still
// builds — just without Firebase. Drop the file in app/ (or let CI write it) and it activates.
if (file("google-services.json").exists()) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
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

    // Firebase platform BoM: pins the versions of the Firebase SDKs added under it. No service SDK is declared
    // yet — the ported setup only uses Firebase App Distribution from CI, which needs none. Add e.g.
    // `implementation(libs.firebase.analytics)` here (no version) when a service is wanted; see docs/firebase.md.
    implementation(platform(libs.firebase.bom))
}
