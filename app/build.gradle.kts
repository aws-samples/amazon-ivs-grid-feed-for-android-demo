plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-kapt")
    id("kotlinx-serialization")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
}

android {
    namespace = "com.amazon.ivs.gridfeed"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.amazon.ivs.gridfeed"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of("17"))
        }
    }
}

dependencies {
    // Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0-alpha13")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.4")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Timber
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48.1")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    kapt("com.google.dagger:hilt-compiler:2.48.1")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    ksp("com.github.bumptech.glide:ksp:4.16.0")
    implementation("androidx.exifinterface:exifinterface:1.3.6")

    // DataStore
    implementation("androidx.datastore:datastore:1.1.0-alpha05")

    // Amazon
    implementation("com.amazonaws:ivs-player:1.22.0")
}
