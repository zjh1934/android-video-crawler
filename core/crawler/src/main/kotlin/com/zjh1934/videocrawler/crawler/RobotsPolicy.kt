package com.zjh1934.videocrawler.crawler

import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/** 可注入，测试时用 AllowAll / DenyPath，避免 CI 单测联网。 */
interface RobotsPolicy {
    fun isAllowed(url: String): Boolean
}

object AllowAllRobots : RobotsPolicy {
    override fun isAllowed(url: String) = true
}

/**
 * 精简版 robots.txt：只解析 `User-agent: *` 段，
 * 按最长匹配前缀决定 Allow / Disallow。抓取失败时默认放行（不因 robots 不可达而卡死整站）。
 */
class HttpRobotsPolicy(
    private val fetch: (String) -> String? = ::defaultFetch,
    private val userAgent: String = "*",
) : RobotsPolicy {

    private data class Rule(val prefix: String, val allow: Boolean)

    private val cache = ConcurrentHashMap<String, List<Rule>>()

    override fun isAllowed(url: String): Boolean {
        val uri = runCatching { URI(url) }.getOrElse { return true }
        val origin = "${uri.scheme}://${uri.host}" + if (uri.port > 0) ":${uri.port}" else ""
        val rules = cache.getOrPut(origin) { parse(fetch("$origin/robots.txt").orEmpty()) }
        val path = (uri.rawPath?.takeIf { it.isNotEmpty() } ?: "/") +
            (uri.rawQuery?.let { "?$it" } ?: "")
        val hit = rules.filter { path.startsWith(it.prefix) }.maxByOrNull { it.prefix.length }
        return hit?.allow ?: true
    }

    private fun parse(text: String): List<Rule> {
        val rules = mutableListOf<Rule>()
        var applies = false
        var seenOtherAgent = false
        for (raw in text.lines()) {
            val line = raw.substringBefore('#').trim()
            if (line.isEmpty()) continue
            val (key, value) = line.split(':', limit = 2).let {
                if (it.size == 2) it[0].trim().lowercase() to it[1].trim() else "" to ""
            }
            when (key) {
                "user-agent" -> {
                    applies = value == "*" || value.contains(userAgent, ignoreCase = true)
                    if (!applies) seenOtherAgent = true
                }
                "disallow" -> if (applies && value.isNotEmpty()) rules += Rule(value, false)
                "allow" -> if (applies && value.isNotEmpty()) rules += Rule(value, true)
            }
        }
        // 只有出现 `User-agent: *` 时才保留规则，否则按放行处理
        return if (seenOtherAgent && rules.isEmpty()) emptyList() else rules
    }

    companion object {
        fun defaultFetch(url: String): String? = runCatching {
            val c = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 5_000
                readTimeout = 5_000
                setRequestProperty("User-Agent", "android-video-crawler/0.2")
            }
            c.use { if (it.responseCode == 200) it.inputStream.bufferedReader().readText() else null }
        }.getOrNull()
    }
}

/** 测试/自定义用：按路径前缀拒绝 */
class DenyPathRobots(private vararg val prefixes: String) : RobotsPolicy {
    override fun isAllowed(url: String) = prefixes.none { url.contains(it) }
}

private fun <T : HttpURLConnection> T.use(block: (T) -> String?): String? = try {
    block(this)
} finally {
    disconnect()
}
