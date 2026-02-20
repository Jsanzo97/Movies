plugins {
    alias(libs.plugins.setup.android.library)
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.google.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.glide)
}
