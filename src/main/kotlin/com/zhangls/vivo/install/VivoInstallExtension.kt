package com.zhangls.vivo.install

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal

// ============================================
// 插件扩展配置
// ============================================
/**
 * Vivo 安装插件扩展配置
 *
 * 用于在 build.gradle.kts 中自定义配置：
 * ```kotlin
 * vivoInstall {
 *     sdkRoot.set("/custom/sdk/path")
 *     autoLaunch.set(true)
 *     waitTime.set(30L)
 * }
 * ```
 */
abstract class VivoInstallExtension {

    // ----------------------------------------
    // 配置属性
    // ----------------------------------------
    /**
     * Android SDK 根路径
     *
     * 默认从环境变量 ANDROID_SDK_ROOT、ANDROID_HOME 获取
     */
    @get:Internal
    abstract val sdkRoot: Property<String>

    /**
     * 安装后是否自动启动应用
     *
     * 默认 true
     */
    @get:Internal
    abstract val autoLaunch: Property<Boolean>

    /**
     * 安装后到启动前的等待时间（秒）
     *
     * 默认 30 秒
     */
    @get:Internal
    abstract val waitTime: Property<Long>
}
