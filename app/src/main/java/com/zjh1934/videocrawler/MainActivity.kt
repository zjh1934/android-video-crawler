package com.zjh1934.videocrawler

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.zjh1934.videocrawler.model.CrawlConfig
import com.zjh1934.videocrawler.model.VideoHit
import com.zjh1934.videocrawler.sniffer.WebViewVideoSniffer

/**
 * R4 界面：输入一个网址 → 站内自动翻页 → 嗅探到的视频逐条列出。
 * 仍是原生 View，R6 换 Compose + Material 3（对齐 Seal 的结构）。
 */
class MainActivity : Activity() {

    private lateinit var webView: WebView
    private lateinit var urlEdit: EditText
    private lateinit var logText: TextView
    private lateinit var scroll: ScrollView
    private lateinit var countText: TextView
    private val sniffer = WebViewVideoSniffer()
    private lateinit var pipeline: CrawlPipelineImpl
    private val seen = HashSet<String>()
    private val log = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(20), dp(16), dp(16))
        }

        root.addView(label("站内视频爬取器 · R4 骨架", 19f, "#1B1B1F", true))
        root.addView(label("给一个网址，自动翻页找视频；下载引擎 R5 接入", 13f, "#66667A"))

        urlEdit = EditText(this).apply {
            hint = "https://example.com/"
            inputType = InputType.TYPE_TEXT_VARIATION_URI
            setSingleLine(true)
            textSize = 14f
        }
        root.addView(urlEdit, params(match(), wrap(), top = 14))

        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val startBtn = Button(this).apply { text = "开始爬取" }
        val stopBtn = Button(this).apply { text = "停止" }
        row.addView(startBtn, params(0, wrap(), weight = 1f))
        row.addView(stopBtn, params(0, wrap(), weight = 1f, left = 8))
        root.addView(row, params(match(), wrap(), top = 8))

        countText = label("已发现 0 个视频", 13f, "#1B1B1F", true, top = 10)
        root.addView(countText)

        logText = TextView(this).apply {
            textSize = 11f
            setTextColor(Color.parseColor("#33333A"))
            setBackgroundColor(Color.parseColor("#F3F3F7"))
            setPadding(dp(12), dp(10), dp(12), dp(10))
        }
        scroll = ScrollView(this).apply {
            addView(logText, ViewGroup.LayoutParams(match(), wrap()))
            setBackgroundColor(Color.parseColor("#F3F3F7"))
        }
        root.addView(scroll, params(match(), 0, weight = 1f, top = 10))

        // 真加载页面的 WebView：让 JS 播放器发出子资源请求，被拦截链路嗅探到
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            webViewClient = sniffer.webViewClient
        }
        root.addView(webView, params(match(), dp(170), top = 10))

        setContentView(root)

        pipeline = CrawlPipelineImpl(this, webView, sniffer, ::appendLog, ::onHit)

        startBtn.setOnClickListener {
            val seed = urlEdit.text.toString().trim()
            if (!seed.startsWith("http://") && !seed.startsWith("https://")) {
                appendLog("请填 http:// 或 https:// 开头的网址")
                return@setOnClickListener
            }
            seen.clear()
            countText.text = "已发现 0 个视频"
            appendLog("开始：$seed")
            val cfg = CrawlConfig.DEFAULT.copy(
                seedUrl = seed,
                maxDepth = 2,
                maxPages = 12,
                delayMs = 500L,
                pageLoadWaitMs = 2_500L,
            )
            pipeline.run(seed, cfg)
        }
        stopBtn.setOnClickListener {
            pipeline.stop()
            appendLog("已请求停止")
        }

        appendLog("R4 已就绪：BFS 翻页 + WebView 嗅探 + 命中列表")
        appendLog("未接入：yt-dlp 下载引擎 / 队列 / 参数面板（R5-R6）")
    }

    private fun onHit(hit: VideoHit) {
        if (!seen.add(hit.mediaUrl)) return
        countText.text = "已发现 ${seen.size} 个视频"
        val short = if (hit.mediaUrl.length > 96) hit.mediaUrl.take(96) + "…" else hit.mediaUrl
        appendLog("[${hit.format.label}] $short")
    }

    private fun appendLog(line: String) {
        val body = {
            log.append(line).append('\n')
            logText.text = log.toString()
            scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
        }
        if (Looper.myLooper() == Looper.getMainLooper()) body() else runOnUiThread { body() }
    }

    private fun label(
        text: String,
        size: Float,
        color: String,
        bold: Boolean = false,
        top: Int = 0,
    ) = TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(Color.parseColor(color))
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        gravity = Gravity.START
        layoutParams = params(match(), wrap(), top = top)
    }

    private fun params(w: Int, h: Int, weight: Float = 0f, top: Int = 0, left: Int = 0) =
        LinearLayout.LayoutParams(w, h, weight).apply {
            topMargin = dp(top)
            leftMargin = dp(left)
        }

    private fun match() = ViewGroup.LayoutParams.MATCH_PARENT
    private fun wrap() = ViewGroup.LayoutParams.WRAP_CONTENT
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        pipeline.stop()
        sniffer.stop()
        webView.destroy()
        super.onDestroy()
    }
}
