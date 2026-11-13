# 上游核心代码清单（核心功能不自己写）

> 原则：找视频、解析站点、下载流、队列与界面这四块，全部复用成熟开源实现；
> 只有「站内自动翻页策略」和「参数编排」自己写。

| 能力 | 仓库 | 许可证 | 直接搬的文件 / 类 | 接入轮次 |
|---|---|---|---|---|
| 浏览器嗅探 | `alexch33/super-video-downloader` | GPL-3.0 | `CustomWebViewClient.kt`（shouldInterceptRequest）、`VideoUtils.kt`（Content-Type / 文件魔数）、`IVideoDetector.kt`、`VideoDetectionTabViewModel.kt` | R4 |
| 嗅探（轻量备选） | `sniffWebkit` | 见仓库 | `SniffTool` + `MySniffFilter`，aar 直引 | R4 备选 |
| 模块化备选 | `Amisles/V4AW` | 见仓库 | `webview-manager` / `video-parser` / `download-manager` 模块，含按站点自定义解析规则 | R4 备选 |
| 站点解析引擎 | `yt-dlp/yt-dlp` | Unlicense（打包二进制含 GPL 组件需注意） | 不修改源码，通过 Android 封装调用 | R5 |
| Android 封装 | `JunkFood02/youtubedl-android` | 见仓库 | `YoutubeDL.getInstance().init()` / `getInfo()` / `YoutubeDLRequest` / `updateYoutubeDL()` | R5 |
| 下载队列与 UI | `JunkFood02/Seal` | Apache-2.0 | `Downloader.kt`、`DownloadService.kt`、`database/`（Room）、自定义命令模板页 | R5-R6 |
| 全机流量抓取 | `emanuele-f/PCAPdroid` | GPL-3.0 | VpnService 本地 L4 代理思路（仅借鉴，不整包引入） | R6 可选 |

## 自己写的部分

- `CrawlPlanner`：seed 首页 → 同域链接抽取 → 去重 BFS → 逐页交给嗅探器；控制 `maxDepth / maxPages / delayMs / respectRobots`。上游项目都只做「打开某一页嗅这一页」。
- `CrawlConfig` → yt-dlp 命令行的映射层：把用户面板参数翻译成 `-f` / `-o` / `--cookies` / `--proxy` / `--external-downloader aria2c` 等。
- 权限分级开关：按「是否真办事 / 是否过审」分批开启，见 `ARCHITECTURE.md`。

## 许可证约束

- 引入 GPL-3.0 代码并分发时，整体需按 GPL-3.0 开源；单纯内部使用不触发。
- `yt-dlp` 源码为 Unlicense，但 PyInstaller 打包二进制含 GPLv3+ 组件，组合分发按 GPLv3+ 处理。
- Seal 为 Apache-2.0，可较自由地借用结构。
- 本仓库当前为骨架，未复制任何上游源码文件；复制时请保留各自 LICENSE 与署名。
