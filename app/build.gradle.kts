// app/build.gradle.kts (Module :app)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize") // Keep this for Parcelable support
    id("androidx.navigation.safeargs.kotlin") version "2.7.7"
    // REMOVED: id("org.jetbrains.kotlin.plugin.serialization") // REMOVED: We are using Gson, not Kotlinx Serialization
}

android {
    namespace = "com.zeroqore.mutualfundapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zeroqore.mutualfundapp"
        minSdk = 23 // CHANGED: Updated minimum SDK to 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        named("debug") {
            isMinifyEnabled = false
            buildConfigField("String", "BASE_URL", "\"http://192.168.0.102:8080/\"")

            // --- UPDATED CONFIGURATION FOR GRANULAR CONTROL ---
            // Flag to enable/disable live login API calls
            buildConfigField("Boolean", "USE_LIVE_LOGIN_API", "true")
            // Flag to enable/disable mocking for dashboard (holdings, portfolio, transactions) data
            buildConfigField("Boolean", "USE_DASHBOARD_MOCKS", "true")
            // Keep USE_MOCK_ASSET_INTERCEPTOR for the AssetInterceptor if needed,
            // but now it's effectively controlled by USE_DASHBOARD_MOCKS for relevant paths
            buildConfigField("Boolean", "USE_MOCK_ASSET_INTERCEPTOR", "true") // Ensure this is true to activate AssetInterceptor for dashboard mocks
        }
        named("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"http://192.168.0.102:8080/\"")
            // For release, typically you'd want live APIs if backend is ready
            buildConfigField("Boolean", "USE_LIVE_LOGIN_API", "false") // Set to 'true' when backend is fully ready
            buildConfigField("Boolean", "USE_DASHBOARD_MOCKS", "false") // Set to 'false' when backend is fully ready
            buildConfigField("Boolean", "USE_MOCK_ASSET_INTERCEPTOR", "false") // No asset mocks in release
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    // Core AndroidX UI and utilities
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // ADDED: Explicit RecyclerView dependency to ensure a modern version for bindingAdapterPosition
    // Using 1.3.2 which is a stable and recent version that supports bindingAdapterPosition
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // AndroidX Navigation Component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // AndroidX Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // AndroidX Fragment KTX
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.swiperefreshlayout)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Keep this for Gson compatibility

    // OkHttp for logging network requests
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")

    // NEW: AndroidX Security-Crypto for EncryptedSharedPreferences
    implementation(libs.androidx.security.crypto)

    // REMOVED: kotlinx.serialization JSON library - NOT NEEDED WITH GSON
    // REMOVED: implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}