// Top-level build.gradle.kts
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Add AGP classpath here
        classpath("com.android.tools.build:gradle:8.6.0")
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
}