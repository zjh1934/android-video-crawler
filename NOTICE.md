# NOTICE

本仓库自 R4 起包含从 GPL-3.0 项目精简移植的代码，整体按 **GPL-3.0** 分发，
许可证全文见根目录 `LICENSE`。

## 致谢与署名

- **alexch33/super-video-downloader** — GPL-3.0
  <https://github.com/alexch33/super-video-downloader>
  R4 复刻来源：
  `app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/CustomWebViewClient.kt`
  `app/src/main/java/com/myAllVideoBrowser/util/VideoUtils.kt`
  `app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/detectedVideos/IVideoDetector.kt`
  `.../VideoDetectionTabViewModel.kt`
  对应本地文件：`core/model/.../MediaFormat.kt`、`core/sniffer/.../SniffingWebViewClient.kt`、
  `core/sniffer/.../VideoDetector.kt`（各文件头均注明原文件）。

- **JunkFood02/Seal** — Apache-2.0
  <https://github.com/JunkFood02/Seal>
  `gradle/wrapper/gradle-wrapper.jar` 取自该仓库；R5-R6 将参考其下载队列与设置面板结构。

- **yt-dlp/yt-dlp** — Unlicense（打包二进制含 GPLv3+ 组件）
  <https://github.com/yt-dlp/yt-dlp> — R5 作为站点解析引擎接入。

- **JunkFood02/youtubedl-android** — 见其仓库 LICENSE
  <https://github.com/JunkFood02/youtubedl-android> — R5 作为 Android 封装接入。

- **emanuele-f/PCAPdroid** — GPL-3.0
  <https://github.com/emanuele-f/PCAPdroid> — 仅借鉴 VpnService 思路，未引入代码。

## 自写部分

`core/crawler/`（BFS 翻页器、URL 归一化、robots 策略）与 `app/` 的管道编排、
`core/model/` 的参数模型与契约，为本仓库自写。
