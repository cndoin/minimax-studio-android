# MiniMax Studio

![Android](https://img.shields.io/badge/Android-API%2026%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
[![Build](https://github.com/cndoin/minimax-studio-android/actions/workflows/android.yml/badge.svg)](https://github.com/cndoin/minimax-studio-android/actions/workflows/android.yml)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

一个简洁、原生、开源的 Android MiniMax 全模态客户端。

MiniMax Studio 将文本对话、图片、视频、语音、音乐、视觉理解和网络搜索集中在一个 Android 应用中。API 请求由应用直接发送到 MiniMax，API Key 使用 Android Keystore 加密保存在本机。

> 项目处于早期版本，接口和模型支持可能随 MiniMax 开放平台变化。欢迎体验、反馈和贡献。

## 直接下载

不想自己构建？可以直接下载已构建好的 Android 安装包：

**[下载 MiniMax Studio v1.0.0 APK](downloads/MiniMaxStudio-v1.0.0-debug.apk)**

安装前请在 Android 设置中允许当前浏览器或文件管理器安装应用。这个 APK 是未签名的 Debug 构建，适合体验和测试；正式分发前请等待项目提供签名的 Release 构建。APK 文件保存在仓库的 [`downloads/`](downloads/) 目录中。

文件校验：

```text
SHA-256: 7B1CC746A889BCA1BA0260B26ECF1CEDB0BBF7EE8C6940E7E3C5E8E09A70BA2B
```

## 功能一览

| 模块 | 能力 |
| --- | --- |
| 对话 | MiniMax-M2.7、M2.5、M2.1、M3 |
| 图片 | 文生图、画幅选择、1–4 张批量生成 |
| 视频 | Hailuo-2.3、MiniMax-H3、任务轮询与结果打开 |
| 语音 | Speech 2.8 HD，支持试听返回的本地音频 |
| 音乐 | Music 3.0、描述与歌词 |
| 视觉 | 公网图片 URL / `file_id` 图片理解 |
| 搜索 | Token Plan 网络搜索与结果跳转 |
| 账户 | 国内 / 国际区域切换、Token Plan 用量查询 |
| 安全 | Android Keystore 加密保存 API Key，不内置任何密钥 |

## 运行要求

- Android Studio 最新稳定版
- JDK 17
- Android SDK 35
- Android 8.0（API 26）或更高版本
- 一个可用的 MiniMax API Key

## 快速开始

```powershell
git clone https://github.com/cndoin/minimax-studio-android.git
cd minimax-studio-android
.\gradlew.bat :app:assembleDebug
```

构建完成后，APK 位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

也可以直接用 Android Studio 打开仓库根目录，等待 Gradle 同步后运行 `app` 配置。

首次启动后，打开“设置”，填写自己的 MiniMax API Key，并选择 API Key 对应的服务区域。

## API Key、隐私与费用

- 本项目不提供、不收集、也不内置 MiniMax API Key。
- API 请求从应用直接发送到 MiniMax 服务，不经过本项目的自有服务器。
- API Key 通过 Android Keystore 加密后保存在应用私有存储中。
- 如果设备无法使用 Android Keystore，应用不会把 API Key 回退保存为明文。
- MiniMax API 的调用可能产生费用，账号、额度和网络费用由使用者自行承担。
- 请勿在 Issue、截图、日志或 Pull Request 中公开 API Key、账号信息或个人数据。

## 项目结构

```text
.
├── app/
│   └── src/main/
│       ├── java/com/minimax/mobile/
│       │   ├── data/             # API 客户端、模型、Keystore 存储
│       │   ├── ui/               # ViewModel 与主题
│       │   └── MainActivity.kt   # Compose 入口与界面
│       └── res/                  # Android 资源、图标与主题
├── .github/
│   ├── ISSUE_TEMPLATE/           # Issue 表单
│   ├── workflows/                # CI 构建检查
│   └── pull_request_template.md
├── gradle/wrapper/               # Gradle Wrapper
├── CONTRIBUTING.md
├── SECURITY.md
└── README.md
```

## 开发与贡献

欢迎提交 Bug、功能建议和 Pull Request。请先阅读：

- [贡献指南](CONTRIBUTING.md)
- [安全策略](SECURITY.md)
- [变更记录](CHANGELOG.md)

本地提交前建议运行：

```powershell
.\gradlew.bat :app:assembleDebug
```

GitHub Actions 会在推送和 Pull Request 时自动执行 Debug 构建。

## 常见问题

### 为什么应用提示 API Key 无效？

请检查 API Key 是否完整、是否选择了正确的国内/国际区域，以及对应账号是否有可用额度。

### 为什么生成的视频需要等待？

视频接口是异步任务。应用会自动轮询任务状态，最长等待约 144 秒；超时后可以到 MiniMax 控制台查看任务。

### 生成的图片或视频链接为什么会失效？

媒体地址由 MiniMax 返回，具体有效期由 MiniMax 服务决定。重要内容请及时保存或下载。

## 免责声明

MiniMax Studio 是 MiniMax API 的第三方客户端，不代表 MiniMax 官方立场。模型名称、接口路径、区域、返回格式和计费规则可能变化，请以 [MiniMax 官方文档](https://platform.minimaxi.com/docs) 为准。

## 许可证

本项目采用 [MIT License](LICENSE)。
