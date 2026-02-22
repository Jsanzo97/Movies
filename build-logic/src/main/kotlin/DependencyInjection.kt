import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import com.google.devtools.ksp.gradle.KspExtension

internal fun Project.setupKoin() {
    pluginManager.apply("com.google.devtools.ksp")

    extensions.configure<KspExtension>("ksp") {
        arg("KOIN_DEFAULT_MODULE", "false")
    }

    dependencies {
        "implementation"(platform(libs().getLibrary("koin.bom")))
        "implementation"(libs().getLibrary("koin.core"))
        "implementation"(libs().getLibrary("koin.android"))
        "implementation"(libs().getLibrary("koin.annotations"))
        "ksp"(libs().getLibrary("koin.ksp.compiler"))

    }
}

internal fun Project.setupArrow() {
    dependencies {
        "implementation"(libs().getLibrary("arrow.core"))
    }
}
