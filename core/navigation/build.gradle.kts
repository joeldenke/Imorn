plugins {
    alias(libs.plugins.imorn.android.library)
    alias(libs.plugins.imorn.android.compose)
    alias(libs.plugins.imorn.android.unittest)
}

imorn {
    // A ViewModelStore per entry (rememberViewModelStoreNavEntryDecorator), see NavigationState.toEntries.
    lifecycle()
}

dependencies {
    api(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
}
