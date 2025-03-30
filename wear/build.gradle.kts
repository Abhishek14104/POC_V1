plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.kotlin.kapt)
}
android {
    namespace = "com.example.wear"
    compileSdk = 36
    buildFeatures {
        viewBinding = true
        compose = true
    }
    defaultConfig {
        applicationId = "com.example.mobile"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Wear OS and other Android dependencies
    implementation(libs.androidx.wear) // androidx.wear:wear:1.3.0
    implementation(libs.play.services.wearable) // com.google.android.gms:play-services-wearable:19.0.0
    implementation(libs.androidx.wear.tooling.preview) // androidx.wear:wear-tooling-preview:1.0.0
    implementation(libs.androidx.compose.material) // androidx.wear.compose:compose-material:1.2.1
    implementation(libs.androidx.compose.foundation) // androidx.wear.compose:compose-foundation:1.2.1
    implementation(libs.androidx.activity.compose) // androidx.activity:activity-compose:1.10.1
    implementation(libs.androidx.core.splashscreen) // androidx.core:core-splashscreen:1.0.1

    // Material Design components and others
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.compose.material3:material3:1.0.0")

    // Compose BOM (Bill of Materials) for dependency management
    implementation(platform(libs.androidx.compose.bom)) // androidx.compose:compose-bom:2024.09.00

    // Compose UI dependencies
    implementation(libs.androidx.ui) // androidx.compose.ui:ui:1.3.0
    implementation(libs.androidx.ui.graphics) // androidx.compose.ui:ui-graphics
    implementation(libs.androidx.ui.tooling.preview) // androidx.compose.ui:ui-tooling-preview
    implementation(libs.androidx.ui.test.junit4) // androidx.compose.ui:ui-test-junit4

    // Test dependencies
    androidTestImplementation(platform(libs.androidx.compose.bom)) // androidx.compose:compose-bom
    androidTestImplementation(libs.androidx.ui.test.junit4) // androidx.compose.ui:ui-test-junit4

    // Debug dependencies
    debugImplementation(libs.androidx.ui.tooling) // androidx.compose.ui:ui-tooling
    debugImplementation(libs.androidx.ui.test.manifest) // androidx.compose.ui:ui-test-manifest
}
