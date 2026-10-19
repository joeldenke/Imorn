package se.imorn.buildlogic

import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * `imorn.android.library`: an Android library with the same SDK, Java and lint setup as the app. The namespace
 * comes from the module path (see [imornNamespace]), so the module's build file needs no `android {}` block.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            imornExtension()

            extensions.configure<LibraryExtension> {
                configureAndroid(this)
                namespace = imornNamespace
                lint.targetSdk = libs.intVersion("targetSdk")
            }
            extensions.configure<LibraryAndroidComponentsExtension> {
                disableUnusedTests()
            }
        }
    }
}
