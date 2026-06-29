# Native Android Alpha

Kotlin + Jetpack Compose 原生客户端，当前对应 `v0.2.0-alpha`。

## 当前能力

- 中文 Notion 工具风格和五入口底部导航
- Room 本地生词与学习事件
- 生词新增、删除和搜索
- DataStore 每日学习目标与提醒开关
- 首页真实数据摘要和学习统计
- GitHub `latest.json` 版本检查
- `dev` 与 `prod` 构建变体

## 构建

本地需要 JDK 17、Android SDK 35：

```bash
cd native-android
./gradlew testDevDebugUnitTest assembleDevDebug
```

APK 输出：

```text
native-android/app/build/outputs/apk/dev/debug/app-dev-debug.apk
```

也可以在 GitHub Actions 手动运行 `Native Android Alpha`，下载 `english-learning-native-alpha` artifact。

开发版包名为 `com.personal.englishlearning.dev.debug`，可以与当前正式 App 同时安装。
