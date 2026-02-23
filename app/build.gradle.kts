import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-parcelize")
}

// Read local.properties
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

android {
    signingConfigs {
        create("release") {
            val storeFilePath = localProperties.getProperty("RELEASE_STORE_FILE")

            // This logic handles the path safely even with spaces
            if (!storeFilePath.isNullOrEmpty()) {
                val keystoreFile = file(storeFilePath)
                if (keystoreFile.exists()) {
                    storeFile = keystoreFile
                } else {
                    // This will print a helpful message in the Build log if it still fails
                    println("Keystore Debug: File not found at $storeFilePath")
                }
            }

            storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")
            keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
            keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")
        }
    }
    namespace = "com.menac1ngmonkeys.monkeyslimit"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.menac1ngmonkeys.monkeyslimit"
        minSdk = 29
        targetSdk = 36
        versionCode = 9
        versionName = "1.2.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"https://monkeylimitsbe.rtbconnect.space/\"")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "BASE_URL", "\"https://monkeylimitsbe.rtbconnect.space/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// 👇 New recommended way instead of kotlinOptions { jvmTarget = "21" }
kotlin {
    compilerOptions {
        // Match your Java target; upgraded to JVM_21
        jvmToolchain(21)
    }
}

dependencies {
    // ---------------- Core Android & Lifecycle ----------------
    implementation(libs.androidx.core.ktx)                 // Core KTX extensions
    implementation(libs.androidx.lifecycle.runtime.ktx)    // Lifecycle & coroutines
    implementation(libs.androidx.work.runtime.ktx)         // WorkManager (background tasks)

    // ---------------- Firebase ----------------
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.google.auth)
    implementation(libs.firebase.firestore)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    implementation(libs.coil.compose)

    // ---------------- Jetpack Compose UI ----------------
    implementation(platform(libs.androidx.compose.bom))    // Compose BOM (align versions)
    implementation(libs.androidx.compose.ui)               // Core Compose UI
    implementation(libs.androidx.compose.ui.graphics)      // Drawing & graphics
    implementation(libs.androidx.compose.ui.tooling.preview) // @Preview support
    implementation(libs.androidx.compose.foundation.layout)

    // ---------------- Splashscreen ----------------
    implementation(libs.androidx.core.splashscreen)

    // ---------------- External Libraries ----------------
    implementation(libs.vico.compose.m3) // Compose support for Vico charts

    // ---------------- Material Design ----------------
    implementation(libs.material)                         // Material 2 (View system)
    implementation(libs.androidx.compose.material3)       // Material 3 for Compose
    implementation(libs.androidx.material.icons.extended)

    // ---------------- CameraX ----------------
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    implementation(libs.guava)

    // ---------------- ML Kit ----------------
    implementation(libs.play.services.mlkit.text.recognition)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // Compose Cropper for image zoom, pan, rotate
    implementation(libs.compose.cropper)

    implementation(libs.androidx.runtime.livedata)

    // ---------------- Permissions ----------------
    implementation(libs.androidx.concurrent.futures.ktx)
    implementation(libs.accompanist.permissions)

    // ---------------- Navigation ----------------
    implementation(libs.androidx.navigation.compose)      // Navigation for Compose

    // ---------------- Data / Persistence (Room) ----------------
    implementation(libs.androidx.room.runtime)            // Room runtime
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)
    implementation(libs.foundation)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.ui.text)
    // For Kotlin, prefer KSP (remove annotationProcessor if not needed)
    ksp(libs.androidx.room.compiler)                      // Room annotation processor

    implementation(libs.androidx.work.runtime)

    // ---------------- Testing ----------------
    testImplementation(libs.junit)                        // Local unit tests

    androidTestImplementation(libs.androidx.junit)        // Instrumented tests (JUnit)
    androidTestImplementation(libs.androidx.espresso.core)// Espresso UI tests
    androidTestImplementation(platform(libs.androidx.compose.bom))
    // UI Testing (Android Test)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    // Needed for "createComposeRule"
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Debug Manifest (Required for UI tests to launch activities)
    debugImplementation(libs.androidx.ui.test.manifest)

    debugImplementation(libs.androidx.compose.ui.tooling) // Layout inspector tooling
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
