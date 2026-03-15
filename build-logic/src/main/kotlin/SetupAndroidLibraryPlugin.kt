import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class SetupAndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.apply()
    }
}

private fun Project.apply() {
    pluginManager.apply("com.android.library")
    pluginManager.apply("common-setup")

    extensions.configure<LibraryExtension>("android") {
        namespace = calculateNamespace()
        compileSdk = sdkCompile

        defaultConfig {
            minSdk = sdkMin
            consumerProguardFiles("proguard-rules.pro")
        }

        buildTypes {
            getByName("release") {
                // IMPORTANT: Libraries should NOT minify themselves. 
                // The :app module will handle minification for the entire project.
                isMinifyEnabled = false
            }
        }

        compileOptions {
            isCoreLibraryDesugaringEnabled = true
            sourceCompatibility = JavaVersion.toVersion(javaSourceMin)
            targetCompatibility = JavaVersion.toVersion(jdkVersion)
        }
    }

    dependencies {
        "coreLibraryDesugaring"(libs().getLibrary("desugar-jdk"))
    }
}
