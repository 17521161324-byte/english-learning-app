# 网页生词学习系统

一个本地优先的个人英语学习 PWA。目标是把浏览英文网站时遇到的生词沉淀到自己的生词库里，并在 App 内完成复习、口语练习和打卡。

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

## 数据说明

学习数据默认保存在当前浏览器的 IndexedDB 中，不会上传到服务器。可以在「我的」或「设置与同步」里导出 JSON 备份，也可以导入之前的备份恢复数据。

## 后续路线

- Chrome 插件：划词释义、收藏单词、保存原句和来源链接
- 云同步：可选接入 Supabase 或其他个人后端
- 手机封装：可选用 Capacitor 打包成 iOS / Android App
- 复习算法：从当前简洁模式升级为更完整的间隔重复
