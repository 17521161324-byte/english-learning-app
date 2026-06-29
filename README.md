# 网页生词学习系统

一个本地优先的个人英语学习 PWA。目标是把浏览英文网站时遇到的生词沉淀到自己的生词库里，并在 App 内完成复习、口语练习和打卡。

## 原生 Android 开发

新的 Kotlin + Jetpack Compose 客户端位于 [`native-android`](native-android/README.md)，当前版本为 `v0.2.0-alpha`。现有 Web/Capacitor 代码保留为需求和视觉参考，不再作为新功能主线。

## 当前能力

- 中文界面，Notion 式清爽工具感
- 生词新增、编辑、删除、搜索和筛选
- 网页生词库、单词详情、原文语境、个人笔记
- 复习三档标记：不认识、有点模糊、已掌握
- 本地复习记录和下次复习日期
- 口语练习录音和录音记录
- 发音播放
- 每日计划和打卡
- IndexedDB 本地持久保存
- JSON 数据导入、导出
- PWA manifest 和 Service Worker 离线缓存

## 本地运行

项目是静态前端 + Capacitor Android 壳。网页模式可以直接运行：

```bash
python3 -m http.server 4173
```

然后打开：

```text
http://127.0.0.1:4173/
```

不要直接用 `file://` 检查 PWA 能力，因为 Service Worker、安装入口和录音权限在文件模式下不完整。

## Android APK

首次准备：

```bash
npm install
npm run build
npx cap add android
```

同步前端到 Android：

```bash
npm run sync
```

生成 debug APK：

```bash
npm run android:apk
```

生成的 APK 通常位于：

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

打包 APK 需要本机安装 Java JDK 21 和 Android Studio / Android SDK。当前 App 采用本地优先数据策略，不需要后端数据库即可离线使用。

如果本机暂时没有 Android 构建环境，也可以用 GitHub Actions 云端构建。推送到 GitHub 后，打开仓库的 `Actions` 页面，运行 `Android Debug APK` 工作流，完成后在 artifact 中下载 `english-learning-debug-apk`。

## 应用内更新

App 内已经预留「版本与更新」入口，会检查 GitHub Release 中的 `latest.json`。发现更高 `versionCode` 后，用户可以在 App 内点击下载新版 APK。

长期迭代请使用正式签名 APK，而不是 debug APK。Android 要求每次覆盖安装更新都使用同一个签名证书，具体见 [Release Signing And In-App Updates](docs/release-signing-and-updates.md)。

## 数据说明

学习数据默认保存在当前浏览器的 IndexedDB 中，不会上传到服务器。可以在「我的」或「设置与同步」里导出 JSON 备份，也可以导入之前的备份恢复数据。

## 后续路线

项目后续将迁移到 Kotlin + Jetpack Compose 原生 Android 客户端，并按可安装、可验证的版本逐步加入拼词、自动计划、统计、关联词、短语、演讲和 Chrome 插件同步。

详细范围、技术路线和版本验收标准见 [产品功能与版本迭代计划](docs/product-version-roadmap.md)。
