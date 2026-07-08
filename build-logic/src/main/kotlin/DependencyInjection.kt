import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.configure

internal fun Project.setupKoin() {
    pluginManager.apply("io.insert-koin.compiler.plugin")

    val isApp = pluginManager.hasPlugin("com.android.application")

    extensions.findByName("koinCompiler")?.let { extension ->
        val method = extension.javaClass.getMethod("getCompileSafety")
        @Suppress("UNCHECKED_CAST")
        val property = method.invoke(extension) as? org.gradle.api.provider.Property<Boolean>
        property?.set(isApp)
    }

    dependencies {
        "implementation"(platform(libs().getLibrary("koin.bom")))
        "implementation"(libs().getLibrary("koin.core"))
        "implementation"(libs().getLibrary("koin.android"))
        "implementation"(libs().getLibrary("koin.annotations"))
    }
}

internal fun Project.setupArrow() {
    dependencies {
        "implementation"(libs().getLibrary("arrow.core"))
    }
}
