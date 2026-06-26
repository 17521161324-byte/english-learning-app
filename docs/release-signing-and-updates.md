# Release Signing And In-App Updates

## Why Signing Matters

Android 只有在新旧 APK 使用同一个签名证书时，才允许覆盖安装更新。Debug APK 适合临时测试，但不适合作为长期迭代更新包。

长期个人使用应该从第一个正式版本开始使用同一套 release keystore。

## Required GitHub Secrets

在 GitHub 仓库中打开：

```text
Settings -> Secrets and variables -> Actions -> New repository secret
```

添加这些 secrets：

```text
ANDROID_KEYSTORE_BASE64
ANDROID_KEYSTORE_PASSWORD
ANDROID_KEY_ALIAS
ANDROID_KEY_PASSWORD
```

`ANDROID_KEYSTORE_BASE64` 是 release keystore 文件的 base64 内容。

## Create A Keystore Locally

安装 JDK 后执行：

```bash
keytool -genkeypair \
  -v \
  -keystore english-learning-release.keystore \
  -alias english-learning \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

生成 base64：

```bash
base64 -i english-learning-release.keystore | pbcopy
```

把剪贴板内容填入 `ANDROID_KEYSTORE_BASE64`。

## Build A Release

在 GitHub Actions 中手动运行：

```text
Android Release APK
```

填写：

```text
version_name: 0.1.1
version_code: 2
changelog: 本次更新说明
```

构建完成后会生成 GitHub Release，并上传：

```text
english-learning-app-0.1.1.apk
latest.json
```

App 内「设置与同步 -> 版本与更新 -> 检查更新」会读取 latest.json。如果发现更高的 `versionCode`，会提示下载新版 APK。

## Update Limit

普通 Android 应用不能静默安装 APK。App 内点击更新后，可以打开 APK 下载页或下载链接，但最终仍需要用户在 Android 系统安装器中确认安装。

