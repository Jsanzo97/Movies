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
