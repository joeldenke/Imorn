plugins {
    alias(libs.plugins.imorn.android.library)
    alias(libs.plugins.imorn.android.compose)
    alias(libs.plugins.imorn.android.unittest)
}

dependencies {
    implementation(projects.core.designsystem)
    // The camera, Photo Picker and file picker (rememberCapture), and the FileProvider the camera writes through.
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    // The chapter in PreviewData.
    api(projects.core.model)
}
