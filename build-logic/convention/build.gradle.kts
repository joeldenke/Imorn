import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "se.imorn.buildlogic"

// Targets the JDK that runs Gradle, not what runs on the device.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    // compileOnly: the root build puts these plugins on the classpath (apply false), so every
    // module shares one copy. AGP is compiled against its public API only. Roborazzi and KSP are
    // applied by id and configured by name, so they need no compile dependency.
    compileOnly(libs.android.gradle.api)
    compileOnly(libs.compose.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.kover.gradle.plugin)
    compileOnly(libs.room.gradle.plugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.imorn.android.application.get().pluginId
            implementationClass = "se.imorn.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.imorn.android.library.get().pluginId
            implementationClass = "se.imorn.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = libs.plugins.imorn.android.feature.get().pluginId
            implementationClass = "se.imorn.buildlogic.AndroidFeatureConventionPlugin"
        }
        register("androidCompose") {
            id = libs.plugins.imorn.android.compose.get().pluginId
            implementationClass = "se.imorn.buildlogic.AndroidComposeConventionPlugin"
        }
        register("androidUnitTest") {
            id = libs.plugins.imorn.android.unittest.get().pluginId
            implementationClass = "se.imorn.buildlogic.AndroidUnitTestConventionPlugin"
        }
        register("androidScreenshot") {
            id = libs.plugins.imorn.android.screenshot.get().pluginId
            implementationClass = "se.imorn.buildlogic.AndroidScreenshotConventionPlugin"
        }
        register("jvmLibrary") {
            id = libs.plugins.imorn.jvm.library.get().pluginId
            implementationClass = "se.imorn.buildlogic.JvmLibraryConventionPlugin"
        }
    }
}
