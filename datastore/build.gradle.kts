plugins {
    alias(libs.plugins.setup.android.library)
}

dependencies {
    implementation(project(":data"))
    implementation(libs.datastore.preferences)
}