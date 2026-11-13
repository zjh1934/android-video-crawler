package com.zjh1934.videocrawler.crawler

import com.zjh1934.videocrawler.model.MediaFormat
import org.jsoup.Jsoup
import java.net.URI

/** 站内翻页器的 URL 工具。纯 JVM，便于在 CI 里跑单元测试。 */
object UrlNormalizer {

    private val NON_PAGE_EXT = Regex(
        """\.(jpg|jpeg|png|gif|webp|svg|ico|css|js|json|xml|pdf|zip|rar|7z|apk|exe|woff2?|ttf|mp4|m3u8|mpd|mp3|m4a|ts|m4s)(\?|#|$)""",
        RegexOption.IGNORE_CASE,
    )

    /** 去掉 fragment、规范化末尾斜杠；相对/协议相对 URL 交给 Jsoup.absUrl 解析 */
    fun normalize(raw: String): String? {
        val s = raw.trim()
        if (s.isEmpty() || s.startsWith("javascript:") || s.startsWith("mailto:") || s.startsWith("tel:")) return null
        return runCatching {
            val u = URI(s)
            if (u.scheme != null && u.scheme !in setOf("http", "https")) return null
            val noFragment = s.substringBefore('#')
            if (noFragment.endsWith("/")) noFragment.dropLast(1) else noFragment
        }.getOrNull()
    }

    fun host(url: String): String? = runCatching { URI(url).host?.lowercase() }.getOrNull()

    /** 同域 = 同 host 或其子域；allowDomains 非空时以白名单为准 */
    fun isInScope(url: String, seedHost: String, allowDomains: List<String>, sameHostOnly: Boolean): Boolean {
        val h = host(url) ?: return false
        if (allowDomains.isNotEmpty()) {
            return allowDomains.any { d -> h == d.lowercase() || h.endsWith("." + d.lowercase()) }
        }
        if (!sameHostOnly) return true
        return h == seedHost || h.endsWith(".$seedHost")
    }

    fun isPageLink(url: String): Boolean =
        !NON_PAGE_EXT.containsMatchIn(url) && !url.startsWith("blob:") && !url.startsWith("data:")

    /** 抽可继续翻页的链接：a[href] 与 iframe[src] */
    fun extractLinks(html: String, baseUrl: String): List<String> = runCatching {
        val doc = Jsoup.parse(html, baseUrl)
        doc.select("a[href], iframe[src], frame[src]")
            .map { el -> el.absUrl(if (el.hasAttr("href")) "href" else "src") }
            .filter { it.startsWith("http") }
            .mapNotNull(::normalize)
            .filter(::isPageLink)
            .distinct()
    }.getOrDefault(emptyList())

    /** 抽 HTML 里写死的媒体直链（JS 动态注入的流由 WebView 嗅探补上） */
    fun extractMedia(html: String, baseUrl: String): List<String> = runCatching {
        val doc = Jsoup.parse(html, baseUrl)
        val candidates = mutableListOf<String>()
        doc.select("video[src], source[src], audio[src]").forEach {
            candidates += it.absUrl("src")
        }
        doc.select("a[href]").forEach { candidates += it.absUrl("href") }
        candidates.filter { it.startsWith("http") && MediaFormat.fromExtension(it) != MediaFormat.OTHER }
            .distinct()
    }.getOrDefault(emptyList())
}
