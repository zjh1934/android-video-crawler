plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.zjh1934.videocrawler"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.zjh1934.videocrawler"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "0.2.0-r4"
    }

    buildTypes {
        debug { isMinifyEnabled = false }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(project(":core:model"))
    implementation(project(":core:crawler"))
    implementation(project(":core:sniffer"))

    // R5 接 yt-dlp 引擎与前台下载队列（站点解析不自己写）：
    // implementation(libs.youtubedl.android.library)
    // implementation(libs.youtubedl.android.ffmpeg)
    // implementation(libs.youtubedl.android.aria2c)

    testImplementation(libs.junit)
}
