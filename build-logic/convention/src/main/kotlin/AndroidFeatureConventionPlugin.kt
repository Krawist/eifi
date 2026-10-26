import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** Screen module: Android library + Compose + design system. */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("eifi.android.library")
            pluginManager.apply("eifi.android.compose")
            dependencies {
                add("implementation", project(":core:design"))
            }
        }
    }
}
