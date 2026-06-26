# Product Notes

## Product Goal

做一个个人长期可用的英语学习工具，先保证本地 App 主体稳定可用，再接 Chrome 插件。核心闭环是：

```text
阅读英文网页 -> 收藏生词 -> App 生词库沉淀 -> 复习 -> 口语使用 -> 打卡复盘
```

## Design Direction

- 中文界面
- Notion 式清爽工具感
- 弱游戏化，偏个人知识库和每日学习工作台
- 黑白灰为主，少量绿色表示完成，琥珀色表示模糊，蓝色表示来源/同步

## Current Technical Shape

- 静态 PWA
- `index.html` 管理基础结构
- `styles.css` 管理视觉系统
- `app.js` 管理应用状态、IndexedDB、录音、导入导出和交互
- `manifest.webmanifest` 提供安装能力
- `service-worker.js` 提供离线缓存

## Next Milestones

1. 提升复习算法和学习统计。
2. 增加 Chrome 插件，将网页划词收藏接入生词库。
3. 选择是否加入云同步。
4. 需要更像原生 App 时，用 Capacitor 封装。

