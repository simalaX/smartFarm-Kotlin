import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    alias(libs.plugins.google.gms.google.services)

    //parcelize location
    //alias(libs.plugins.kotlin.parcelize)
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.smartfarm.activity"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.smartfarm.activity"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Load API key from keys.properties
        val keysPropertiesFile = rootProject.file("keys.properties")
        val keysProperties = Properties()
        if (keysPropertiesFile.exists()) {
            keysProperties.load(FileInputStream(keysPropertiesFile))
        } else {
            println("WARNING: keys.properties file not found!")
        }

        buildConfigField(
            "String",
            "WEATHER_API_KEY",
            "\"${keysProperties.getProperty("WEATHER_API_KEY", "")}\""
        )

        manifestPlaceholders["MAP_API_KEY"] = keysProperties.getProperty("MAP_API_KEY", "")

        buildConfigField(
            "String",
            "MAP_API_KEY",
            "\"${keysProperties.getProperty("MAP_API_KEY", "")}\""
        )

        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${keysProperties.getProperty("GOOGLE_WEB_CLIENT_ID", "")}\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
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

//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_17  // Changed from 11 to 17
//        targetCompatibility = JavaVersion.VERSION_17  // Changed from 11 to 17
    //isCoreLibraryDesugaringEnabled = true
//    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Firebase BoM and Firebase Auth (single place)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore)

    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Compose Animation
    implementation("androidx.compose.animation:animation:1.9.3")

    // for Google Sign-In (from `jude` branch)
    //implementation("com.google.android.gms:play-services-auth:21.2.0")

    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // coil
    implementation("io.coil-kt:coil-compose:2.7.0")

    // extend icons
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.57.2")
    ksp("com.google.dagger:hilt-compiler:2.57.2")

    implementation("com.google.accompanist:accompanist-swiperefresh:0.32.0")

    // Hilt Navigation Compose (recommended for Compose + Hilt)
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.datastore:datastore-preferences-core:1.1.7")

    // Local Database (Room) - for offline storage
    implementation("androidx.room:room-runtime:2.8.2")
    implementation("androidx.room:room-ktx:2.8.2")
    ksp("androidx.room:room-compiler:2.8.2")

    // Timber for logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Modern Networking Stack
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:5.2.1")
    implementation("com.squareup.okhttp3:logging-interceptor:5.2.1")

    // Gson
    implementation("com.google.code.gson:gson:2.13.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    // Location Services
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // SwipeRefresh for Compose
    implementation("com.google.accompanist:accompanist-swiperefresh:0.36.0")

    // Google Maps
    implementation("com.google.maps.android:maps-compose:6.12.1")
    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Optional: Places API for better location search
    implementation("com.google.android.libraries.places:places:5.0.0")
}