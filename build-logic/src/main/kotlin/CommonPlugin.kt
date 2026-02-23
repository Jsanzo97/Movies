import org.gradle.api.Plugin
import org.gradle.api.Project

class CommonSetupPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            setupDetekt()
            setupSpotless()
            setupJunitTests()
            setupCheck()
            setupKoin()
            setupArrow()
            setupSerialization()
        }
    }

    private fun Project.setupSerialization() {
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
    }
}
