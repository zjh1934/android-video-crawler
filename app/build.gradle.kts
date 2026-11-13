plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.zjh1934.videocrawler"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zjh1934.videocrawler"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-skeleton"
    }

    buildTypes {
        debug {
            // 骨架阶段不做混淆，便于后续逐块接入上游代码
            isMinifyEnabled = false
        }
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
    // R3 骨架：只保留最小依赖，保证 CI 首次构建稳定
    implementation(libs.androidx.core.ktx)

    // R4 接 WebView 嗅探（复用 super-video-downloader 的 shouldInterceptRequest 链路）
    // implementation(libs.jsoup)
    // R5 接 yt-dlp 引擎与队列（不自己写站点解析）
    // implementation(libs.youtubedl.android.library)
    // implementation(libs.youtubedl.android.ffmpeg)
    // implementation(libs.youtubedl.android.aria2c)
    // implementation(libs.androidx.room.runtime)
    // R6 换 Compose UI（对齐 Seal 的 Material 3 设置面板）
    // implementation(platform(libs.compose.bom))
    // implementation(libs.compose.ui)
    // implementation(libs.compose.material3)
    testImplementation(libs.junit)
}
