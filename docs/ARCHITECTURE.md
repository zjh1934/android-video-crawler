# 架构与演进路线

## 数据流（目标形态）

```
用户给一个网址 (seed)
   │
   ▼
[CrawlPlanner] 自己写：同域 BFS，按深度/页数/间隔/robots 控制
   │  逐页 URL
   ▼
[WebView + VideoSniffer] 复用 super-video-downloader
   │  shouldInterceptRequest 拦子资源 → Content-Type/魔数/后缀判定 → 去重
   ▼  VideoHit(url, mime, headers)
[DownloadQueue + yt-dlp] 复用 youtubedl-android / Seal
   │  getInfo() 拿清晰度 → YoutubeDLRequest 下载 → ffmpeg 合并 → aria2c 加速
   ▼
[MediaStore / SAF] 写入 Download/，前台服务常驻通知保活
   ▼
[Compose UI] 对齐 Seal：任务列表、格式选择、参数面板、日志
```

## 模块演进

- **R3（当前）**：单 `:app` 模块 + 接口契约（`contract/`、`config/`），能编译出空壳 APK。
- **R4**：拆 `:core:model` / `:core:sniffer` / `:core:crawler`，接 WebView 拦截与 BFS。
- **R5**：加 `:core:downloader`，引 `youtubedl-android` + ffmpeg + aria2c，接前台服务与 Room 队列。
- **R6**：`:feature:settings` 换 Compose + Material 3，参数面板持久化。

## 权限分档（按「拿到了要真能办事」）

| 档位 | 权限 | 干什么 | 何时开 |
|---|---|---|---|
| A 已启用 | `INTERNET`、`ACCESS_NETWORK_STATE` | 请求与网络状态判断 | R3 |
| A 已启用 | `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_DATA_SYNC`、`POST_NOTIFICATIONS`、`WAKE_LOCK` | 后台下载不被杀、进度通知 | R3 声明，R5 真正使用 |
| B 按需 | `MANAGE_EXTERNAL_STORAGE` | 写任意目录 | 仅自签/F-Droid 分发时开；Play 白名单不含下载器，需声明表 + 演示视频 + 应用内显著披露 |
| B 按需 | `SYSTEM_ALERT_WINDOW` | 悬浮下载按钮 | R6，明确申请 |
| B 按需 | `VpnService` | 抓「别的 App」的视频流，免 root | R6，参考 PCAPdroid |
| C 不申请 | 通讯录/短信/通话记录/定位/相机/录音 | 与本功能无关 | 永远不开 |
| C 不申请 | `AccessibilityService` | 自动点击虽强，但非残障辅助用途属明令禁止，拒审高发区 | 永远不开 |
| C 不申请 | ROOT 提权 | 依赖设备已 root、商店不可能给，而 VpnService + 前台服务已够用 | 不考虑 |

存储：Android 11+ 走 MediaStore / SAF 写 `Download/`，无需全盘权限。

## 合规边界

只处理自己有权保存的内容；遵守站点 ToS 与 robots；不绕 DRM、付费墙、登录墙；不做批量盗抓。Play 版同类应用（如 Seal）会主动阉割部分站点提取以过审。
