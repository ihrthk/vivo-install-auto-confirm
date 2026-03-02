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

    // ----------------------------------------
    // 自动确认配置属性
    // ----------------------------------------
    /**
     * 是否启用 vivo 设备自动确认安装
     *
     * 默认 true
     */
    @get:Internal
    abstract val autoConfirm: Property<Boolean>

    /**
     * 等待安装界面出现的时间（秒）
     *
     * 默认 30 秒
     */
    @get:Internal
    abstract val autoConfirmWaitTime: Property<Long>

    /**
     * 复选框点击 X 坐标
     *
     * 默认 365
     */
    @get:Internal
    abstract val checkboxX: Property<Int>

    /**
     * 复选框点击 Y 坐标
     *
     * 默认 2270
     */
    @get:Internal
    abstract val checkboxY: Property<Int>

    /**
     * 安装按钮 Y 坐标占屏幕高度的百分比
     *
     * 默认 0.93（93%）
     */
    @get:Internal
    abstract val buttonYPercent: Property<Float>
}
