package com.zjh1934.videocrawler

import android.app.Activity
import android.webkit.CookieManager
import android.webkit.WebView
import com.zjh1934.videocrawler.crawler.BfsCrawlPlanner
import com.zjh1934.videocrawler.crawler.HttpRobotsPolicy
import com.zjh1934.videocrawler.model.CrawlConfig
import com.zjh1934.videocrawler.model.CrawlPipeline
import com.zjh1934.videocrawler.model.VideoHit
import com.zjh1934.videocrawler.sniffer.WebViewVideoSniffer
import java.util.concurrent.atomic.AtomicInteger

/**
 * R4 版管道：planner 产页面 → WebView 真加载触发子资源 → sniffer 嗅探 → 命中列表。
 * 下载队列仍是 R5 的 TODO，本轮不做成品。
 */
class CrawlPipelineImpl(
    private val activity: Activity,
    private val webView: WebView,
    private val sniffer: WebViewVideoSniffer,
    private val onLog: (String) -> Unit,
    private val onHit: (VideoHit) -> Unit,
) : CrawlPipeline {

    @Volatile
    private var running = false
    private var worker: Thread? = null
    private val counter = AtomicInteger(0)

    override fun run(seedUrl: String, config: CrawlConfig) {
        if (running) return
        running = true
        counter.set(0)
        sniffer.attach(seedUrl) { hit -> deliver(hit) }

        activity.runOnUiThread {
            CookieManager.getInstance().setAcceptCookie(true)
            if (config.cookie.isNotEmpty()) CookieManager.getInstance().setCookie(seedUrl, config.cookie)
            if (config.userAgent.isNotEmpty()) webView.settings.userAgentString = config.userAgent
        }

        worker = Thread({
            val planner = BfsCrawlPlanner(robots = HttpRobotsPolicy())
            try {
                planner.plan(seedUrl, config) { hit -> deliver(hit) }
                    .takeWhile { running }
                    .forEach { pageUrl ->
                        activity.runOnUiThread {
                            sniffer.webViewClient.currentPage = pageUrl
                            webView.loadUrl(pageUrl)
                            onLog("→ 打开 $pageUrl")
                        }
                        Thread.sleep(config.pageLoadWaitMs.coerceAtLeast(500L))
                    }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (e: Throwable) {
                activity.runOnUiThread { onLog("爬取出错：${e.javaClass.simpleName} ${e.message.orEmpty()}") }
            } finally {
                running = false
                activity.runOnUiThread { onLog("本轮结束，共命中 ${counter.get()} 个视频") }
            }
        }, "vc-crawl").apply { isDaemon = true; start() }
    }

    private fun deliver(hit: VideoHit) {
        counter.incrementAndGet()
        activity.runOnUiThread { onHit(hit) }
    }

    override fun stop() {
        running = false
        worker?.interrupt()
    }
}
