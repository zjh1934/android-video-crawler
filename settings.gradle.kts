pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    // Kotlin 的 android/jvm 两个插件来自同一 jar，若由各模块 alias 各自带版本请求，
    // Gradle 会报 "plugin already on classpath with an unknown version"。
    // 这里统一钉版本，子模块用 id(...) 不带版本即可。
    // 版本与 gradle/libs.versions.toml 保持一致，升级时两处同改。
    plugins {
        id("com.android.application") version "8.5.2"
        id("com.android.library") version "8.5.2"
        id("org.jetbrains.kotlin.android") version "1.9.24"
        id("org.jetbrains.kotlin.jvm") version "1.9.24"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "android-video-crawler"

// R4 起按 docs/ARCHITECTURE.md 拆模块：
//   :core:model    纯 JVM：数据模型 + 能力契约（可在 CI 里跑单测）
//   :core:crawler  纯 JVM：同域 BFS 翻页器（这层自己写，上游没有）
//   :core:sniffer  Android：WebView 嗅探链路（复刻 super-video-downloader）
//   :app           界面与管道编排
include(":app")
include(":core:model")
include(":core:crawler")
include(":core:sniffer")
