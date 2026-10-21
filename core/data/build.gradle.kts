plugins {
    alias(libs.plugins.imorn.android.library)
    alias(libs.plugins.imorn.android.unittest)
}

imorn {
    coroutines()
    metro()
}

dependencies {
    api(projects.core.model)
    api(projects.core.coach)
    implementation(libs.androidx.datastore.preferences)
    // Every preference file is sealed with a key from the Android Keystore: datastore-tink's
    // AeadSerializer plus Tink itself (Aead, AndroidKeysetManager). See EncryptingPreferencesSerializer.
    implementation(libs.androidx.datastore.tink)
    implementation(libs.tink.android)
    // Gemini Nano on the device, for the assistant's message. See GeminiNanoLanguageModel.
    implementation(libs.mlkit.genai.prompt)
}
