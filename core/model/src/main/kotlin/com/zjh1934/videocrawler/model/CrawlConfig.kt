package com.zjh1934.videocrawler.model

/**
 * 参数面板数据模型（R6 接 UI 持久化）。
 * 默认口径对齐 yt-dlp 命令行与 Seal 的自定义命令模板，
 * 避免自己发明一套跟上游引擎对不上的参数。
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
    val pageLoadWaitMs: Long = 2_500L,

    // —— 下载行为（R5 交给 yt-dlp）——
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
    val detectByContentType: Boolean = true,
) {
    companion object {
        val DEFAULT = CrawlConfig()
    }
}
