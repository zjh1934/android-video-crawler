# 上游核心代码清单（核心功能不自己写）

> 原则：找视频、解析站点、下载流、队列与界面这四块，全部复用成熟开源实现；
> 只有「站内自动翻页策略」和「参数编排」自己写。

| 能力 | 仓库 | 许可证 | 直接搬的文件 / 类 | 状态 |
|---|---|---|---|---|
| 浏览器嗅探 | `alexch33/super-video-downloader` | GPL-3.0 | `CustomWebViewClient.kt`（shouldInterceptRequest）、`VideoUtils.kt`、`IVideoDetector.kt`、`VideoDetectionTabViewModel.kt` | ✅ R4 已复刻精简版 |
| 嗅探（轻量备选） | `sniffWebkit` | 见仓库 | `SniffTool` + `MySniffFilter`，aar 直引 | 备选 |
| 模块化备选 | `Amisles/V4AW` | 见仓库 | `webview-manager` / `video-parser` / `download-manager`，含按站点自定义解析规则 | 备选 |
| 站点解析引擎 | `yt-dlp/yt-dlp` | Unlicense（打包二进制含 GPL 组件需注意） | 不修改源码，通过 Android 封装调用 | ⏳ R5 |
| Android 封装 | `JunkFood02/youtubedl-android` | 见仓库 | `YoutubeDL.init()` / `getInfo()` / `YoutubeDLRequest` / `updateYoutubeDL()` | ⏳ R5 |
| 下载队列与 UI | `JunkFood02/Seal` | Apache-2.0 | `Downloader.kt`、`DownloadService.kt`、`database/`（Room）、命令模板页 | ⏳ R5-R6 |
| 全机流量抓取 | `emanuele-f/PCAPdroid` | GPL-3.0 | VpnService 本地 L4 代理思路（仅借鉴） | R6 可选 |

## R4 实际复刻对照

上游文件与本地落点一一对应，均去掉了项目内耦合（OkHttpProxyClient / RxJava / DataBinding / ViewModel）：

| 上游原文件 | 本地落点 | 保留的判定口径 |
|---|---|---|
| `util/VideoUtils.kt` | `core/model/.../MediaFormat.kt` | `.js/.css/.m4s/.ts` 排除；`mpegurl / dash+xml / video / audio` Content-Type；`application/octet-stream` 读前 7 字节判 `#EXTM3U` / `<MPD` |
| `browser/CustomWebViewClient.kt` | `core/sniffer/.../SniffingWebViewClient.kt` | 主帧跳过、子资源全过一遍、请求头透传、**返回 null 不吞响应**、`Accept` 判资源类型 |
| `detectedVideos/IVideoDetector.kt` + `VideoDetectionTabViewModel.kt` | `core/sniffer/.../VideoDetector.kt` | 按 URL 去重、异步校验链接、命中推给候选列表；改为单线程 Executor 做 Range 探测，避免阻塞 WebView 回调 |

`gradle/wrapper/gradle-wrapper.jar` 取自 `JunkFood02/Seal`（R3 首次拿到的 gradle/gradle 版本是损坏的空文件）。

## 自己写的部分

- `core/crawler/BfsCrawlPlanner`：seed 首页 → Jsoup 抽同域链接 → 去重 BFS → 逐页交给嗅探器；控制 `maxDepth / maxPages / delayMs / respectRobots`。上游项目都只做「打开某一页嗅这一页」。
- `core/crawler/UrlNormalizer` / `RobotsPolicy`：URL 归一化、范围判定、精简 robots.txt。
- `CrawlConfig` → yt-dlp 命令行的映射层（R5 落地）：把用户面板参数翻译成 `-f` / `-o` / `--cookies` / `--proxy` / `--external-downloader aria2c` 等。
- 权限分级开关：按「是否真办事 / 是否过审」分批开启，见 `ARCHITECTURE.md`。

## 许可证约束

- **本仓库整体按 GPL-3.0 分发**（R4 起引入上游 GPL-3.0 代码），根目录 `LICENSE` 为 GPLv3 全文，`NOTICE.md` 保留署名。
- `yt-dlp` 源码为 Unlicense，但 PyInstaller 打包二进制含 GPLv3+ 组件，组合分发按 GPLv3+ 处理。
- Seal 为 Apache-2.0，可较自由地借用结构。
- 复制上游源码时保留各自 LICENSE 与作者署名；本仓库采用「精简移植 + 文件头注明原文件」而非整文件复制，以避免引入不必要的项目内依赖。
