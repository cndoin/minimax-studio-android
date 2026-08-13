# 贡献指南

感谢你愿意改进 MiniMax Studio。

## 提交 Issue

- 先搜索已有 Issue，避免重复提交。
- 描述复现步骤、设备/Android 版本、应用版本和相关日志。
- 不要在 Issue、截图或日志中公开 API Key、账号信息或其他隐私数据。
- 如果问题涉及安全漏洞，请按照 [SECURITY.md](SECURITY.md) 中的说明私下报告，不要直接公开创建 Issue。

## 提交 Pull Request

1. 从最新代码创建分支。
2. 保持修改范围聚焦，并为用户可见的行为变化更新 README。
3. 提交前执行：

   ```powershell
   .\gradlew.bat :app:assembleDebug
   ```

4. 在 Pull Request 中说明修改内容、测试方式和已知限制。

Pull Request 会在 GitHub Actions 中自动执行 Debug 构建检查。请确保检查通过后再请求合并。

## 代码风格

- 使用 Kotlin 官方代码风格。
- 优先保持现有 Compose 和 ViewModel 的组织方式。
- 不要提交构建产物、本地配置、签名文件或 API Key。
