plugins {
    alias(libs.plugins.imorn.android.library)
    alias(libs.plugins.imorn.android.compose)
    alias(libs.plugins.imorn.android.unittest)
}

imorn {
    activity()
    coroutines()
}

dependencies {
    implementation(projects.core.database)
    implementation(projects.core.model)
    // The camera file the quick photo writes into (newCaptureUriFor) and the shared FileProvider.
    implementation(projects.core.ui)
    // lifecycleScope in WidgetPhotoActivity, to keep the photo off the main thread.
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.glance.appwidget)
}
