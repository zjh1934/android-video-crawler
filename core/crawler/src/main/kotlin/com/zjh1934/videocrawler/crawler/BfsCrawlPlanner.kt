package com.zjh1934.videocrawler.crawler

import com.zjh1934.videocrawler.model.CrawlConfig
import com.zjh1934.videocrawler.model.CrawlPlanner
import com.zjh1934.videocrawler.model.MediaFormat
import com.zjh1934.videocrawler.model.VideoHit
import org.jsoup.Jsoup
import java.util.ArrayDeque

/**
 * 「给一个首页 → 自己在这个站里翻出所有视频页」这一层，上游项目都没有，自己写。
 *
 * 链路：seed → Jsoup 抽同域链接 → 去重 BFS → 每页 yield 给管道（WebView 嗅探），
 * 同时把 HTML 里写死的 <video>/<source>/<a href=*.mp4> 直链交给 onMedia。
 * 控制项全部来自 CrawlConfig，且 fetch / robots / sleep 都可注入，便于纯 JVM 单测。
 */
class BfsCrawlPlanner(
    private val fetchHtml: (String, CrawlConfig) -> String? = ::defaultFetch,
    private val robots: RobotsPolicy = HttpRobotsPolicy(),
    private val sleep: (Long) -> Unit = { Thread.sleep(it) },
) : CrawlPlanner {

    override fun plan(
        seedUrl: String,
        config: CrawlConfig,
        onMedia: (VideoHit) -> Unit,
    ): Sequence<String> = sequence {
        val seedHost = UrlNormalizer.host(seedUrl) ?: return@sequence
        val start = UrlNormalizer.normalize(seedUrl) ?: return@sequence
        val queue = ArrayDeque<Pair<String, Int>>()
        val seen = HashSet<String>()
        queue.addLast(start to 0)
        seen.add(start)
        var emitted = 0

        while (queue.isNotEmpty() && emitted < config.maxPages) {
            val (url, depth) = queue.removeFirst()
            if (config.respectRobots && !robots.isAllowed(url)) continue

            yield(url)
            emitted++

            val html = runCatching { fetchHtml(url, config) }.getOrNull()
            if (html != null) {
                UrlNormalizer.extractMedia(html, url).forEach { media ->
                    onMedia(
                        VideoHit(
                            pageUrl = url,
                            mediaUrl = media,
                            format = MediaFormat.fromExtension(media),
                            headers = buildMap {
                                if (config.referer.isNotEmpty()) put("Referer", config.referer)
                                if (config.cookie.isNotEmpty()) put("Cookie", config.cookie)
                            },
                        ),
                    )
                }
                if (depth < config.maxDepth) {
                    UrlNormalizer.extractLinks(html, url)
                        .filter { UrlNormalizer.isInScope(it, seedHost, config.allowDomains, config.sameHostOnly) }
                        .filter { seen.add(it) }
                        .forEach { queue.addLast(it to depth + 1) }
                }
            }
            if (config.delayMs > 0) sleep(config.delayMs)
        }
    }

    companion object {
        fun defaultFetch(url: String, config: CrawlConfig): String? = runCatching {
            Jsoup.connect(url)
                .timeout(10_000)
                .followRedirects(true)
                .ignoreHttpErrors(true)
                .apply {
                    if (config.userAgent.isNotEmpty()) userAgent(config.userAgent)
                    if (config.referer.isNotEmpty()) referrer(config.referer)
                    if (config.cookie.isNotEmpty()) header("Cookie", config.cookie)
                }
                .get().html()
        }.getOrNull()
    }
}
