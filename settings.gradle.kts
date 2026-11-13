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

// 骨架阶段：单 app 模块。
// R4 起按 docs/ARCHITECTURE.md 拆分为 :core:model / :core:sniffer / :core:crawler / :core:downloader / :feature:settings
include(":app")
