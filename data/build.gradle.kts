plugins {
    alias(libs.plugins.setup.android.library)
}

dependencies {
    implementation(project(":domain"))
    implementation(libs.coroutines.core)
    implementation(libs.arrow.core)
    implementation(libs.koin.core)
}
