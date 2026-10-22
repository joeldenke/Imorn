plugins {
    alias(libs.plugins.imorn.android.library)
    alias(libs.plugins.imorn.android.compose)
    alias(libs.plugins.imorn.android.unittest)
}

dependencies {
    // api: the public API exposes Compose UI and Foundation types (Modifier, TextStyle, shapes, layouts).
    api(platform(libs.compose.bom))
    api(libs.compose.foundation)
    api(libs.compose.ui)
    // implementation: Material 3 stays inside this module. ImornTheme and the Imorn* components expose only Imorn
    // types, so feature code can't import Material and it can be replaced component by component.
    implementation(libs.compose.material3)
    // Glass panels over the dawn scene (ImornDawnScene): Haze captures the scene and blurs it behind each panel.
    implementation(libs.haze)
    implementation(libs.haze.blur)
    // The scene's light status bar icons (WindowCompat).
    implementation(libs.androidx.core.ktx)
}
