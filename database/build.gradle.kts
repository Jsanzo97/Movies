plugins {
    alias(libs.plugins.setup.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(libs.coroutines.core)
    implementation(libs.arrow.core)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.serialization.json)

    ksp(libs.androidx.room.compiler)
}
