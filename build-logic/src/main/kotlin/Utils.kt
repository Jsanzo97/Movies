import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

internal fun Project.calculateNamespace(): String {
    val packageName = path.removePrefix(":").split("-", ":").joinToString(".") {
        if (it == "public") "publicapi" else it
    }
    return if (path == ":app") "jsanzo.movies" else "jsanzo.movies.$packageName"
}

internal fun Project.libs(): VersionCatalog {
    return extensions.getByType<VersionCatalogsExtension>().named("libs")
}

internal val Project.versionMajor: Int get() = libs().findVersion("versionMajor").get().requiredVersion.toInt()
internal val Project.versionMinor: Int get() = libs().findVersion("versionMinor").get().requiredVersion.toInt()
internal val Project.versionPatch: Int get() = libs().findVersion("versionPatch").get().requiredVersion.toInt()
internal val Project.sdkCompile: Int get() = libs().findVersion("sdk-compile").get().requiredVersion.toInt()
internal val Project.sdkMin: Int get() = libs().findVersion("sdk-min").get().requiredVersion.toInt()
internal val Project.sdkTarget: Int get() = libs().findVersion("sdk-target").get().requiredVersion.toInt()
internal val Project.javaSourceMin: Int get() = libs().findVersion("java-source-min").get().requiredVersion.toInt()
internal val Project.jdkVersion: Int get() = libs().findVersion("jdk").get().requiredVersion.toInt()

internal fun VersionCatalog.getLibrary(library: String): Provider<MinimalExternalModuleDependency> {
    return findLibrary(library).get()
}


