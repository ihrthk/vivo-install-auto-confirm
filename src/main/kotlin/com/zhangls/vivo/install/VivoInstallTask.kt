package com.zhangls.vivo.install

import com.zhangls.vivo.install.adb.AdbHelper
import com.zhangls.vivo.install.adb.ApkParser
import com.zhangls.vivo.install.adb.AppLauncher
import com.zhangls.vivo.install.adb.SdkResolver
import com.zhangls.vivo.install.adb.VivoAutoInstaller
import com.zhangls.vivo.install.model.ApkInfo
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * vivo 设备安装任务
 *
 * 负责协调 APK 安装和启动流程，具体操作委托给专用类处理
 */
open class VivoInstallTask : DefaultTask() {

    // ----------------------------------------
    // 任务属性
    // ----------------------------------------
    /**
     * 构建变体名称（如 "Debug"、"Release"）
     */
    @Internal
    lateinit var variantName: String

    /**
     * Vivo 安装插件扩展配置
     */
    @Internal
    lateinit var extension: VivoInstallExtension

    // ----------------------------------------
    // 任务执行
    // ----------------------------------------
    @TaskAction
    fun launch() {
        val apkFile = findApkFile(variantName) ?: run {
            project.logger.warn("未找到 ${variantName} 变体的 APK 文件，跳过安装")
            return
        }

        val apkInfo = installApk(apkFile)
        launchAppIfConfigured(apkInfo)
    }

    // ----------------------------------------
    // APK 操作
    // ----------------------------------------
    /**
     * 查找指定变体的 APK 文件
     *
     * @param variantName 构建变体名称（如 "Debug"、"Release"）
     * @return APK 文件，未找到则返回 null
     */
    protected fun findApkFile(variantName: String): File? {
        val projectDir = project.projectDir
        val apkDir = File(projectDir, "build/outputs/apk/${variantName.lowercase()}")

        if (!apkDir.exists()) {
            // 尝试查找子目录中的 APK（如 product-flavors）
            val parentDir = File(projectDir, "build/outputs/apk")
            if (parentDir.exists()) {
                val apks = parentDir.walkTopDown()
                    .filter { it.extension == "apk" }
                    .filter { it.absolutePath.contains(variantName.lowercase()) }
                    .toList()
                return apks.firstOrNull()
            }
            return null
        }

        val apks = apkDir.listFiles()?.filter { it.extension == "apk" }
        return apks?.firstOrNull()
    }

    // ----------------------------------------
    // 安装与启动
    // ----------------------------------------
    /**
     * 通过 ADB 安装 APK 到设备
     *
     * 使用自动确认方式安装（后台安装 + 自动点击确认）
     *
     * @param apkFile 要安装的 APK 文件
     * @return APK 应用信息
     * @throws org.gradle.api.GradleException 安装失败时抛出异常
     */
    protected fun installApk(apkFile: File): ApkInfo {
        val sdkResolver = SdkResolver(extension.sdkRoot)
        val adbPath = sdkResolver.resolveAdbPath()
        val aaptPath = sdkResolver.resolveAaptPath()
        val ext = extension

        project.logger.lifecycle("正在安装: ${apkFile.name}")

        val adbHelper = AdbHelper(adbPath, project.logger)
        val autoInstaller = VivoAutoInstaller(
            adbHelper = adbHelper,
            waitTime = ext.autoConfirmWaitTime.getOrElse(5L),
            checkboxX = ext.checkboxX.getOrElse(365),
            checkboxY = ext.checkboxY.getOrElse(2270),
            buttonYPercent = ext.buttonYPercent.getOrElse(0.93f),
            logger = project.logger
        )

        val success = autoInstaller.installWithAutoConfirm(apkFile)
        if (!success) {
            throw GradleException("adb 安装失败")
        }

        // 使用 ApkParser 解析应用信息
        val apkParser = ApkParser(aaptPath, project.logger)
        return apkParser.parse(apkFile)
    }

    // ----------------------------------------
    // 私有辅助方法
    // ----------------------------------------
    /**
     * 根据配置启动应用
     */
    private fun launchAppIfConfigured(apkInfo: ApkInfo) {
        val config = extension
        if (!config.autoLaunch.getOrElse(false)) return

        val sdkResolver = SdkResolver(extension.sdkRoot)
        val adbPath = sdkResolver.resolveAdbPath()
        val appLauncher = AppLauncher(adbPath, project.logger)
        appLauncher.launch(apkInfo)
    }
}