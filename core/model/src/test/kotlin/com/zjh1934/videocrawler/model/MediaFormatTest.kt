package com.zjh1934.videocrawler.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaFormatTest {

    @Test fun `后缀命中带查询参数的直链`() {
        assertEquals(MediaFormat.MP4, MediaFormat.fromExtension("https://a.com/v.mp4?token=1"))
        assertEquals(MediaFormat.M3U8, MediaFormat.fromExtension("https://a.com/i.m3u8#t=10"))
        assertEquals(MediaFormat.MPD, MediaFormat.fromExtension("https://a.com/manifest.mpd"))
        assertEquals(MediaFormat.WEBM, MediaFormat.fromExtension("https://a.com/x.webm"))
    }

    @Test fun `上游排除规则 分片与脚本不算媒体`() {
        assertEquals(MediaFormat.OTHER, MediaFormat.fromExtension("https://a.com/seg.ts"))
        assertEquals(MediaFormat.OTHER, MediaFormat.fromExtension("https://a.com/chunk.m4s"))
        assertEquals(MediaFormat.OTHER, MediaFormat.fromExtension("https://a.com/app.js"))
        assertFalse(MediaFormat.looksLikeMediaUrl("blob:https://a.com/1234"))
    }

    @Test fun `Content-Type 与文件头魔数判定`() {
        assertEquals(MediaFormat.M3U8, MediaFormat.fromContentType("application/vnd.apple.mpegurl"))
        assertEquals(MediaFormat.MPD, MediaFormat.fromContentType("application/dash+xml"))
        assertEquals(MediaFormat.MP4, MediaFormat.fromContentType("video/mp4"))
        assertEquals(MediaFormat.M3U8, MediaFormat.fromHead("#EXTM3U\n#EXT-X"))
        assertEquals(MediaFormat.MPD, MediaFormat.fromHead("<?xml <MPD ..."))
    }

    @Test fun `guess 按 后缀 到 ContentType 到 文件头 依次回退`() {
        assertEquals(MediaFormat.MP4, MediaFormat.guess("https://a.com/v.mp4", "application/octet-stream"))
        assertEquals(MediaFormat.MPD, MediaFormat.guess("https://a.com/get?id=9", "application/dash+xml"))
        assertEquals(MediaFormat.M3U8, MediaFormat.guess("https://a.com/get?id=9", "application/octet-stream", "#EXTM3U"))
        assertEquals(MediaFormat.OTHER, MediaFormat.guess("https://a.com/page.html"))
    }

    @Test fun `looksLikeMediaUrl 只认已知媒体后缀`() {
        assertTrue(MediaFormat.looksLikeMediaUrl("https://a.com/v.mp4"))
        assertFalse(MediaFormat.looksLikeMediaUrl("https://a.com/page.html"))
    }
}
