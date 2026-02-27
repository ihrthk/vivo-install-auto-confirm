# gradle-vivo-plugin

自动安装 APK 到 vivo 设备的 Gradle 插件。

## 功能

- 在 `assemble` 任务完成后自动安装 APK
- 支持安装后自动启动应用
- 自动解析 APK 包名和启动 Activity

## 使用方式

在 `app/build.gradle.kts` 中添加：

```kotlin
plugins {
    // ... 其他插件
}

// 应用 Vivo ADB 自动安装插件
buildscript {
    repositories {
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.github.ihrthk.gradle-vivo-plugin:gradle-vivo-plugin:1.0.0")
    }
}

apply(plugin = "com.github.ihrthk.vivo-plugin")
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

如果都未设置，将抛出异常提示配置。

**建议在 `~/.zshrc` 或 `~/.bash_profile` 中添加：**

```bash
export ANDROID_SDK_ROOT=/path/to/android-sdk
```

## 本地开发

如需本地开发，可在 `settings.gradle.kts` 中添加：

```kotlin
pluginManagement {
    includeBuild("/path/to/gradle-vivo-plugin")
}
```

## 链接

- GitHub: https://github.com/ihrthk/gradle-vivo-plugin
- JitPack: https://jitpack.io/#ihrthk/gradle-vivo-plugin
