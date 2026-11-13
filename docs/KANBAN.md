# 流动看板

> 每轮对话结束前，把下面这份看板原样贴进回复的代码框里；本文件随仓库同步更新。

```text
========================================================
 流动看板 · android-video-crawler
 仓库   : https://github.com/zjh1934/android-video-crawler
 工作区 : /data/workspace/android-video-crawler  (与仓库同步)
 轮次   : R3 / 2026-11-13
========================================================

【前两轮做了什么】
 R1  提出需求：安卓浏览器给一个网址就自动在站内找视频并爬取；
     参数借鉴开源、核心不自己写、权限越大越好。
     → 完成开源选型与评估：
       super-video-downloader(WebView 嗅探) / yt-dlp + youtubedl-android(解析下载)
       / Seal(队列与设置面板) / V4AW、QuickTube、sniffWebkit(备选)。
     → 给出权限分档结论：前台服务+通知+MediaStore 是真办事的最小集；
       MANAGE_EXTERNAL_STORAGE、VpnService 有条件可用；
       无障碍/ROOT/无关隐私权限不拿。
 R2  下达本轮指令：给 GitHub 凭证，在 workspace 起项目、新建仓库并同步、
     先搭骨架不出成品、沙盒无 SDK 改用 GitHub CI 打包、
     增加流动看板且每轮用代码框汇报。

【本轮做了什么 R3】
 ✅ 用凭证确认账号 zjh1934，新建公开仓库 android-video-crawler (main)
 ✅ 在 /data/workspace/android-video-crawler 搭起可编译的 Android 骨架：
    Gradle 8.7 + AGP 8.5.2 + Kotlin 1.9.24 + JDK17 / compileSdk34 / minSdk26
    gradlew + wrapper.jar 齐备；单 app 模块，零业务逻辑
 ✅ 落地接口契约：VideoHit / VideoSniffer / CrawlPlanner / DownloadQueue / CrawlPipeline
 ✅ 落地参数模型 CrawlConfig（深度/页数/并发/限速/Cookie/代理/格式模板…）
 ✅ Manifest 按分档声明「真办事」权限，高危权限注释留位
 ✅ GitHub Actions：setup-java17 + setup-android35 + assembleDebug + 上传 APK
 ✅ docs/UPSTREAM.md（核心代码借鉴清单+许可证）、docs/ARCHITECTURE.md（数据流/模块演进）
 ✅ 本看板 docs/KANBAN.md，并写进 CI Run Summary
 ✅ 代码 push 到 main，workspace 与仓库同步
 ⚠ 未做：任何真实抓取/下载逻辑、成品 APK 功能、Compose 界面

【后面准备做什么】
 R4  拆 :core:model / :core:sniffer / :core:crawler；
     搬 super-video-downloader 的 shouldInterceptRequest 嗅探链路；
     自己写同域 BFS 翻页器（上游没有这层）。
 R5  引 youtubedl-android(+ffmpeg/aria2c)，接 yt-dlp 解析与下载；
     抄 Seal 的前台服务 + Room 队列 + 命令模板；参数翻译成 yt-dlp 命令行；
     接 MediaStore 写入、断点续传、仅 WiFi。
 R6  Compose/Material3 设置面板与任务列表；
     按分发渠道决定是否开 MANAGE_EXTERNAL_STORAGE / 悬浮窗 / VpnService；
     CI 增加 release 签名构建与版本发布。
 合规 只爬有权保存的内容，遵守 ToS/robots，不碰 DRM/付费墙/登录墙。

【CI】
 workflow: .github/workflows/android-ci.yml
 产物    : app-debug-skeleton (app/build/outputs/apk/debug/*.apk)
 状态    : 已推送，等待首次 Actions 运行
========================================================
```
