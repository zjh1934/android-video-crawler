package com.zjh1934.videocrawler.crawler

import com.zjh1934.videocrawler.model.CrawlConfig
import com.zjh1934.videocrawler.model.CrawlPlanner
import com.zjh1934.videocrawler.model.MediaFormat
import com.zjh1934.videocrawler.model.VideoHit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrawlPlannerTest {

    private val seed = "https://site.com/"
    private val host = "site.com"

    @Test fun `normalize 去掉 fragment 与末尾斜杠`() {
        assertEquals("https://site.com", UrlNormalizer.normalize("https://site.com/"))
        assertEquals("https://site.com/p", UrlNormalizer.normalize("https://site.com/p#top"))
        assertEquals(null, UrlNormalizer.normalize("javascript:void(0)"))
        assertEquals("site.com", UrlNormalizer.host("https://site.com/p"))
    }

    @Test fun `同域包含子域 白名单优先`() {
        assertTrue(UrlNormalizer.isInScope("https://site.com/a", host, emptyList(), true))
        assertTrue(UrlNormalizer.isInScope("https://m.site.com/a", host, emptyList(), true))
        assertFalse(UrlNormalizer.isInScope("https://other.com/a", host, emptyList(), true))
        assertTrue(UrlNormalizer.isInScope("https://cdn.other.com/a", host, listOf("other.com"), true))
    }

    @Test fun `抽链接解析相对地址并过滤非页面资源`() {
        val html = """
            <a href="/p1">p1</a>
            <a href="https://site.com/p2#x">p2</a>
            <a href="https://other.com/x">out</a>
            <a href="/a.jpg">img</a>
            <iframe src="/embed/1"></iframe>
        """.trimIndent()
        val links = UrlNormalizer.extractLinks(html, seed)
        assertTrue(links.contains("https://site.com/p1"))
        assertTrue(links.contains("https://site.com/p2"))
        assertTrue(links.contains("https://site.com/embed/1"))
        assertFalse(links.any { it.contains("a.jpg") })
    }

    @Test fun `抽 HTML 写死的媒体直链`() {
        val html = """
            <video src="/v.mp4"></video>
            <source src="https://cdn.site.com/h.m3u8">
            <a href="/v.mp4?dl=1">下载</a>
            <a href="/about">关于</a>
        """.trimIndent()
        val media = UrlNormalizer.extractMedia(html, seed)
        assertEquals(3, media.size)
        assertTrue(media.any { it.endsWith("v.mp4") })
        assertTrue(media.any { it.contains("h.m3u8") })
    }

    private fun fakeSite(): Map<String, String> = mapOf(
        "https://site.com" to """<a href="/p1">1</a><a href="/p2">2</a><video src="/a.mp4"></video>""",
        "https://site.com/p1" to """<a href="/p3">3</a><source src="/b.m3u8">""",
        "https://site.com/p2" to """<a href="https://outside.com/x">out</a><a href="/p1">back</a>""",
        "https://site.com/p3" to """<a href="/p4">4</a>""",
        "https://site.com/p4" to "<html></html>",
    )

    private fun planner(
        site: Map<String, String> = fakeSite(),
        robots: RobotsPolicy = AllowAllRobots,
    ): CrawlPlanner = BfsCrawlPlanner(
        fetchHtml = { url, _ -> site[url] },
        robots = robots,
        sleep = { },
    )

    @Test fun `BFS 按深度产出页面 且去重不外跳`() {
        val cfg = CrawlConfig(maxDepth = 1, maxPages = 20, delayMs = 0, respectRobots = true)
        val pages = planner().plan(seed, cfg).toList()
        assertEquals(listOf("https://site.com", "https://site.com/p1", "https://site.com/p2"), pages)
    }

    @Test fun `BFS 深度上限与页数上限生效`() {
        assertEquals(5, planner().plan(seed, CrawlConfig(maxDepth = 5, maxPages = 20, delayMs = 0)).toList().size)
        assertEquals(2, planner().plan(seed, CrawlConfig(maxDepth = 5, maxPages = 2, delayMs = 0)).toList().size)
    }

    @Test fun `onMedia 把 HTML 直链交给队列`() {
        val hits = mutableListOf<VideoHit>()
        planner().plan(seed, CrawlConfig(maxDepth = 1, delayMs = 0), onMedia = { hits += it }).toList()
        assertEquals(2, hits.size)
        assertTrue(hits.any { it.format == MediaFormat.MP4 })
        assertTrue(hits.any { it.format == MediaFormat.M3U8 })
        assertTrue(hits.all { it.pageUrl.startsWith("https://site.com") })
    }

    @Test fun `robots 禁止的路径不产出`() {
        val cfg = CrawlConfig(maxDepth = 1, delayMs = 0, respectRobots = true)
        val pages = planner(robots = DenyPathRobots("/p2")).plan(seed, cfg).toList()
        assertFalse(pages.any { it.endsWith("/p2") })
        assertTrue(pages.contains("https://site.com/p1"))
    }

    @Test fun `请求身份参数透传到媒体头`() {
        val hits = mutableListOf<VideoHit>()
        val cfg = CrawlConfig(maxDepth = 0, delayMs = 0, referer = "https://site.com/", cookie = "sid=1")
        planner().plan(seed, cfg, onMedia = { hits += it }).toList()
        assertEquals("https://site.com/", hits.first().headers["Referer"])
        assertEquals("sid=1", hits.first().headers["Cookie"])
    }
}
