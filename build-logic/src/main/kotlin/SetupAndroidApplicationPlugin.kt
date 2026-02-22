@file:Suppress("UnstableApiUsage")
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import java.io.File
import com.google.devtools.ksp.gradle.KspExtension

class SetupAndroidApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.apply()
    }
}

private fun Project.apply() {
    pluginManager.apply("com.android.application")
    pluginManager.apply("common-setup")

    extensions.configure<ApplicationExtension>("android") {
        namespace = calculateNamespace()
        compileSdk = sdkCompile

        defaultConfig {
            minSdk = sdkMin
            targetSdk = sdkTarget
            versionCode = versionMajor * 1_000_000 + versionMinor * 1_000 + versionPatch
            versionName = "$versionMajor.$versionMinor.$versionPatch"
        }

        buildFeatures {
            buildConfig = true
            resValues = true
        }

        buildTypes {
            getByName("debug") {
                applicationIdSuffix = ".debug"
            }
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

        packaging {
            resources.excludes.add("META-INF/com.android.tools/proguard/coroutines.pro")
        }

        lint {
            abortOnError = false
        }

        testOptions {
            unitTests.apply {
                isIncludeAndroidResources = true
            }
        }

        sourceSets.apply {
            forEach {
                it.java.srcDir("src/${it.name}/kotlin")
            }
            getByName("main") {
                val addResources: (Array<File>) -> Unit = { files: Array<File> ->
                    files.filter { it.exists() }
                        .mapNotNull { it.listFiles { file: File -> file.isDirectory } }
                        .forEach { folders -> res.srcDirs(*folders) }
                }
                val resScreens = file("src/main/res-screens")

                addResources(arrayOf(resScreens))
            }
        }
    }

    extensions.configure<KspExtension>("ksp") {
        arg("KOIN_CONFIG_CHECK", "true")
        arg("KOIN_DEFAULT_MODULE", "false")
    }

    dependencies {
        "coreLibraryDesugaring"(libs().getLibrary("desugar-jdk"))
    }
}


