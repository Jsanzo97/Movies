plugins {
    alias(libs.plugins.setup.android.library)
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.arrow.core)
    implementation(libs.koin.core)
}
