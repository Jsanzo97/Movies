import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.detekt.gradle.plugin) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.firebase.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.stability.analyzer) apply false
}

apply(plugin = "jacoco")


tasks.register("detektAll") {
    group = "verification"
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("detekt") })
}

tasks.register("testAll") {
    group = "verification"
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("testDebugUnitTest") })
}

val jacocoExcludes = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "android/**/*.*",
    "**/di/**",
    "**/dao/**",
    "**/*_Factory*.*",
    "**/generated/**",
    "**/ksp/**",
    "**/*\$lambda$*",
    "**/*\$inlined$*",
    "**/*\$default$*",
    "**/*\$sam$*",
    "**/*\$*Function*",
    "**/*\$1*",
    "**/*\$2*",
    "**/*\$3*",
    "**/*\$4*",
    "**/*\$5*",
    "**/*\$6*",
    "**/*\$7*",
    "**/*\$8*",
    "**/*\$9*",
    "**/*ComposableSingletons*",
    "**/*WhenMappings*",
    "**/*DefaultImpls*",
    "**/ui/**",
    "**/MoviesApplication*",
    "**/LocalDatabase*",
    "**/model/**",
    "**/error/**",
    "**/entity/**",
    "**/Converters*.*",
    "**/DataStore*.*",
    "**/dto/response/**",
)

allprojects {
    extra.set("jacocoExcludes", jacocoExcludes)
}

tasks.register<JacocoReport>("jacocoMergedReport") {
    group = "verification"
    dependsOn("testAll")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.register<JacocoCoverageVerification>("jacocoMergedCoverageVerification") {
    group = "verification"
    description = "Run tests, generate report and verify coverage for all modules"
    dependsOn("jacocoMergedReport")
    violationRules {
        rule {
            limit {
                minimum = "0.95".toBigDecimal()
            }
        }
    }
}

gradle.projectsEvaluated {
    val reportTask = tasks.named<JacocoReport>("jacocoMergedReport")
    val verificationTask = tasks.named<JacocoCoverageVerification>("jacocoMergedCoverageVerification")

    listOf(reportTask, verificationTask).forEach { taskProvider ->
        taskProvider.configure {
            val variant = "debug"
            val variantName = variant.replaceFirstChar { it.uppercase() }

            subprojects.forEach { subproject ->
                if (subproject.plugins.hasPlugin("com.android.library") || subproject.plugins.hasPlugin("com.android.application")) {
                    classDirectories.from(
                        subproject.fileTree("${subproject.layout.buildDirectory.get()}/intermediates/javac/$variant/classes") {
                            exclude(jacocoExcludes)
                        },
                        subproject.fileTree("${subproject.layout.buildDirectory.get()}/tmp/kotlin-classes/$variant") {
                            exclude(jacocoExcludes)
                        },
                        subproject.fileTree("${subproject.layout.buildDirectory.get()}/intermediates/built_in_kotlinc/$variant/compile${variantName}Kotlin/classes") {
                            exclude(jacocoExcludes)
                        }
                    )
                    sourceDirectories.from(subproject.files("${subproject.projectDir}/src/main/kotlin", "${subproject.projectDir}/src/main/java"))
                    executionData.from(subproject.fileTree(subproject.layout.buildDirectory.get()) {
                        include("outputs/unit_test_code_coverage/$variant/test${variantName}UnitTest.exec", "jacoco/test${variantName}UnitTest.exec")
                    })
                }
            }
        }
    }
}

tasks.register("installGitHooks", Copy::class) {
    group = "setup"
    from("config/git-hooks")
    into(".git/hooks")
    filePermissions {
        unix("755")
    }
}
