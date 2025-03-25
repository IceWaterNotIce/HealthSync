pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // 確保 JitPack 倉庫已添加
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // 確保 JitPack 倉庫已添加
    }
}

rootProject.name = "HealthSync"
include(":app")
