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
     * 默认从环境变量 ANDROID_SDK_ROOT、ANDROID_HOME 获取，
     * 若都未设置则使用默认路径
     */
    @get:Internal
    abstract val sdkRoot: Property<String>
}
