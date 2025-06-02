// app/build.gradle.kts (Module :app)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin") version "2.7.7"
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.zeroqore.mutualfundapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zeroqore.mutualfundapp"
        minSdk = 21 // Your current minSdk
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // --- START OF CHANGES FOR BUILD FEATURES ---
    buildFeatures {
        viewBinding = true
        buildConfig = true // ADDED: This enables BuildConfig generation
    }
    // --- END OF CHANGES FOR BUILD FEATURES ---

    // --- START OF CHANGES FOR BUILD TYPES ---
    buildTypes {
        named("debug") { // ADDED: Explicitly define the 'debug' build type
            isMinifyEnabled = false
            // Define BuildConfig fields for the DEBUG build type
            buildConfigField("String", "BASE_URL", "\"http://private-anon-e766e44b9e-mutualfundapi.apiary-mock.com/\"")
            buildConfigField("Boolean", "USE_MOCK_ASSET_INTERCEPTOR", "true") // Use mock assets in debug
            // Proguard files are usually not needed for debug, but you can add if desired
        }
        named("release") {
            isMinifyEnabled = true // SUGGESTED CHANGE: Typically true for release to reduce APK size
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Define BuildConfig fields for the RELEASE build type
            buildConfigField("String", "BASE_URL", "\"http://your.production.api.com/\"") // IMPORTANT: REPLACE with your actual production API URL
            buildConfigField("Boolean", "USE_MOCK_ASSET_INTERCEPTOR", "false") // Do NOT use mock assets in release
        }
    }
    // --- END OF CHANGES FOR BUILD TYPES ---

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    // Core AndroidX UI and utilities (from your existing libs.versions.toml)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // AndroidX Navigation Component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // AndroidX Lifecycle (for ViewModel and LiveData)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // AndroidX Fragment KTX (explicitly added for fragment management)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.swiperefreshlayout)

    // Testing dependencies (from your existing libs.versions.toml)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // For JSON parsing

    // OkHttp for logging network requests (optional but very useful for debugging)
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0") // For logging

    // ADDED: kotlinx.serialization JSON library
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // ADDED: For SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0") // Or a newer stable version if available
}