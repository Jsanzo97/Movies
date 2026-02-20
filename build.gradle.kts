plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.detekt.gradle.plugin) apply false
}

tasks.register("detektAll") {
    group = "verification"
    description = "Run Detekt analisis"
    dependsOn(subprojects.mapNotNull { project ->
        val detektTask = "${project.path}:detekt"
        if (project.plugins.hasPlugin("io.gitlab.arturbosch.detekt")) detektTask else null
    })
}
