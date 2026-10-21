package se.imorn.buildlogic

import androidx.room.gradle.RoomExtension
import org.gradle.api.Project
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType
import javax.inject.Inject

/**
 * The `imorn { }` block in a module's build file: libraries that several modules use, by what the module does
 * rather than by artifact. Each call adds the right artifacts (and compiler plugins) as `implementation`, with
 * versions from the catalog, so modules only take what they use:
 *
 * ```
 * plugins {
 *     alias(libs.plugins.imorn.android.feature)
 * }
 *
 * imorn {
 *     lifecycle()
 * }
 *
 * dependencies {
 *     implementation(projects.core.coach)
 * }
 * ```
 *
 * Project modules stay in `dependencies {}` with type-safe accessors; this is for external libraries.
 */
abstract class ImornExtension @Inject constructor(private val project: Project) {
    private fun implementation(alias: String) {
        project.dependencies.add("implementation", project.libs.library(alias))
    }

    /** kotlinx.coroutines: suspend functions, Flow and StateFlow. */
    fun coroutines() = implementation("kotlinx-coroutines-core")

    /** `@Serializable` classes: the kotlinx.serialization compiler plugin and runtime, plus JSON when [json]. */
    fun serialization(json: Boolean = false) {
        project.pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
        implementation("kotlinx-serialization-core")
        if (json) implementation("kotlinx-serialization-json")
    }

    /** Navigation 3: `NavKey` routes and entry providers. Every feature has this through `imorn.android.feature`. */
    fun navigation() {
        serialization()
        implementation("androidx-navigation3-runtime")
        project.dependencies.add("implementation", project.module(":core:navigation"))
    }

    /**
     * ViewModels and lifecycle-aware state collection in Compose, and ViewModel stores scoped to Navigation 3
     * entries (`rememberViewModelStoreNavEntryDecorator`).
     */
    fun lifecycle() {
        implementation("androidx-lifecycle-runtime-compose")
        implementation("androidx-lifecycle-viewmodel-compose")
        implementation("androidx-lifecycle-viewmodel-navigation3")
    }

    /**
     * Metro, compile-time dependency injection: the compiler plugin and its runtime (`@Inject`, `@SingleIn`,
     * `@ContributesBinding` …). Every module with injected classes needs it, so Metro generates their factories.
     */
    fun metro() {
        project.pluginManager.apply("dev.zacsweers.metro")
    }

    /**
     * ViewModels created by Metro: `@ViewModelKey @ContributesIntoMap(AppScope::class)` on the ViewModel and
     * `metroViewModel()` in the Navigation 3 entry, so each entry gets its own instance. Includes [metro] and
     * [lifecycle].
     */
    fun viewModels() {
        metro()
        lifecycle()
        coroutines()
        implementation("metrox-viewmodel-compose")
    }

    /**
     * The content pack in the repository's `content/` folder (templates and the link registry, see
     * docs/content-system.md). Tests read it from the folder named by the `imorn.content` system property, and the
     * folder is an input of the test task, so a change to the content reruns them.
     */
    fun bundledContent() {
        val content = project.isolated.rootProject.projectDirectory.dir("content")
        project.tasks.withType<Test>().configureEach {
            inputs.dir(content).withPropertyName("bundledContent").withPathSensitivity(PathSensitivity.RELATIVE)
            systemProperty("imorn.content", content.asFile.absolutePath)
        }
    }

    /**
     * Room: the runtime, its compiler in KSP, and the exported schema in the module's `schemas/` folder. The schema is
     * reviewed like badging: CI fails when the build changes it, and `/record-schemas` records it, so every
     * migration has the schema it starts from.
     */
    fun room() {
        project.pluginManager.apply("com.google.devtools.ksp")
        project.pluginManager.apply("androidx.room")
        project.extensions.configure<RoomExtension> {
            schemaDirectory(project.layout.projectDirectory.dir("schemas").asFile.path)
        }
        implementation("androidx-room-runtime")
        implementation("androidx-room-ktx")
        project.dependencies.add("ksp", project.libs.library("androidx-room-compiler"))
    }

    /** Activities with Compose content (`setContent`). */
    fun activity() = implementation("androidx-activity-compose")
}

/** Registers `imorn { }` once, whichever Imorn plugins the module applies. */
internal fun Project.imornExtension(): ImornExtension =
    extensions.findByType<ImornExtension>() ?: extensions.create<ImornExtension>("imorn", this)
