import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension


//plugins {
//    alias(libs.plugins.detekt.gradle.plugin)
//}
//
//configure<DetektExtension> {
//    config.setFrom("$rootDir/config/detekt.yml")
//    source.setFrom(fileTree("src") {
//        include("**/*.kt")
//    })
//
//    tasks.withType<Detekt>().configureEach {
//        jvmTarget = "17"
//        exclude { it.file.absolutePath.contains("/build/generated") }
//    }
//
//    val detektAll = tasks.register("detektAll") {
//        group = "verification"
//        description = "Run Detekt analisis"
//        dependsOn(tasks.withType<Detekt>())
//    }
//
//    afterEvaluate {
//        tasks.named("check").configure {
//            setDependsOn(dependsOn.filterNot { it is TaskProvider<*> && it.name == "detekt"})
//            dependsOn(detektAll)
//        }
//    }
//}
