plugins {
    alias(libs.plugins.setup.android.library)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(project(":common"))
    implementation(project(":data"))
    implementation(libs.coroutines.core)
    implementation(libs.arrow.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.core)
    implementation(libs.okhttp.core)
}
