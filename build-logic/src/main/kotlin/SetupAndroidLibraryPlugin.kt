import com.android.build.api.dsl.LibraryExtension
import com.android.build.gradle.ProguardFiles.getDefaultProguardFile
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.analysis.api.components.compile

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
        }

        buildTypes {
            getByName("release") {
                isMinifyEnabled = true
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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

