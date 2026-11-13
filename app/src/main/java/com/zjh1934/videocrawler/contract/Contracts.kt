package com.zjh1934.videocrawler.contract

import com.zjh1934.videocrawler.config.CrawlConfig

/** 一次嗅探命中：URL + 播放/下载所需的请求头 */
data class VideoHit(
    val pageUrl: String,
    val mediaUrl: String,
    val mime: String?,
    val headers: Map<String, String> = emptyMap(),
    val format: String = "unknown", // mp4 / m3u8 / mpd / webm / flv
)

/**
 * ① 浏览器嗅探 —— 不自己写，直接搬
 * alexch33/super-video-downloader 的 CustomWebViewClient.shouldInterceptRequest
 * + VideoUtils + IVideoDetector 三级链路。本接口只作为接线契约。
 */
interface VideoSniffer {
    fun start(pageUrl: String, onHit: (VideoHit) -> Unit)
    fun stop()
}

/**
 * ② 站内自动翻页 —— 这一层自己写（上游只做「单页嗅探」）
 * 参考实现思路：Jsoup 抽 a[href] / iframe[src] / video[src] / source[src]，
 * 同域过滤 + 去重 + BFS，按 CrawlConfig 控制深度与频率。
 */
interface CrawlPlanner {
    fun plan(seedUrl: String, config: CrawlConfig): Sequence<String>
}

/** ③ 解析/下载队列 —— 不自己写站点解析，交给 yt-dlp (youtubedl-android) */
interface DownloadQueue {
    fun enqueue(hit: VideoHit, config: CrawlConfig)
    fun cancel(taskId: String)
}

/** 三块拼起来的总流程（R4-R5 落地实现） */
interface CrawlPipeline {
    fun run(seedUrl: String, config: CrawlConfig)
}
