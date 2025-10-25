plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt") // <-- AÑADE ESTA LÍNEA
}

android {
    namespace = "com.example.proyectofinal_itanestours"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.proyectofinal_itanestours"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // --- UI y Componentes Jetpack ---
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // --- Navegación (Jetpack Navigation) [cite: 83] ---
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // --- Arquitectura (MVVM) [cite: 113] ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.3")
    implementation("androidx.activity:activity-ktx:1.9.0")

    // --- Room (Base de Datos Local)  ---
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    // Opcional: Soporte de Coroutines para Room
    implementation("androidx.room:room-ktx:$roomVersion")
    // Para usar KSP en lugar de KAPT (recomendado)
    // ksp("androidx.room:room-compiler:$roomVersion")

    // --- Red (Retrofit y GSON) [cite: 80, 84] ---
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // --- Carga de Imágenes (Glide) [cite: 84] ---
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // --- Coroutines (Asincronía) ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // --- Sincronización en Segundo Plano (WorkManager) ---
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
}


