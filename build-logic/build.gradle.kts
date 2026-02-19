plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(gradleApi())
    implementation(libs.android.gradle.plugin)
    implementation(libs.detekt.gradle.plugin)
}

gradlePlugin {
    plugins {
        create("SetupModules") {
            id = "setup-modules-plugin"
            implementationClass = "com.example.buildlogic.SetupModulesPlugin"
        }
    }
}

