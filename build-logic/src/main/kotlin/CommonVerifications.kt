import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.TestedExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

internal fun Project.setupDetekt() {
    pluginManager.apply("io.gitlab.arturbosch.detekt")

    dependencies {
        "detektPlugins"(libs().getLibrary("detekt-rules-compose"))
    }

    extensions.configure<DetektExtension> {
        config.setFrom("$rootDir/config/detekt.yml")
        source.setFrom(fileTree("src") {
            include("**/*.kt")
        })
    }

    tasks.withType<Detekt>().configureEach {        
        jvmTarget = "17"
        exclude { it.file.absolutePath.contains("/build/generated") }        
    }
}

internal fun Project.setupSpotless() {
    pluginManager.apply("com.diffplug.spotless")

    extensions.configure<SpotlessExtension> {
        lineEndings = LineEnding.PLATFORM_NATIVE

        kotlin {
            target("src/*/kotlin/**/*.kt")

            ktlint(libs().getLibrary("ktlint").get().version)
                .editorConfigOverride(
                    mapOf(
                        "ktlint_standard_filename" to "disabled",
                        "ktlint_standard_class-naming" to "disabled",
                        "ktlint_standard_function-naming" to "disabled",
                        "ktlint_standard_property-naming" to "disabled",
                        "ktlint_standard_discouraged-comment-location" to "disabled",
                        "ktlint_standard_no-empty-file" to "disabled",
                        "ktlint_standard_backing-property-naming" to "disabled",
                        "ktlint_standard_binary-expression-wrapping" to "disabled",
                        "ktlint_standard_chain-method-continuation" to "disabled",
                        "ktlint_standard_class-signature" to "disabled",
                        "ktlint_standard_condition-wrapping" to "disabled",
                        "ktlint_standard_function-expression-body" to "disabled",
                        "ktlint_standard_function-literal" to "disabled",
                        "ktlint_standard_function-type-modifier-spacing" to "disabled",
                        "ktlint_standard_multiline-loop" to "disabled",
                        "ktlint_standard_no-unused-imports" to "enabled",
                    )
                )
        }
    }
}

internal fun Project.setupCheck() {
    afterEvaluate {
        tasks.named("check").configure {
            setDependsOn(dependsOn.filterNot { it is TaskProvider<*> && it.name == "detekt" })
            dependsOn(tasks.withType<Detekt>())
            dependsOn(tasks.named("spotlessCheck"))
        }
    }
}

internal fun Project.setupJunitTests() {
    afterEvaluate {
        extensions.findByName("android")?.let { ext ->
            when (ext) {
                is com.android.build.api.dsl.ApplicationExtension -> {
                    ext.testOptions.unitTests {
                        isReturnDefaultValues = true
                        all { test ->
                            test.useJUnitPlatform()
                        }
                    }
                }
                is com.android.build.api.dsl.LibraryExtension -> {
                    ext.testOptions.unitTests {
                        isReturnDefaultValues = true
                        all {
                            it.useJUnitPlatform()
                        }
                    }
                }
            }
        }
    }

    dependencies {
        "testImplementation"(libs().getLibrary("junit-jupiter-api"))
        "testImplementation"(libs().getLibrary("junit-jupiter-engine"))
        "testImplementation"(libs().getLibrary("coroutines-test"))
        "testImplementation"(libs().getLibrary("mockk"))
        "testImplementation"(libs().getLibrary("kotest-runner-junit5"))
        "testImplementation"(libs().getLibrary("kotest-assertions-core"))
        "testImplementation"(libs().getLibrary("arrow-core"))
    }
}

internal fun Project.setupJacocoReport() {
    pluginManager.apply("jacoco")

    extensions.configure<JacocoPluginExtension> {
        toolVersion = "0.8.12"
    }

    val excludes = listOf(
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
        $$"**/*$lambda$*",
        $$"**/*$inlined$*",
        $$"**/*$default$*",
        $$"**/*$sam$*",
        "**/*$*Function*",
        $$"**/*$1*",
        $$"**/*$2*",
        $$"**/*$3*",
        $$"**/*$4*",
        $$"**/*$5*",
        $$"**/*$6*",
        $$"**/*$7*",
        $$"**/*$8*",
        $$"**/*$9*",
        "**/*ComposableSingletons*",
        "**/*WhenMappings*",
        "**/*DefaultImpls*",
        "**/ui/theme/**",
        "**/ui/navigation/**",
        "**/ui/screens/**/*Screen*",
        "**/ui/screens/**/*ViewStateProvider*",
        "**/LocalDatabase*",
        "**/ui/ComposeActivity*",
        "**/model/**",
        "**/MoviesApplication*",
    )

    extra["jacocoExcludes"] = excludes

    afterEvaluate {
        extensions.findByName("android")?.let { ext ->
            val variants = when (ext) {
                is com.android.build.api.dsl.ApplicationExtension -> listOf("debug")
                is com.android.build.api.dsl.LibraryExtension -> listOf("debug")
                else -> emptyList()
            }

            variants.forEach { variant ->
                val variantName = variant.replaceFirstChar { it.uppercase() }

                val compileTask = tasks.names.firstOrNull {
                    it.startsWith("compile${variantName}Kotlin")
                }

                val androidTasksToWaitFor = listOf(
                    "process${variantName}Manifest",
                    "merge${variantName}Assets",
                    "compile${variantName}LibraryResources",
                    "merge${variantName}JavaResource",
                ).filter { tasks.names.contains(it) }

                tasks.register("jacoco${variantName}TestReport", JacocoReport::class.java) {
                    dependsOn(listOfNotNull("test${variantName}UnitTest", compileTask))
                    if (androidTasksToWaitFor.isNotEmpty()) mustRunAfter(androidTasksToWaitFor)
                    group = "verification"
                    description = "Generate JaCoCo coverage report for $variant variant"

                    reports {
                        xml.required.set(true)
                        html.required.set(true)
                    }

                    val javaClasses = fileTree("${layout.buildDirectory.get()}/intermediates/javac/$variant/classes") {
                        exclude(excludes)
                    }

                    val kotlinClasses = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/$variant") {
                        exclude(excludes)
                    }

                    val kotlincClasses = fileTree("${layout.buildDirectory.get()}/intermediates/built_in_kotlinc/$variant/compile${variantName}Kotlin/classes") {
                        exclude(excludes)
                    }

                    classDirectories.setFrom(files(javaClasses, kotlinClasses, kotlincClasses))
                    sourceDirectories.setFrom(files("src/main/kotlin", "src/main/java"))
                    executionData.setFrom(
                        fileTree(layout.buildDirectory.get()) {
                            include("**/*.exec", "**/*.ec")
                        }
                    )
                }
            }
        }
    }
}
