plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.navigation.safeargs.kotlin)
}

val VERSION_MAJOR: String by project
val VERSION_MINOR: String by project
val VERSION_PATCH: String by project
val versionMajor = VERSION_MAJOR.toInt()
val versionMinor = VERSION_MINOR.toInt()
val versionPatch = VERSION_PATCH.toInt()

val appVersionCode = versionMajor * 1_000_000 + versionMinor * 1_000 + versionPatch
val appVersionName = "$versionMajor.$versionMinor.$versionPatch"

android {
    namespace = "com.example.movielist"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    sourceSets.apply {
        forEach {
            it.java.srcDir("src/${it.name}/kotlin")
        }
        getByName("main") {
            val addResources: (Array<File>) -> Unit = { files: Array<File> ->
                files.filter { it.exists() }
                    .mapNotNull { it.listFiles { file: File -> file.isDirectory } }
                    .forEach { folders -> res.srcDirs(*folders) }
            }
            val resScreens = file("src/main/res-screens")

            addResources(arrayOf(resScreens))
        }
    }

    defaultConfig {
        applicationId = "com.example.movielist"
        minSdk = 23
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName
        multiDexEnabled = true
        buildConfigField("String", "SERVER_ENDPOINT", "\"https://api.themoviedb.org/3/movie/\"")
        buildConfigField("String", "SERVER_API_KEY", "\"3ce5fa18330f82a0e8c84eea49508b46\"")
    }

    signingConfigs {
        val KEYSTORE_PASSWORD: String by project
        val KEY_PASSWORD: String by project
        getByName("debug") {
            try {
                storeFile = file("${project.rootDir}/keystore.jks")
                storePassword = KEYSTORE_PASSWORD
                keyAlias = "debug"
                keyPassword = KEY_PASSWORD
            } catch (e: Exception) {
                throw Throwable("You should define KEYSTORE_PASSWORD and KEY_PASSWORD in gradle.properties.")
            }
        }
        register("release") {
            try {
                storeFile = file("${project.rootDir}/keystore.jks")
                storePassword = KEYSTORE_PASSWORD
                keyAlias = "release"
                keyPassword = KEY_PASSWORD
            } catch (e: Exception) {
                throw Throwable("You should define KEYSTORE_PASSWORD and KEY_PASSWORD in gradle.properties.")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging {
        resources.excludes.add("META-INF/com.android.tools/proguard/coroutines.pro")
    }

    lint {
        abortOnError = false
    }

    testOptions {
        unitTests.apply {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":database"))
    implementation(project(":common"))
    implementation(project(":remote"))
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.google.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.arrow.core)
    implementation(libs.androidx.room.ktx)
    implementation(libs.okhttp.core)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)

    debugImplementation(libs.chucker)

    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.robolectric)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit.ext)
}
