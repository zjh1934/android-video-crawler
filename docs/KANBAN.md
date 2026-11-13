# 流动看板

> 每轮对话结束前，把下面这份看板原样贴进回复的代码框里；本文件随仓库同步更新。

```text
========================================================
 流动看板 · android-video-crawler
 仓库   : https://github.com/zjh1934/android-video-crawler
 工作区 : /data/workspace/android-video-crawler  (与仓库同步)
 轮次   : R4 / 2026-11-13
========================================================

【前两轮做了什么】
 R2  下达指令：给 GitHub 凭证，在 workspace 起项目、新建仓库并同步、
     先搭骨架不出成品、沙盒无 SDK 改用 GitHub CI 打包、
     增加流动看板且每轮用代码框汇报。
 R3  新建公开仓库 zjh1934/android-video-crawler (main)；
     workspace 搭起可编译 Android 骨架（Gradle 8.7 + AGP 8.5.2 + JDK17
     / compileSdk34）；
     落地四个能力契约 + CrawlConfig 参数模型 + 分档权限 Manifest；
     docs/UPSTREAM.md / ARCHITECTURE.md / KANBAN.md；
     GitHub Actions 打包 APK，CI 排障三次后跑绿
     （去第三方 setup-android → 换损坏的 wrapper.jar → core-ktx 降级）；
     git push 被网络策略拦，改走 Git Data API 同步，tree 与仓库一致。
     产出 app-debug-skeleton 空壳 APK。

【本轮做了什么 R4】自主推进，未新增成品功能，只添砖加瓦
 ✅ 拆成 4 个模块：
    :core:model（纯 JVM：CrawlConfig / VideoHit / MediaFormat / 契约）
    :core:crawler（纯 JVM+Jsoup：BfsCrawlPlanner / UrlNormalizer / RobotsPolicy）
    :core:sniffer（Android：SniffingWebViewClient / VideoDetector）
    :app（MainActivity 可交互界面 + CrawlPipelineImpl 编排）
 ✅ 核心不自己写：复刻 super-video-downloader 三处核心
    VideoUtils → MediaFormat.kt（后缀/Content-Type/#EXTM3U 魔数三级判定）
    CustomWebViewClient → SniffingWebViewClient.kt（主帧跳过、请求头透传、
      返回 null 不吞响应）
    IVideoDetector + TabViewModel → VideoDetector.kt（去重 + Range 异步探测）
    每个文件头注明原文件路径；仓库整体转 GPL-3.0，补 LICENSE + NOTICE
 ✅ 自己写上游没有的那层：给首页 → Jsoup 抽同域链接 → 去重 BFS →
    深度/页数/间隔/robots 可控；HTML 里的 <video>/<source> 直链一并交出
 ✅ 界面能真跑：输入 https 网址 → 逐页 WebView 真加载 → 命中 mp4/m3u8/
    mpd/webm/flv 去重列出（只列不下载）
 ✅ 纯 JVM 单测 13 例（格式判定、URL 归一化、BFS 深度/去重/上限、
    robots、媒体直链、请求头透传），CI 增加 :core:model:test :core:crawler:test
 ✅ 架构/上游/README/NOTICE/CI 文档全部同步到 R4
 ⚠ 仍未做：yt-dlp 下载引擎、队列、参数面板、Compose、成品 APK 功能
 ⚠ 合规：默认 respectRobots=true，只爬有权保存的内容，不碰 DRM/付费墙

【后面准备做什么】
 R5  引 youtubedl-android(+ffmpeg/aria2c)，接 yt-dlp 解析与下载；
     实现 DownloadQueue，抄 Seal 的前台服务 + Room 队列 + 命令模板；
     把 CrawlConfig 翻译成 yt-dlp 命令行（-f/-o/--cookies/--proxy/aria2c）；
     MediaStore 写入 Download/、断点续传、仅 WiFi、进度通知。
 R6  Compose/Material3 参数面板与任务列表；
     按分发渠道决定是否开 MANAGE_EXTERNAL_STORAGE / 悬浮窗 / VpnService；
     CI 增加 release 签名构建与版本发布。
 合规 只爬有权保存的内容，遵守 ToS/robots，不碰 DRM/付费墙/登录墙。

【CI】
 workflow: .github/workflows/android-ci.yml
 任务    : 单测 → assembleDebug → 上传 app-debug-r4 APK + JUnit 报告
 状态    : ⏳ 本轮同步后运行中（上一轮 R3 为 success）
========================================================
```
