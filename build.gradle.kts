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

tasks.register("testAll") {
    group = "verification"
    description = "Run unit tests on all modules"
    dependsOn(subprojects.mapNotNull { project ->
        if (project.plugins.hasPlugin("com.android.library") ||
            project.plugins.hasPlugin("com.android.application")) {
            "${project.path}:testDebugUnitTest"
        } else null
    })
}

tasks.register("installGitHooks", Copy::class) {
    group = "setup"
    description = "Installs git hooks for the project"
    from("config/git-hooks")
    into(".git/hooks")
    filePermissions {
        unix("755")
    }
}
