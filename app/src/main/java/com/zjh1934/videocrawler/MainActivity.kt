package com.zjh1934.videocrawler

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.zjh1934.videocrawler.config.CrawlConfig

/**
 * R3 骨架入口：只展示工程状态与待接入的能力块，不含任何抓取逻辑。
 * 真正的界面在 R6 换成 Compose + Material 3（对齐 Seal 的结构）。
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(28))
        }

        root.addView(title(R.string.skeleton_title, 20f, Color.parseColor("#1B1B1F")))
        root.addView(title(R.string.skeleton_hint, 14f, Color.parseColor("#5A5A66"), dp(16)))

        val blocks = listOf(
            "① 浏览器嗅探" to "WebView.shouldInterceptRequest → 识别 mp4 / m3u8 / mpd\n上游：alexch33/super-video-downloader (GPL-3.0)\n状态：待接入 (R4)",
            "② 解析与下载引擎" to "yt-dlp + youtubedl-android，站点解析不自己写\n上游：yt-dlp / JunkFood02/youtubedl-android\n状态：待接入 (R5)",
            "③ 站内自动翻页" to "seed 首页 → 同域 BFS → 逐页喂给嗅探器\n这部分没有现成实现，自己写\n状态：待接入 (R4)",
            "④ 参数面板与队列" to "格式 / 并发 / 限速 / Cookie / 代理 / 深度\n上游：Seal 命令模板 + Room 队列\n状态：待接入 (R5-R6)",
            "⑤ 权限与保活" to "前台服务 + 通知 + MediaStore 写入已声明\n高危权限按看板分批开启\n状态：骨架已声明 (R3)",
        )

        blocks.forEach { (name, detail) -> root.addView(card(name, detail)) }

        val cfg = CrawlConfig.DEFAULT
        root.addView(
            card(
                "当前默认参数（骨架占位）",
                "深度=${cfg.maxDepth} 页数=${cfg.maxPages} 并发=${cfg.concurrency}\n" +
                    "间隔=${cfg.delayMs}ms 仅WiFi=${cfg.onlyWifi} 同域=${cfg.sameHostOnly}\n" +
                    "格式=${cfg.formatSelector}",
            ),
        )

        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun card(name: String, detail: String): TextView = TextView(this).apply {
        text = "$name\n$detail"
        textSize = 13f
        setTextColor(Color.parseColor("#33333A"))
        setBackgroundColor(Color.parseColor("#F3F3F7"))
        setPadding(dp(16), dp(14), dp(16), dp(14))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply { topMargin = dp(12) }
    }

    private fun title(resId: Int, size: Float, color: Int, topMargin: Int = 0): TextView =
        TextView(this).apply {
            text = getString(resId)
            textSize = size
            setTextColor(color)
            gravity = Gravity.START
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { this.topMargin = topMargin }
        }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
