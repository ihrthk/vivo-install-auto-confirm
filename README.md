# vivo-install-auto-confirm

自动安装 APK 到 vivo 设备的 Gradle 插件。

## 功能

- 在 `assemble` 任务完成后自动安装 APK
- 支持安装后自动启动应用
- 自动解析 APK 包名和启动 Activity

## 使用方式

在 `app/build.gradle.kts` 中添加：

```kotlin
plugins {
    id("com.github.ihrthk.vivo-install-auto-confirm-plugin") version "1.0.1"
}
```

在 `settings.gradle.kts` 中添加：

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.github.ihrthk.vivo-install-auto-confirm-plugin") {
                useModule("com.github.ihrthk.vivo-install-auto-confirm:vivo-install-auto-confirm:${requested.version}")
            }
        }
    }
}
```

## 运行命令

```bash
# 仅构建，不安装
./gradlew assembleDebug

# 构建后自动安装
./gradlew assembleDebug -Pvivo-auto-install

# 构建后自动安装并启动应用
./gradlew assembleDebug -Pvivo-auto-install -Pvivo-auto-launch
```

## 环境变量

插件会按以下顺序查找 Android SDK 路径：

1. `ANDROID_SDK_ROOT` 环境变量（推荐）
2. `ANDROID_HOME` 环境变量

如果都未设置，可在 `build.gradle.kts` 中配置：

```kotlin
vivoInstall {
    sdkRoot.set("/path/to/android-sdk")
}
```

**建议在 `~/.zshrc` 或 `~/.bash_profile` 中添加：**

```bash
export ANDROID_SDK_ROOT=/path/to/android-sdk
```

## 本地开发

如需本地开发，可在 `settings.gradle.kts` 中添加：

```kotlin
pluginManagement {
    includeBuild("/Users/zhangls/AiProjects/vivo-install-auto-confirm")
}
```

## 链接

- GitHub: https://github.com/ihrthk/vivo-install-auto-confirm
- JitPack: https://jitpack.io/#ihrthk/vivo-install-auto-confirm
