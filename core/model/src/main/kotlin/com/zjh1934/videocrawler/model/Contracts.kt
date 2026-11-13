package com.zjh1934.videocrawler.model

/** 一次嗅探命中：来源页 + 媒体地址 + 播放/下载所需的请求头 */
data class VideoHit(
    val pageUrl: String,
    val mediaUrl: String,
    val mime: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val format: MediaFormat = MediaFormat.OTHER,
)

/**
 * ① 浏览器嗅探 —— 不自己写，复刻
 * alexch33/super-video-downloader 的 CustomWebViewClient.shouldInterceptRequest
 * + VideoUtils + IVideoDetector 三级链路。本接口只作为接线契约。
 */
interface VideoSniffer {
    fun attach(pageUrl: String, onHit: (VideoHit) -> Unit)
    fun inspect(
        pageUrl: String,
        mediaUrl: String,
        headers: Map<String, String>,
        mimeHint: String? = null,
    )
    fun stop()
}

/**
 * ② 站内自动翻页 —— 这一层自己写（上游只做「单页嗅探」）
 * onMedia 用于把 HTML 里 <video>/<source>/<iframe> 的直链一并交给队列。
 */
interface CrawlPlanner {
    fun plan(
        seedUrl: String,
        config: CrawlConfig,
        onMedia: (VideoHit) -> Unit = {},
    ): Sequence<String>
}

/** ③ 解析/下载队列 —— 不自己写站点解析，R5 交给 yt-dlp (youtubedl-android) */
interface DownloadQueue {
    fun enqueue(hit: VideoHit, config: CrawlConfig)
    fun cancel(taskId: String)
}

/** 三块拼起来的总流程 */
interface CrawlPipeline {
    fun run(seedUrl: String, config: CrawlConfig)
    fun stop()
}
