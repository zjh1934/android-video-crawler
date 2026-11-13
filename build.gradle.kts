// 根工程：只声明插件版本，不写业务逻辑。
// 核心能力（站点解析 / 流下载 / 队列）一律复用上游开源项目，见 docs/UPSTREAM.md
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
}
