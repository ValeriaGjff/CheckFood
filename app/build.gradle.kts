plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.checkfood"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.checkfood"
        minSdk = 24
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // CameraX
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
    implementation("androidx.camera:camera-extensions:1.3.4")
    // ML Kit
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // Guava для ListenableFuture
    implementation("com.google.guava:guava:31.1-android")

    // Material (если ещё нет)
    implementation("com.google.android.material:material:1.11.0")

    implementation("androidx.appcompat:appcompat:1.6.1")


}