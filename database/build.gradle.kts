plugins {
    alias(libs.plugins.setup.android.library)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(project(":data"))
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.serialization.json)

    ksp(libs.androidx.room.compiler)
}
