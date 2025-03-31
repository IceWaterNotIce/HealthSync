plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services") // Apply Google Services plugin
}

android {
    namespace = "com.icewaternotice.healthsync"
    compileSdk = 35 // 確保 compileSdk 為 35

    defaultConfig {
        applicationId = "com.icewaternotice.healthsync"
        minSdk = 26 // 提升 minSdk 至 26
        targetSdk = 35 // 更新 targetSdk 至 35
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0") // 確保版本號正確
    implementation("com.google.android.gms:play-services-auth:20.7.0") // 確保版本號正確
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
    implementation("androidx.health.connect:connect-client:1.1.0-beta01") // 確保版本號正確
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.11.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
}