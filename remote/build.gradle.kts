import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.setup.android.library)
}

extensions.configure<LibraryExtension>("android") {

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "SERVER_ENDPOINT", "\"https://api.themoviedb.org/3/movie/\"")
        buildConfigField("String", "SERVER_API_KEY", "\"3ce5fa18330f82a0e8c84eea49508b46\"")
    }
}

dependencies {
    implementation(project(":data"))
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.core)
    implementation(libs.okhttp.core)

    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.no.op)
}
