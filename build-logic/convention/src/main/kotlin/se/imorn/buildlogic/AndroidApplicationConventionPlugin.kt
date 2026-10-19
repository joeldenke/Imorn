package se.imorn.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * `imorn.android.application`: the app module. Everything about the APK is decided here, so `app/build.gradle.kts`
 * only lists plugins and dependencies: application id, version, signing, build types, locales, lint over all
 * modules, and the badging check of the built APK (see Badging.kt).
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            imornExtension()

            extensions.configure<ApplicationExtension> {
                configureAndroid(this)
                namespace = APPLICATION_ID
                defaultConfig {
                    applicationId = APPLICATION_ID
                    targetSdk = libs.intVersion("targetSdk")
                    // Set by CI (VERSION_CODE = workflow run number) so every Firebase distribution gets a unique,
                    // increasing number. 1 for local builds.
                    val versionCode = providers.environmentVariable("VERSION_CODE").orNull?.toIntOrNull() ?: 1
                    this.versionCode = versionCode
                    versionName = "$VERSION_NAME_PREFIX.$versionCode"
                }
                configureSigning(this)
                buildTypes {
                    getByName("debug") {
                        applicationIdSuffix = ".debug"
                        versionNameSuffix = "-debug"
                    }
                    getByName("release") {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                        signingConfig = signingConfigs.findByName("release")
                    }
                }
                androidResources {
                    // Per-app language support (Android 13+), generated from the res/values-* folders.
                    generateLocaleConfig = true
                    // Imorn ships in English for now; add locales here when translations land.
                    localeFilters += listOf("en")
                }
                packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
                lint {
                    // One lint run for the app and every module it depends on: unused resources and the like are
                    // only right when lint sees all modules together. CI runs :app:lintDebug.
                    checkDependencies = true
                    checkReleaseBuilds = true
                }
            }
            extensions.configure<ApplicationAndroidComponentsExtension> {
                disableUnusedTests()
                configureBadging(this)
            }
        }
    }

    /**
     * Debug builds use the key checked into the repo (same pattern as Google's Now in Android) so every debug build
     * is signed identically; it is not secret and never used for releases. The release key comes only from the
     * environment (CI secrets); without it the release build is unsigned, which is fine locally.
     */
    private fun Project.configureSigning(android: ApplicationExtension) {
        val env = { name: String -> providers.environmentVariable(name).orNull?.takeIf { it.isNotBlank() } }
        val keystore = env("IMORN_RELEASE_KEYSTORE")
        val storePassword = env("IMORN_RELEASE_STORE_PASSWORD")
        val keyAlias = env("IMORN_RELEASE_KEY_ALIAS")
        val keyPassword = env("IMORN_RELEASE_KEY_PASSWORD")
        android.signingConfigs {
            getByName("debug") {
                storeFile = file("debug.keystore")
                this.storePassword = "android"
                this.keyAlias = "androiddebugkey"
                this.keyPassword = "android"
            }
            if (keystore != null && storePassword != null && keyAlias != null && keyPassword != null) {
                create("release") {
                    storeFile = file(keystore)
                    this.storePassword = storePassword
                    this.keyAlias = keyAlias
                    this.keyPassword = keyPassword
                }
            }
        }
    }

    private companion object {
        const val APPLICATION_ID = "se.imorn"

        /** Change only at larger milestones; the last part is the version code. */
        const val VERSION_NAME_PREFIX = "0.1"
    }
}
