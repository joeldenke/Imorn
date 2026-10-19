plugins {
    alias(libs.plugins.imorn.android.library)
    alias(libs.plugins.imorn.android.unittest)
}

imorn {
    coroutines()
    metro()
}

dependencies {
    api(projects.core.coach)
    // Gemini Nano on the device, for the assistant's message. See GeminiNanoLanguageModel.
    implementation(libs.mlkit.genai.prompt)
}
