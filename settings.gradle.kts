pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
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
