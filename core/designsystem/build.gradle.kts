plugins {
    alias(libs.plugins.imorn.android.library)
    alias(libs.plugins.imorn.android.compose)
    alias(libs.plugins.imorn.android.unittest)
}

dependencies {
    // api: ImornTheme exposes Material 3 types (ColorScheme, Typography, Shapes) and Compose UI
    // types in its public API, so consumers need them on their compile classpath.
    api(platform(libs.compose.bom))
    api(libs.compose.material3)
    api(libs.compose.ui)
}
