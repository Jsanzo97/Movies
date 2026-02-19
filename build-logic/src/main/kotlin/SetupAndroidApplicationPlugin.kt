@file:Suppress("UnstableApiUsage")

import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.provider.Provider
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class SetupAndroidApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.apply()
    }
}

private fun Project.apply() {
    pluginManager.apply("com.android.application")
    pluginManager.apply("kotlin-android")

    extensions.configure<ApplicationExtension>("android") {
        namespace = "com.example.movielist"
        compileSdk = 36

        defaultConfig {
            minSdk = 24
            targetSdk = 36
            versionCode = 1
            versionName = "1.0.0"
        }

        buildFeatures {
            buildConfig = true
            resValues = true
        }

        compileOptions {
            isCoreLibraryDesugaringEnabled = true
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }
    }
}