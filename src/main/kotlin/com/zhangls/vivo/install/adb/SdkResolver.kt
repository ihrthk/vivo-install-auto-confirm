package com.zhangls.vivo.install.adb

import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import java.io.File

// ============================================
// SDK 路径解析器
// ============================================
/**
 * SDK 路径解析器
 *
 * 负责解析 Android SDK 路径，包括 aapt 和 adb 可执行文件的位置
 *
 * @property sdkRoot 可选的 SDK 根路径配置
 */
class SdkResolver(
    private val sdkRoot: Property<String>?
) {

    // ----------------------------------------
    // SDK 根路径解析
    // ----------------------------------------
    /**
     * 获取 Android SDK 根路径
     *
     * 优先使用配置值，其次从环境变量 ANDROID_SDK_ROOT、ANDROID_HOME 获取，
     * 若都未设置则抛出异常
     *
     * @return SDK 根路径
     * @throws GradleException 无法解析 SDK 路径时抛出异常
     */
    fun resolveEffectiveSdkRoot(): String {
        return sdkRoot?.orNull
            ?: System.getenv("ANDROID_SDK_ROOT")
            ?: System.getenv("ANDROID_HOME")
            ?: throw GradleException(
                "未配置 Android SDK 路径。请设置环境变量 ANDROID_SDK_ROOT，" +
                "或在 build.gradle.kts 中配置：\n" +
                "vivoInstall {\n" +
                "    sdkRoot.set(\"/path/to/sdk\")\n" +
                "}"
            )
    }

    // ----------------------------------------
    // aapt 路径解析
    // ----------------------------------------
    /**
     * 获取 aapt 可执行文件路径
     *
     * 优先查找 aapt，若不存在则查找 aapt2
     *
     * @return aapt 完整路径
     * @throws GradleException 未找到 aapt 时抛出异常
     */
    fun resolveAaptPath(): String {
        val sdkRoot = resolveEffectiveSdkRoot()
        val buildToolsDir = File("$sdkRoot/build-tools")

        if (!buildToolsDir.exists()) {
            throw GradleException(
                "未找到 build-tools 目录: $buildToolsDir\n" +
                "请确保 Android SDK 已正确安装"
            )
        }

        val buildToolsVersions = buildToolsDir.listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.name }
            ?.sortedDescending()
            ?: emptyList()

        // 查找最新版本的 build-tools
        for (version in buildToolsVersions) {
            val aaptFile = File("$sdkRoot/build-tools/$version/aapt")
            if (aaptFile.exists()) {
                return aaptFile.absolutePath
            }
            val aapt2File = File("$sdkRoot/build-tools/$version/aapt2")
            if (aapt2File.exists()) {
                return aapt2File.absolutePath
            }
        }

        throw GradleException(
            "未找到 aapt 或 aapt2，请确保 Android SDK Build Tools 已正确安装"
        )
    }

    // ----------------------------------------
    // adb 路径解析
    // ----------------------------------------
    /**
     * 获取 ADB 可执行文件路径
     *
     * @return adb 完整路径
     * @throws GradleException 未找到 ADB 时抛出异常
     */
    fun resolveAdbPath(): String {
        val sdkRoot = resolveEffectiveSdkRoot()
        val adbFile = File("$sdkRoot/platform-tools/adb")

        if (!adbFile.exists()) {
            throw GradleException(
                "未找到 ADB: ${adbFile.absolutePath}\n" +
                "请确保 Android SDK Platform Tools 已正确安装"
            )
        }

        return adbFile.absolutePath
    }
}
