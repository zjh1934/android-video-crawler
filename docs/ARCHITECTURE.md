# 架构与演进路线

## 数据流（R4 已跑通前半段）

```
用户给一个网址 (seed)
   │
   ▼
[core:crawler / BfsCrawlPlanner] 自己写 ✅ R4
   │  Jsoup 抽 a[href]/iframe[src] → 同域过滤 → 去重 BFS
   │  控制：maxDepth / maxPages / delayMs / robots.txt
   │  同时把 <video>/<source>/<a href=*.mp4> 直链交给 onMedia
   ▼  逐页 URL
[app / CrawlPipelineImpl] ✅ R4
   │  主线程 loadUrl 到 WebView，等 pageLoadWaitMs 让 JS 播放器发请求
   ▼
[core:sniffer / SniffingWebViewClient + VideoDetector] ✅ R4（复刻上游）
   │  shouldInterceptRequest 拦子资源 → 后缀/Content-Type/魔数判定 → 去重 → 返回 null 不吞响应
   ▼  VideoHit(pageUrl, mediaUrl, mime, headers, format)
[app / MainActivity 命中列表] ✅ R4（只展示，不下载）
   │
   ▼  ⏳ R5 待接
[DownloadQueue + yt-dlp] youtubedl-android / Seal
   │  getInfo() 拿清晰度 → YoutubeDLRequest 下载 → ffmpeg 合并 → aria2c 加速
   ▼
[MediaStore / SAF] 写入 Download/，前台服务常驻通知保活
   ▼
[Compose UI] 对齐 Seal：任务列表、格式选择、参数面板、日志
```

## 模块

| 模块 | 类型 | 内容 | CI |
|---|---|---|---|
| `:core:model` | 纯 JVM | `CrawlConfig` / `VideoHit` / `MediaFormat` / 四个契约接口 | `:core:model:test` |
| `:core:crawler` | 纯 JVM + Jsoup | `BfsCrawlPlanner` / `UrlNormalizer` / `RobotsPolicy` | `:core:crawler:test` |
| `:core:sniffer` | Android library | `SniffingWebViewClient` / `VideoDetector` / `WebViewVideoSniffer` | 随 assembleDebug 编译 |
| `:app` | Android app | `MainActivity`（输入网址 + 日志 + WebView）、`CrawlPipelineImpl` | 产出 debug APK |

纯 JVM 模块 + 可注入的 `fetchHtml / robots / sleep` 让翻页逻辑不需要 Android SDK 或联网就能单测。

## 演进

- **R3**：单 `:app` 骨架、契约、参数模型、权限分档、CI 打包。
- **R4（当前）**：拆四个模块；复刻 super-video-downloader 嗅探链路；自己写同域 BFS；界面可输入网址真跑「翻页 → 嗅探 → 列出命中」。**下载仍未接**。
- **R5**：接 `youtubedl-android` + ffmpeg + aria2c；`DownloadQueue` 实现；抄 Seal 前台服务 + Room 队列 + 命令模板；`CrawlConfig` 翻译为 yt-dlp 命令行；MediaStore 写入、断点续传、仅 WiFi。
- **R6**：Compose / Material 3 设置面板与任务列表；按分发渠道决定高危权限；CI 增加 release 签名构建。

## 权限分档（按「拿到了要真能办事」）

| 档位 | 权限 | 干什么 | 何时开 |
|---|---|---|---|
| A 已启用 | `INTERNET`、`ACCESS_NETWORK_STATE` | 请求与网络状态判断 | R3 ✅ |
| A 已启用 | `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_DATA_SYNC`、`POST_NOTIFICATIONS`、`WAKE_LOCK` | 后台下载不被杀、进度通知 | R3 声明，R5 真正使用 |
| B 按需 | `MANAGE_EXTERNAL_STORAGE` | 写任意目录 | 仅自签/F-Droid 分发时开；Play 白名单不含下载器，需声明表 + 演示视频 + 应用内显著披露 |
| B 按需 | `SYSTEM_ALERT_WINDOW` | 悬浮下载按钮 | R6，明确申请 |
| B 按需 | `VpnService` | 抓「别的 App」的视频流，免 root | R6，参考 PCAPdroid |
| C 不申请 | 通讯录/短信/通话记录/定位/相机/录音 | 与本功能无关 | 永远不开 |
| C 不申请 | `AccessibilityService` | 自动点击虽强，但非残障辅助用途属明令禁止，拒审高发区 | 永远不开 |
| C 不申请 | ROOT 提权 | 依赖设备已 root、商店不可能给，而 VpnService + 前台服务已够用 | 不考虑 |

存储：Android 11+ 走 MediaStore / SAF 写 `Download/`，无需全盘权限。
另：`usesCleartextTraffic=true` 仅为方便本地/HTTP 测试站，不是权限，上架前按目标站收敛。

## 合规边界

只处理自己有权保存的内容；遵守站点 ToS 与 robots（默认 `respectRobots=true`）；不绕 DRM、付费墙、登录墙；不做批量盗抓。Play 版同类应用（如 Seal）会主动阉割部分站点提取以过审。
