# gradle-vivo-plugin

自动安装 APK 到 vivo 设备的 Gradle 插件。

## 功能

- 在 `assemble` 任务完成后自动安装 APK
- 支持安装后自动启动应用
- 自动解析 APK 包名和启动 Activity

## 使用方式

### 1. 添加 JitPack 仓库

在 `settings.gradle.kts` 中：

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

### 2. 应用插件

在 `app/build.gradle.kts` 中：

```kotlin
plugins {
    id("com.github.ihrthk.vivo-plugin") version "1.0.0"
}
```

### 3. 配置（可选）

如果 SDK 路径未在环境变量中设置，可以在 `app/build.gradle.kts` 中配置：

```kotlin
vivoInstall {
    sdkRoot.set("/path/to/android-sdk")
}
```

### 4. 运行命令

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

1. `vivoInstall.sdkRoot` 配置值
2. `ANDROID_SDK_ROOT` 环境变量
3. `ANDROID_HOME` 环境变量

如果都未设置，将抛出异常提示配置。

## 任务

插件会为每个 assemble 变体创建对应的安装任务：

- `vivoInstallDebug` - 安装 Debug APK
- `vivoInstallRelease` - 安装 Release APK
