import org.gradle.api.Plugin
import org.gradle.api.Project

class CommonVerificationsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.setupDetekt()
    }
}
