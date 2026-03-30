# vivo-install-auto-confirm

自动安装 APK 到 vivo 设备的 Gradle 插件。

## 功能

- 在 `assemble` 任务完成后自动安装 APK
- 支持安装后自动启动应用
- 自动解析 APK 包名和启动 Activity

## 使用方式

### 1. 发布插件到本地 Maven

```bash
cd /Users/zhangls/AiProjects/vivo-install-auto-confirm
./gradlew publishToMavenLocal
```

### 2. 在项目中使用

在 `settings.gradle.kts` 中添加本地 Maven 仓库：

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}
```

在 `app/build.gradle.kts` 中添加插件：

```kotlin
plugins {
    id("io.github.ihrthk.vivo-install-auto-confirm-plugin") version "1.0.3"
}
```

### 3. 配置插件（默认不需要）

在 `app/build.gradle.kts` 中配置插件行为：

```kotlin
vivoInstall {
    // ----------------------------------------
    // 基础配置
    // ----------------------------------------

    // Android SDK 根路径（可选）
    // 默认从环境变量 ANDROID_SDK_ROOT 或 ANDROID_HOME 获取
    sdkRoot.set("/path/to/android-sdk")

    // 安装后是否自动启动应用（可选）
    // 默认：true
    autoLaunch.set(true)

    // ----------------------------------------
    // 自动确认配置
    // ----------------------------------------

    // 等待安装界面出现的时间（秒）
    // 默认：5 秒
    // 可根据设备性能调整，较慢的设备可适当增加
    autoConfirmWaitTime.set(5L)

    // 复选框点击 X 坐标
    // 默认：365
    // vivo 安装界面"我已了解"复选框的横坐标位置
    checkboxX.set(365)

    // 复选框点击 Y 坐标
    // 默认：2270
    // vivo 安装界面"我已了解"复选框的纵坐标位置
    checkboxY.set(2270)

    // 安装按钮 Y 坐标占屏幕高度的百分比
    // 默认：0.93（93%）
    // 用于计算"继续安装"按钮的点击位置（适应不同屏幕高度）
    buttonYPercent.set(0.93f)
}
```

## 运行命令

```bash
# 仅构建，不安装
./gradlew assembleDebug

# 构建后自动安装
./gradlew assembleDebug -Pvivo-install
```

## 环境变量

插件会按以下顺序查找 Android SDK 路径：

1. `ANDROID_SDK_ROOT` 环境变量（推荐）
2. `ANDROID_HOME` 环境变量

**建议在 `~/.zshrc` 或 `~/.bash_profile` 中添加：**

```bash
export ANDROID_SDK_ROOT=/path/to/android-sdk
```

## 本地开发

修改插件代码后，重新发布到本地 Maven：

```bash
./gradlew publishToMavenLocal
```

使用项目会自动获取最新版本（SNAPSHOT）或更新版本号。

## 链接

- GitHub: https://github.com/ihrthk/vivo-install-auto-confirm
