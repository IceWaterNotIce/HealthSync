pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://androidx.dev/storage/health-connect/") }
    }
    plugins {
        id("com.android.application") version "8.6.0" apply false // 更新至 8.6.0
        id("com.android.library") version "8.6.0" apply false // 更新至 8.6.0
        id("org.jetbrains.kotlin.android") version "2.1.10" apply false // 新增 Kotlin 插件
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") } // 確保 JitPack 倉庫已添加
        maven { url = uri("https://androidx.dev/storage/health-connect/") } // 確保 Health Connect 倉庫已添加
    }
}

rootProject.name = "HealthSync"
include(":app")
