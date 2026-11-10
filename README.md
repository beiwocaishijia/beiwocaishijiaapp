# 被窝健身 · Android App

按日期记录力量训练、有氧运动和体重，并查看体重与训练重量的变化趋势。首页是月历，点击日期进入当天的记录页面。

## 当前功能

- **月历**：切换月份、打开指定日期；日期格中的训练部位来自当天的力量训练记录。
- **力量训练**：选择动作，记录组数、重量，以及可选的次数和备注。
- **有氧运动**：记录类型、时长，以及可选的距离、热量和备注。
- **体重**：按日期添加体重记录。
- **动作管理**：新增、编辑动作，或将动作停用、重新启用。
- **趋势图表**：查看 7 天、30 天、90 天或全部记录。体重图取每天最后一条体重；力量图取所选动作每天的最大训练重量。
- **个性化**：修改应用内显示名称、选择或清除背景图片。

记录通过 Room 保存到设备本地数据库。应用内名称设置只改变应用内标题，不会重命名手机桌面的应用图标。

## 开发环境

| 项目 | 当前配置 |
| --- | --- |
| 开发工具 | [Android Studio](https://developer.android.com/studio) |
| Gradle 使用的 JDK | [JDK 17](https://adoptium.net/temurin/releases/?version=17) |
| Android SDK | SDK Platform 34，`compileSdk` / `targetSdk` 均为 34 |
| 运行设备 | Android API 24 或以上的真机或模拟器 |
| Android Gradle Plugin | 8.2.0 |
| Gradle Wrapper | 8.9，使用仓库自带的 `gradlew` / `gradlew.bat` |
| Kotlin | 1.9.0 |

首次同步需要联网下载 Gradle 和 Maven 依赖。版本配置见 [根构建文件](build.gradle)、[App 构建文件](app/build.gradle) 和 [Wrapper 配置](gradle/wrapper/gradle-wrapper.properties)。

## 下载并打开项目

安装 Git 后，在自己选择的开发目录打开终端，执行：

```powershell
git clone https://github.com/beiwocaishijia/beiwocaishijiaapp.git
cd beiwocaishijiaapp
```

也可以在仓库页面选择 **Code → Download ZIP** 并解压。

1. 打开 Android Studio，选择 **Open**。
2. 选择下载或解压后的项目根目录，即包含 `settings.gradle` 和 `gradlew.bat` 的目录；不需要使用作者电脑上的路径。
3. 在 Android Studio 的 Gradle 设置中将 **Gradle JDK** 设为 JDK 17。
4. 在 **SDK Manager** 中安装 Android SDK Platform 34 和 Android SDK Platform-Tools，然后完成 Gradle Sync。

SDK 路径由本机 Android Studio 配置；`local.properties` 已被 Git 忽略，不要把个人电脑的 SDK 路径提交到仓库。

## 运行到手机或模拟器

### Android 真机

1. 使用支持数据传输的 USB 线连接手机，在手机中开启开发者选项和 USB 调试。
2. 接受手机弹出的“允许 USB 调试”提示。如果手机系统另外要求“USB 安装”，按该设备的设置开启。
3. 在 Android Studio 的设备列表中选择手机，运行配置选择 `app`，点击 **Run**。

项目不限定小米 15；请使用满足最低 Android API 版本的设备。

### Android 模拟器

在 Android Studio 的 **Device Manager** 中创建并启动 API 24 或以上的虚拟设备，再选择该设备运行 `app`。

### 首次使用

1. 启动后进入月历，点击一个日期。
2. 在“训练”“有氧”“体重”三个页签中添加相应记录；需要新动作时可从当天页面的菜单进入动作管理。
3. 从当天页面的菜单进入“图表”，选择时间范围和训练动作，查看已有记录。

## 命令行构建（可选）

先完成 JDK 17 和 Android SDK 配置，再在项目根目录执行。以下是构建入口，不代表仓库已提供预编译安装包。

Windows PowerShell：

```powershell
.\gradlew.bat :app:assembleDebug
```

macOS / Linux：

```bash
sh ./gradlew :app:assembleDebug
```

构建成功后的调试 APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。

## 常见问题

| 现象 | 排查方式 |
| --- | --- |
| Gradle 提示 Java 版本不匹配 | 检查 Android Studio 的 Gradle JDK；命令行构建还需检查 `JAVA_HOME` 和 `java -version`。使用 JDK 17。 |
| `SDK location not found` 或缺少 Android 34 | 在 SDK Manager 安装 Platform 34，确认 Android Studio 已识别本机 SDK 路径。 |
| 首次同步下载失败 | 根据报错区分 Gradle 分发包、Google Maven 或 Maven Central 的网络问题，恢复连接后重新同步。 |
| 设备列表没有手机 | 检查 USB 数据线、USB 调试授权和厂商驱动；也可以先用模拟器运行。 |
| 图表没有数据 | 先确认所选时间范围内存在体重记录或所选动作的力量训练记录。 |

## 项目结构

| 路径 | 作用 |
| --- | --- |
| [MainActivity.kt](app/src/main/java/com/example/beiwojianshen/MainActivity.kt) | 月历首页、应用内名称和背景设置 |
| [DayDetailActivity.kt](app/src/main/java/com/example/beiwojianshen/DayDetailActivity.kt) | 当天训练、有氧、体重页签 |
| [ChartsActivity.kt](app/src/main/java/com/example/beiwojianshen/ChartsActivity.kt) | 体重与力量趋势图 |
| [ExercisesActivity.kt](app/src/main/java/com/example/beiwojianshen/ExercisesActivity.kt) | 动作管理 |
| [data/](app/src/main/java/com/example/beiwojianshen/data/) | Room 数据库、实体、DAO 与本地偏好 |
| [ui/](app/src/main/java/com/example/beiwojianshen/ui/) | 日历、记录列表与图表组件 |
| [res/layout/](app/src/main/res/layout/) | XML 页面布局 |
| [app/build.gradle](app/build.gradle) | App SDK、依赖和构建配置 |
