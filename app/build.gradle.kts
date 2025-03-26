plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services") // Apply Google Services plugin
}

android {
    namespace = "com.icewaternotice.healthsync"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.icewaternotice.healthsync"
        minSdk = 24
        targetSdk = 34
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
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0") // 修正版本號格式
    implementation("com.google.android.gms:play-services-auth:20.7.0") // 新增 Google Sign-In 依賴
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}