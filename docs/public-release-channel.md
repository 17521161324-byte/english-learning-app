# Public Release Channel

The source repository can stay private. APK files and update metadata are published to a separate public repository so the installed app can check for updates without GitHub authentication.

## Public Repository

Create this repository as public:

```text
17521161324-byte/english-learning-app-release
```

The release workflow publishes:

- `english-learning-app-<version>.apk` to GitHub Releases
- `latest.json` to the same GitHub Release
- `latest.json` to the public repository `main` branch as a fallback feed

## Required Secret

In the private source repository, add this Actions secret:

```text
PUBLIC_RELEASE_TOKEN
```

Use a GitHub token that can write contents to the public release repository. For a fine-grained token, grant repository contents read/write access to `english-learning-app-release`.

Existing Android signing secrets remain in the private source repository:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

## Update URLs

The app checks these public URLs:

```text
https://github.com/17521161324-byte/english-learning-app-release/releases/latest/download/latest.json
https://raw.githubusercontent.com/17521161324-byte/english-learning-app-release/main/latest.json
```

`latest.json` points to the APK asset in the public release repository.

## Migration Note

Versions built before this change still point to the private source repository for updates. Install one bridge version built after this change manually, then future updates can use the in-app update entry.
