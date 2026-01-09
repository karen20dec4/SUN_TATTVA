plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.android.sun"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.android.sun"
        minSdk = 26
        targetSdk = 34
        versionCode = 3
        versionName = "2.02"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        // 🔽 Include doar limbile necesare (reduce APK cu 1–3 MB)
         resourceConfigurations += listOf("en", "ro")
    }

    buildTypes {

        // 🔥 DEBUG — fără minify, fără shrink, fără R8
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }

        // 🔥 RELEASE — fără minify, fără shrink, fără R8
        release {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    // 🔥 Dezactivăm complet orice optimizare pe test APK
    testOptions {
        unitTests.isIncludeAndroidResources = true
        animationsDisabled = true
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
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    packaging {
        resources {
            // 🔽 Elimină fișiere text inutile din librării (reduce APK cu ~200–400 KB)
            excludes += listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/LICENSE*",
                "META-INF/NOTICE*"
            )
        }
    }
}

dependencies {
    // Android Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Location
    implementation("com.google.android.gms:play-services-location:21.1.0")
	


    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Swiss Ephemeris (JAR)
    implementation(files("libs/swisseph.jar"))

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
