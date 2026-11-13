# android-video-crawler · 站内视频自动嗅探/爬取器

给一个网址，App 用内置浏览器在站内自动翻页、找视频、排队下载。
**当前是 R4 骨架（0.2.0-r4）：翻页 + 嗅探已能跑，下载引擎还没接，不是成品。**

- 仓库：<https://github.com/zjh1934/android-video-crawler>
- 本地工作区：`/data/workspace/android-video-crawler`，与仓库 `main` 分支同步
- 核心功能不自己写：清单见 [docs/UPSTREAM.md](docs/UPSTREAM.md)
- 架构与权限分档：[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- 每轮进展：[docs/KANBAN.md](docs/KANBAN.md)
- 许可证：GPL-3.0（因复刻上游 GPL-3.0 嗅探代码），见 `LICENSE` / `NOTICE.md`

## 现在能干什么

装 APK → 输入一个 `https://` 网址 → 点「开始爬取」：

1. `BfsCrawlPlanner` 用 Jsoup 抽同域链接，去重 BFS，受深度/页数/间隔/robots 控制；
2. 每页交给 WebView 真加载，JS 播放器发出的子资源被 `SniffingWebViewClient` 拦一遍；
3. 命中 mp4 / m3u8 / mpd / webm / flv 后去重，逐条列在日志区。

**没有的**：下载、格式选择、队列、参数面板、Compose 界面（R5-R6）。

## 为什么用 CI 打包

开发沙盒没有 Android SDK，不本地出包。所有构建与单测交给
[`.github/workflows/android-ci.yml`](.github/workflows/android-ci.yml)：
JDK 17 → Android SDK 34 → `:core:model:test` + `:core:crawler:test` → `assembleDebug` → 上传 APK。
在仓库 **Actions** 页打开最新一次运行，下载 `app-debug-r4` 即可安装验证。

## 目录

```
core/model/        纯 JVM：CrawlConfig / VideoHit / MediaFormat / 四个契约
core/crawler/      纯 JVM：BfsCrawlPlanner / UrlNormalizer / RobotsPolicy + 单测
core/sniffer/      Android：SniffingWebViewClient / VideoDetector（复刻上游）
app/               MainActivity（输入网址 + 日志 + WebView）、CrawlPipelineImpl
docs/              看板 / 架构 / 上游清单
.github/workflows/ CI 打包 + 单测
```

## 本地继续开发

Android Studio Ladybug+ / JDK 17 / Android SDK 34：

```bash
git clone https://github.com/zjh1934/android-video-crawler.git
cd android-video-crawler
./gradlew :core:model:test :core:crawler:test assembleDebug
```

后续每一轮改动都会先在 workspace 落地、再同步到仓库，并在对话里用代码框贴出流动看板。

## 约定

- **不提交任何凭据**：token、keystore、签名配置一律留在仓库外；`.gitignore` 已做兜底。
- **逐轮添砖加瓦**：R4 嗅探+翻页 → R5 引擎+队列 → R6 UI+权限，不做一次性成品。
- **合规**：仅处理自己有权保存的内容，遵守站点 ToS / robots（默认开启），不绕 DRM、付费墙、登录墙，不批量盗抓。
