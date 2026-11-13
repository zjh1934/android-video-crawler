package com.zjh1934.videocrawler.sniffer

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.zjh1934.videocrawler.model.CrawlConfig
import com.zjh1934.videocrawler.model.VideoHit
import com.zjh1934.videocrawler.model.VideoSniffer

/*
 * 拦截入口移植自 alexch33/super-video-downloader (GPL-3.0)
 *   原文件：ui/main/home/browser/CustomWebViewClient.kt (shouldInterceptRequest)
 * 保留其「主帧跳过 / 子资源全过一遍 / 请求头透传 / 不吞掉请求」的做法，
 * 去掉了广告拦截引擎、CookieUtils、RxJava 与 DataBinding 的项目内耦合。
 *
 * 关键：返回 null（super），绝不截断响应，否则页面自己就播不了。
 */
class SniffingWebViewClient(
    private val detector: VideoDetector,
    private val config: () -> CrawlConfig = { CrawlConfig.DEFAULT },
) : WebViewClient() {

    @Volatile
    var currentPage: String = ""

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        if (!url.isNullOrEmpty()) currentPage = url
        super.onPageStarted(view, url, favicon)
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest,
    ): WebResourceResponse? {
        val url = request.url?.toString()
        if (url != null && !request.isForMainFrame) {
            val headers: Map<String, String> = request.requestHeaders?.toMap().orEmpty()
            detector.inspect(currentPage, url, headers)
        }
        return super.shouldInterceptRequest(view, request)
    }
}

/** 对 app 暴露的接线门面：把 WebViewClient 交给 WebView，把命中回调交给管道 */
class WebViewVideoSniffer(
    config: () -> CrawlConfig = { CrawlConfig.DEFAULT },
) : VideoSniffer {

    private val detector = VideoDetector(config)
    val webViewClient = SniffingWebViewClient(detector, config)

    override fun attach(pageUrl: String, onHit: (VideoHit) -> Unit) {
        detector.onHit = onHit
        webViewClient.currentPage = pageUrl
    }

    override fun inspect(
        pageUrl: String,
        mediaUrl: String,
        headers: Map<String, String>,
        mimeHint: String?,
    ) = detector.inspect(pageUrl, mediaUrl, headers, mimeHint)

    override fun stop() = detector.close()
}
