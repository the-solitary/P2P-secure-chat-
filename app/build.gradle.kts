plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.jorge.p2pchat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jorge.p2pchat"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1-skeleton"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    // Crypto primitivo ligero (X25519, ChaCha20-Poly1305, BLAKE2b) — libsodium nativo, huella pequeña
    implementation("com.goterl:lazysodium-android:5.1.0@aar")
    implementation("net.java.dev.jna:jna:5.13.0@aar")

    // Coroutines para el socket UDP en segundo plano sin bloquear UI
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Cache local ligero para la UI (mensajes, chats)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Ajustes persistentes (tema, privacidad) — ligero, reemplaza SharedPreferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // UI mínima con Compose
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Navegación con transiciones animadas entre pantallas (chats, ajustes)
    implementation("androidx.navigation:navigation-compose:2.7.7")
}
