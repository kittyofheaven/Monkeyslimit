plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.menac1ngmonkeys.monkeyslimit"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.menac1ngmonkeys.monkeyslimit"
        minSdk = 29
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
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
//    implementation(libs.googleid)
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

    // ---------------- Permissions ----------------
    implementation(libs.androidx.concurrent.futures.ktx)
    implementation(libs.accompanist.permissions)

    // ---------------- Navigation ----------------
    implementation(libs.androidx.navigation.compose)      // Navigation for Compose

    // ---------------- Data / Persistence (Room) ----------------
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)            // Room runtime
    // For Kotlin, prefer KSP (remove annotationProcessor if not needed)
    ksp(libs.androidx.room.compiler)                      // Room annotation processor

    // ---------------- Testing ----------------
    testImplementation(libs.junit)                        // Local unit tests

    androidTestImplementation(libs.androidx.junit)        // Instrumented tests (JUnit)
    androidTestImplementation(libs.androidx.espresso.core)// Espresso UI tests
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4) // Compose UI tests

    debugImplementation(libs.androidx.compose.ui.tooling) // Layout inspector tooling
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
