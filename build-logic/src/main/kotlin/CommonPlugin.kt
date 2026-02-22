import org.gradle.api.Plugin
import org.gradle.api.Project

class CommonSetupPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.setupDetekt()
        target.setupSpotless()
        target.setupJunitTests()
        target.setupCheck()
        target.setupKoin()
    }
}
