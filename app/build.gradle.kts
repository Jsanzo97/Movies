import java.util.Properties

plugins {
    alias(libs.plugins.setup.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.stability.analyzer)
}

val localProps = Properties()
val localPropsFile = File(rootProject.projectDir, "local.properties")
if (localPropsFile.exists()) {
    localProps.load(localPropsFile.inputStream())
}

val sdkDir = localProps.getProperty("sdk.dir")
    ?: System.getenv("ANDROID_HOME")
    ?: System.getenv("ANDROID_SDK_ROOT")
    ?: ""

val adb = "$sdkDir/platform-tools/adb"

tasks.register("enableFirebaseDebug") {
    group = "firebase"
    description = "Enables Firebase Analytics DebugView on the connected emulator/device"
    val adbPath = adb
    doLast {
        ProcessBuilder(adbPath, "shell", "setprop", "debug.firebase.analytics.app", "jsanzo.movies.debug")
            .inheritIO()
            .start()
            .waitFor()
        println("✅ Firebase Analytics DebugView enabled for jsanzo.movies.debug")
    }
}

tasks.register("disableFirebaseDebug") {
    group = "firebase"
    description = "Disables Firebase Analytics DebugView on the connected emulator/device"
    val adbPath = adb
    doLast {
        ProcessBuilder(adbPath, "shell", "setprop", "debug.firebase.analytics.app", ".none.")
            .inheritIO()
            .start()
            .waitFor()
        println("✅ Firebase Analytics DebugView disabled")
    }
}

composeStabilityAnalyzer {
    stabilityValidation {
        enabled.set(true)
        outputDir.set(layout.projectDirectory.dir("stability"))
        includeTests.set(false)
        ignoreNonRegressiveChanges.set(true)
        failOnStabilityChange.set(System.getenv("CI") == "true")
    }
}

tasks.matching { it.name.contains("StabilityCheck") }.configureEach {
    mustRunAfter(tasks.named("compileDebugUnitTestKotlin"))
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":database"))
    implementation(project(":remote"))
    implementation(project(":datastore"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
}
