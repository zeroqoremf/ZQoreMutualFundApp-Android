// app/build.gradle.kts (Module :app)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // REMOVED: Explicitly define version for kotlin-parcelize
   // id("org.jetbrains.kotlin.plugin.parcelize") version "1.9.24" // Use your Kotlin version
    id("kotlin-parcelize") // RE-ADDED: Common idiomatic way to apply Parcelize, no explicit version for now
    // ADDED: Explicitly define version for Safe Args plugin
    id("androidx.navigation.safeargs.kotlin") version "2.7.7" // Use your Navigation version
}

android {
    namespace = "com.zeroqore.mutualfundapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zeroqore.mutualfundapp"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    // IMPORTANT: This block enables View Binding
    buildFeatures {
        viewBinding = true
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

    // Testing dependencies (from your existing libs.versions.toml)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}