# Android APK Roadmap

## Current Strategy

先做本地可安装 APK，不接后端数据库。学习数据继续保存在本机 IndexedDB 中，后续如需多设备同步，再引入 Supabase 或其他同步服务。

## Build Flow

```bash
npm install
npm run build
npx cap add android
npm run sync
npm run android:apk
```

生成的 debug APK 通常位于：

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

## Required Local Tools

- Node.js and npm
- Java JDK 21
- Android Studio / Android SDK

当前仓库已经具备 Node/npm。打包 APK 前还需要安装 Java JDK 和 Android Studio。

## GitHub Actions Build

仓库包含 `.github/workflows/android-debug-apk.yml`。推送到 `main` 或手动运行该 workflow 后，会自动构建 debug APK，并上传 artifact：

```text
english-learning-debug-apk
```

这适合当前本机还没有 JDK / Android SDK 的阶段。

## Update Strategy

个人自用阶段采用 GitHub Releases 分发：

```text
App 启动
-> 检查 GitHub Releases 中的 latest.json
-> 对比 versionCode
-> 提示下载新版 APK
-> 调起 Android 系统安装器
```

Android 普通应用不能静默安装更新，用户仍需确认安装。

当前 App 已加入「版本与更新」入口，会优先读取：

```text
https://github.com/17521161324-byte/english-learning-app/releases/latest/download/latest.json
```

如果暂无正式 Release，会回退读取仓库内的：

```text
update/latest.json
```

正式更新能力依赖同一个 release keystore。不要用不同签名的 APK 互相覆盖安装。

## Android Permissions

- `INTERNET`: 检查更新、未来同步、在线词典或 AI 能力。
- `RECORD_AUDIO`: 口语练习录音。
- `REQUEST_INSTALL_PACKAGES`: 后续从 GitHub Releases 下载 APK 后调起系统安装器。
