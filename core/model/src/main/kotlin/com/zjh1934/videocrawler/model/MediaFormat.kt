package com.zjh1934.videocrawler.model

/*
 * 判定口径移植自 alexch33/super-video-downloader (GPL-3.0)
 *   原文件：app/src/main/java/com/myAllVideoBrowser/util/VideoUtils.kt
 *   原文件：app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/CustomWebViewClient.kt
 * 保留其「后缀 → Content-Type → 文件头魔数」三级判定与排除规则，
 * 去掉了对 OkHttpProxyClient / RxJava / DataBinding 的项目内耦合，改为纯 JVM 函数。
 * 本仓库因引入 GPL-3.0 代码，整体按 GPL-3.0 分发，见根目录 LICENSE。
 */

enum class MediaFormat(val label: String, val isVideo: Boolean) {
    MP4("mp4", true),
    M3U8("m3u8", true),
    MPD("mpd", true),
    WEBM("webm", true),
    FLV("flv", true),
    MP3("mp3", false),
    M4A("m4a", false),
    OTHER("other", false),
    ;

    companion object {
        /** 上游 VideoUtils：这些后缀即使 Content-Type 像视频也不当媒体处理 */
        private val SKIP_EXT = Regex("""\.(js|css|m4s|ts)$""", RegexOption.IGNORE_CASE)

        /** 上游 CustomWebViewClient 的 media 后缀集合，另补 flv / mov / mkv */
        private val KNOWN_EXT = Regex(
            """\.(mp4|m3u8|mpd|webm|flv|mov|mkv|mp3|m4a)(\?|#|$)""",
            RegexOption.IGNORE_CASE,
        )

        fun fromExtension(url: String): MediaFormat {
            val path = url.substringBefore('?').substringBefore('#').lowercase()
            if (SKIP_EXT.containsMatchIn(path)) return OTHER
            return when {
                path.endsWith(".m3u8") -> M3U8
                path.endsWith(".mpd") -> MPD
                path.endsWith(".mp4") -> MP4
                path.endsWith(".webm") -> WEBM
                path.endsWith(".flv") || path.endsWith(".mov") || path.endsWith(".mkv") -> FLV
                path.endsWith(".mp3") -> MP3
                path.endsWith(".m4a") -> M4A
                else -> OTHER
            }
        }

        fun fromContentType(raw: String?): MediaFormat {
            val v = raw?.lowercase().orEmpty()
            return when {
                v.contains("mpegurl") || v.contains("vnd.apple.mpegurl") -> M3U8
                v.contains("dash+xml") || v.contains("dash") -> MPD
                v.contains("video") -> MP4
                v.contains("audio") -> MP3
                else -> OTHER
            }
        }

        /** 上游对 application/octet-stream 读前 7 字节判断 #EXTM3U / <MPD */
        fun fromHead(head: String?): MediaFormat = when {
            head == null -> OTHER
            head.startsWith("#EXTM3U") -> M3U8
            head.contains("<MPD") -> MPD
            else -> OTHER
        }

        fun looksLikeMediaUrl(url: String): Boolean =
            !url.startsWith("blob:") && KNOWN_EXT.containsMatchIn(url) && !SKIP_EXT.containsMatchIn(url)

        /**
         * 三级判定：URL 后缀 → Content-Type → 文件头。
         * 与上游 VideoUtils.getContentTypeByUrl 同口径，但不发网络请求（网络探测由 VideoDetector 异步做）。
         */
        fun guess(url: String, contentType: String? = null, head: String? = null): MediaFormat {
            val byUrl = fromExtension(url)
            if (byUrl != OTHER) return byUrl
            val byType = fromContentType(contentType)
            if (byType != OTHER) return byType
            return fromHead(head)
        }
    }
}
