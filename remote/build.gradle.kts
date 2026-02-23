import com.android.build.api.dsl.LibraryExtension
import java.util.Properties

plugins {
    alias(libs.plugins.setup.android.library)
}


extensions.configure<LibraryExtension>("android") {

    buildFeatures {
        buildConfig = true
    }

    val localProperties = Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) load(file.inputStream())
    }

    defaultConfig {
        buildConfigField(
            "String",
            "SERVER_ENDPOINT",
            "\"${localProperties["SERVER_ENDPOINT"] ?: System.getenv("SERVER_ENDPOINT")}\"",
        )
        buildConfigField(
            "String",
            "SERVER_API_KEY",
            "\"${localProperties["SERVER_API_KEY"] ?: System.getenv("SERVER_API_KEY")}\"",
        )
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
