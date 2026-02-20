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

internal fun VersionCatalog.getLibrary(library: String): Provider<MinimalExternalModuleDependency> {
    return findLibrary(library).get()
}
