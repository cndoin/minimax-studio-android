# MiniMax Studio

MiniMax Studio 是一个面向 Android 的 MiniMax 全模态客户端，使用 Kotlin、Jetpack Compose 和 Material 3 构建。

项目目前处于早期阶段，欢迎试用、反馈和提交改进。

## 功能

- 文本对话：MiniMax-M2.7、M2.5、M2.1、M3
- 文生图：比例选择与批量生成
- 文生视频：Hailuo-2.3、MiniMax-H3，以及任务状态轮询
- 语音合成：Speech 2.8 HD
- 音乐生成：Music 3.0 与歌词
- 视觉理解：公网图片 URL / `file_id`
- Token Plan 网络搜索与用量查询
- 国内 / 国际区域切换
- 使用 Android Keystore 在本地加密保存 API Key

## 环境要求

- Android Studio（推荐使用最新稳定版）
- JDK 17
- Android SDK 35
- Android 8.0（API 26）或更高版本的设备/模拟器

## 快速开始

1. 克隆仓库并用 Android Studio 打开项目根目录。
2. 等待 Gradle 同步完成。
3. 运行 `app` 配置，或执行：

   ```powershell
   .\gradlew.bat :app:assembleDebug
   ```

4. 构建产物位于 `app/build/outputs/apk/debug/app-debug.apk`。

首次使用时，在应用的“设置”中填写自己的 MiniMax API Key，并选择对应的国内或国际区域。

## API Key 与隐私

- 本项目不提供或内置 MiniMax API Key。
- API 请求由应用直接发送到 MiniMax 服务，使用者需要自行承担账号、额度和网络请求产生的费用。
- API Key 只保存在当前设备的本地存储中，并通过 Android Keystore 加密。
- 请勿把 API Key、签名文件或 `local.properties` 提交到公开仓库。

## 项目结构

```text
app/
├── src/main/java/com/minimax/mobile/
│   ├── data/       # API、数据模型与 API Key 存储
│   ├── ui/         # ViewModel 与 Compose 界面
│   └── MainActivity.kt
└── src/main/res/   # Android 资源
```

## 参与贡献

提交 Issue 或 Pull Request 前，请先阅读 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 许可证

本项目采用 [MIT License](LICENSE)。

## 免责声明

本项目是 MiniMax API 的第三方客户端，不代表 MiniMax 官方立场。MiniMax API 的可用模型、接口、地区和计费规则可能发生变化，请以官方文档为准。
