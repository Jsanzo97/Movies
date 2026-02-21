plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(gradleApi())
    implementation(libs.android.gradle.plugin)
    implementation(libs.detekt.gradle.plugin)
    implementation(libs.spotless.gradle.plugin)
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

        create("CommonVerifications") {
            id = "common-verifications"
            implementationClass = "CommonVerificationsPlugin"
        }
    }
}
