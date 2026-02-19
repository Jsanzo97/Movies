plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(libs.coroutines.core)
    implementation(libs.arrow.core)
    implementation(libs.koin.core)
}
