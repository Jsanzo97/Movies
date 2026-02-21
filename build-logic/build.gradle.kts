plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(gradleApi())
    implementation(libs.android.gradle.plugin)
    implementation(libs.detekt.gradle.plugin)
    implementation(libs.detekt.rules.compose)
    implementation(libs.spotless.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
}

gradlePlugin {
    plugins {
        create("SetupAndroidApplication") {
            id = "setup-android-application"
            implementationClass = "SetupAndroidApplicationPlugin"
        }

        create("SetupAndroidLibrary") {
            id = "setup-android-library"
            implementationClass = "SetupAndroidLibraryPlugin"
        }

        create("CommonSetup") {
            id = "common-setup"
            implementationClass = "CommonSetupPlugin"
        }
    }
}
