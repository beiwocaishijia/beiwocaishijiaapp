# 被窝健身 - Android APP 项目说明

这是一个基础的健身 Android APP 项目模板。

## 如何开始

1. **安装软件**:
   - 安装 [JDK 17](https://adoptium.net/temurin/releases/?version=17)。
   - 下载并安装 [Android Studio](https://developer.android.com/studio)。

2. **打开项目**:
   - 启动 Android Studio。
   - 选择 `Open`，定位到本文件夹 (`d:\beiwojianshenapp`) 并打开。
   - 等待 Gradle 构建完成 (第一次可能需要几分钟，取决于网速)。

3. **连接小米 15 手机**:
   - 开启手机的 `开发者选项` 和 `USB 调试`。
   - 开启 `USB 安装`。
   - 使用数据线连接电脑，并点击手机上的 "允许调试"。

4. **运行 APP**:
   - 在 Android Studio 顶部点击绿色运行按钮。
   - 手机上会安装并运行一个显示 "欢迎使用被窝健身！" 的界面。

## 项目结构说明

- `app/src/main/java/com/example/beiwojianshen/MainActivity.kt`: 主活动逻辑。
- `app/src/main/res/layout/activity_main.xml`: 主界面布局。
- `app/build.gradle`: 模块级构建配置。
- `settings.gradle`: 项目配置。
