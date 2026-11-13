# android-video-crawler · 站内视频自动嗅探/爬取器

给一个网址，App 用内置浏览器在站内自动翻页、找视频、排队下载。
**当前是骨架（0.1.0-skeleton），不是成品**：能编译出空壳 APK，没有任何真实抓取逻辑。

- 仓库：<https://github.com/zjh1934/android-video-crawler>
- 本地工作区：`/data/workspace/android-video-crawler`，与仓库 `main` 分支同步
- 核心功能不自己写：清单见 [docs/UPSTREAM.md](docs/UPSTREAM.md)
- 架构与权限分档：[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- 每轮进展：[docs/KANBAN.md](docs/KANBAN.md)

## 为什么用 CI 打包

开发沙盒没有 Android SDK，不本地出包。所有构建交给
[`.github/workflows/android-ci.yml`](.github/workflows/android-ci.yml)：
JDK 17 → Android SDK 34 → `./gradlew assembleDebug` → 上传 APK artifact。
在仓库 **Actions** 页打开最新一次运行，下载 `app-debug-skeleton` 即可。

## 目录

```
app/src/main/java/com/zjh1934/videocrawler/
├── MainActivity.kt          骨架状态页（R6 换 Compose）
├── config/CrawlConfig.kt    参数面板数据模型
└── contract/Contracts.kt    四块核心能力的接线契约
docs/                       看板 / 架构 / 上游清单
.github/workflows/          CI 打包
```

## 本地继续开发

Android Studio Ladybug+ / JDK 17 / Android SDK 34：

```bash
git clone https://github.com/zjh1934/android-video-crawler.git
cd android-video-crawler
./gradlew assembleDebug
```

后续每一轮改动都会先在 workspace 落地、再 push 到仓库，并在对话里用代码框贴出流动看板。

## 约定

- **不提交任何凭据**：token、keystore、签名配置一律留在仓库外；`.gitignore` 已做兜底。
- **逐轮添砖加瓦**：R4 嗅探+翻页 → R5 引擎+队列 → R6 UI+权限，不做一次性成品。
- **合规**：仅处理自己有权保存的内容，遵守站点 ToS / robots，不绕 DRM、付费墙、登录墙，不批量盗抓。
- 引入 GPL-3.0 上游代码并分发时，整体需遵循 GPL-3.0；详见 `docs/UPSTREAM.md`。
