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
    implementation(libs.mpandroidchart)
    implementation(libs.play.services.auth)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    implementation(libs.connect.client)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.connect.client.v110alpha11)
}