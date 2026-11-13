package com.zjh1934.videocrawler.sniffer

import com.zjh1934.videocrawler.model.CrawlConfig
import com.zjh1934.videocrawler.model.MediaFormat
import com.zjh1934.videocrawler.model.VideoHit
import java.net.HttpURLConnection
import java.net.URL
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/*
 * 职责对应 alexch33/super-video-downloader (GPL-3.0) 的
 *   detectedVideos/IVideoDetector.kt 与 VideoDetectionTabViewModel.kt：
 *   去重 + 异步校验链接状态 + 把结果推给候选列表。
 * 这里去掉 RxJava / DataBinding / 项目内 ViewModel 依赖，
 * 用单线程 Executor 做非阻塞的 Content-Type 探测，保证不卡 WebView 回调线程。
 */
class VideoDetector(private val config: () -> CrawlConfig = { CrawlConfig.DEFAULT }) {

    @Volatile
    var onHit: ((VideoHit) -> Unit)? = null

    private val seen: MutableSet<String> =
        Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    private val probeExecutor: ExecutorService = Executors.newSingleThreadExecutor { r ->
        Thread(r, "vc-probe").apply { isDaemon = true }
    }

    private val staticExt = Regex(
        """\.(js|css|png|jpe?g|gif|webp|svg|ico|woff2?|ttf|html?|json|xml|txt|m4s|ts)(\?|#|$)""",
        RegexOption.IGNORE_CASE,
    )
    private val probeKeyword = Regex(
        """(video|stream|play|media|m3u8|mpd|manifest|/api/)""",
        RegexOption.IGNORE_CASE,
    )

    fun inspect(
        pageUrl: String,
        mediaUrl: String,
        headers: Map<String, String>,
        mimeHint: String? = null,
    ) {
        if (mediaUrl.startsWith("blob:") || mediaUrl.startsWith("data:")) return
        val byUrlOrMime = MediaFormat.guess(mediaUrl, mimeHint)
        if (byUrlOrMime != MediaFormat.OTHER) {
            emit(pageUrl, mediaUrl, byUrlOrMime, headers, mimeHint)
            return
        }
        if (shouldProbe(mediaUrl, headers)) {
            probeExecutor.execute {
                runCatching { probe(pageUrl, mediaUrl, headers) }
            }
        }
    }

    private fun shouldProbe(url: String, headers: Map<String, String>): Boolean {
        if (!config().detectByContentType) return false
        if (staticExt.containsMatchIn(url) || MediaFormat.looksLikeMediaUrl(url)) return false
        val accept = headers.entries
            .firstOrNull { it.key.equals("Accept", ignoreCase = true) }?.value.orEmpty()
        return accept.contains("video", true) || accept.contains("audio", true) ||
            probeKeyword.containsMatchIn(url)
    }

    /** 对应上游 VideoUtils：发 Range 请求，读 Content-Type + 前 7 字节魔数 */
    private fun probe(pageUrl: String, url: String, headers: Map<String, String>) {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 5_000
            readTimeout = 5_000
            instanceFollowRedirects = true
            setRequestProperty("Range", "bytes=0-6")
            headers.forEach { (k, v) ->
                if (!k.equals("Range", true) && !k.equals("Host", true)) {
                    setRequestProperty(k, v)
                }
            }
            val ua = config().userAgent
            if (ua.isNotEmpty() && headers.keys.none { it.equals("User-Agent", true) }) {
                setRequestProperty("User-Agent", ua)
            }
        }
        try {
            val mime = conn.headerField("Content-Type")
            val head = conn.inputStream.bufferedReader().use { r ->
                val buf = CharArray(7)
                val n = r.read(buf)
                if (n > 0) String(buf, 0, n) else ""
            }
            val fmt = MediaFormat.guess(url, mime, head)
            if (fmt != MediaFormat.OTHER) emit(pageUrl, url, fmt, headers, mime)
        } finally {
            conn.disconnect()
        }
    }

    private fun emit(
        pageUrl: String,
        mediaUrl: String,
        format: MediaFormat,
        headers: Map<String, String>,
        mime: String?,
    ) {
        if (!seen.add(mediaUrl)) return
        onHit?.invoke(VideoHit(pageUrl, mediaUrl, mime, headers, format))
    }

    fun close() {
        probeExecutor.shutdownNow()
        onHit = null
    }
}
