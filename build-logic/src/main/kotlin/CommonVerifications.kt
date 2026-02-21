import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

internal fun Project.setupDetekt() {
    pluginManager.apply("io.gitlab.arturbosch.detekt")

    extensions.configure<DetektExtension> {
        config.setFrom("$rootDir/config/detekt.yml")
        source.setFrom(fileTree("src") {
            include("**/*.kt")
        })

        tasks.withType<Detekt>().configureEach {
            jvmTarget = "17"
            exclude { it.file.absolutePath.contains("/build/generated") }
        }

        afterEvaluate {
            tasks.named("check").configure {
                setDependsOn(dependsOn.filterNot { it is TaskProvider<*> && it.name == "detekt" })
                dependsOn(tasks.withType<Detekt>())
            }
        }
    }
}

internal fun Project.setupSpotless() {
    pluginManager.apply("com.diffplug.spotless")

    extensions.configure<SpotlessExtension> {
        lineEndings = LineEnding.UNIX

        kotlin {
            target("src/*/kotlin/**/*.kt")

            ktlint(libs().getLibrary("ktlint").get().version)
                .editorConfigOverride(
                    mapOf(
                        "ktlint_standard_filename" to "disabled",
                        "ktlint_standard_class-naming" to "disabled",
                        "ktlint_standard_function-naming" to "disabled",
                        "ktlint_standard_property-naming" to "disabled",
                        "ktlint_standard_discouraged-comment-location" to "disabled",
                        "ktlint_standard_no-empty-file" to "disabled",
                        "ktlint_standard_backing-property-naming" to "disabled",
                        "ktlint_standard_binary-expression-wrapping" to "disabled",
                        "ktlint_standard_chain-method-continuation" to "disabled",
                        "ktlint_standard_class-signature" to "disabled",
                        "ktlint_standard_condition-wrapping" to "disabled",
                        "ktlint_standard_function-expression-body" to "disabled",
                        "ktlint_standard_function-literal" to "disabled",
                        "ktlint_standard_function-type-modifier-spacing" to "disabled",
                        "ktlint_standard_multiline-loop" to "disabled",
                    )
                )
        }
    }
}
