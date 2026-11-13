package com.zjh1934.videocrawler.config

/**
 * 参数面板的数据模型（R5 起接入 UI 与持久化，R6 用 DataStore/Room 存储）。
 * 默认口径直接对齐 yt-dlp 命令行与 Seal 的自定义命令模板，
 * 避免自己发明一套和上游引擎对不上的参数。
 */
data class CrawlConfig(
    // —— 站内翻页策略（自己写的那一层）——
    val seedUrl: String = "",
    val maxDepth: Int = 2,
    val maxPages: Int = 60,
    val sameHostOnly: Boolean = true,
    val allowDomains: List<String> = emptyList(),
    val delayMs: Long = 800L,
    val respectRobots: Boolean = true,

    // —— 下载行为（交给 yt-dlp）——
    val formatSelector: String = "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best",
    val outputTemplate: String = "%(title)s.%(ext)s",
    val concurrency: Int = 3,
    val onlyWifi: Boolean = true,
    val resumeDownloads: Boolean = true,
    val aria2cEnabled: Boolean = false,

    // —— 请求身份（交给 WebView / yt-dlp）——
    val userAgent: String = "",
    val referer: String = "",
    val cookie: String = "",
    val proxy: String = "",

    // —— 嗅探（交给 WebView 拦截链路）——
    val sniffTimeoutMs: Long = 50_000L,
    val jsTimeoutMs: Long = 15_000L,
) {
    companion object {
        val DEFAULT = CrawlConfig()
    }
}
