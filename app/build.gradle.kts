import java.util.Properties

plugins {
    alias(libs.plugins.setup.android.application)
    alias(libs.plugins.navigation.safeargs.kotlin)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
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

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":database"))
    implementation(project(":remote"))
    implementation(libs.coroutines.android)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.google.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.glide)

    androidTestImplementation(libs.androidx.junit.ext)
}
