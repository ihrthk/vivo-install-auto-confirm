package com.zhangls.vivo.install

import org.gradle.api.logging.Logger
import java.io.File

// ============================================
// Vivo 自动安装确认核心逻辑
// ============================================
/**
 * Vivo 自动安装确认处理类
 *
 * 实现类似 Flutter 工具的 vivo 设备自动安装确认功能：
 * 1. 检测是否为 vivo 设备
 * 2. 后台执行安装命令
 * 3. 自动点击复选框和继续安装按钮
 * 4. 输出格式化的日志信息
 *
 * @property adbHelper ADB 辅助类
 * @property config 自动确认配置
 * @property logger Gradle 日志记录器
 */
class VivoAutoInstaller(
    private val adbHelper: AdbHelper,
    private val config: AutoConfirmConfig,
    private val logger: Logger
) {

    // ----------------------------------------
    // 设备检测
    // ----------------------------------------
    /**
     * 检测当前连接的设备是否为 vivo 设备
     *
     * @return 如果是 vivo 设备则返回 true
     */
    fun isVivoDevice(): Boolean {
        val deviceInfo = adbHelper.getDeviceInfo()
        logger.lifecycle("检测到设备: ${deviceInfo.deviceId} (${deviceInfo.manufacturer})")
        return deviceInfo.isVivoDevice()
    }

    // ----------------------------------------
    // 安装流程
    // ----------------------------------------
    /**
     * 使用自动确认方式安装 APK
     *
     * 执行流程：
     * 1. 启动后台安装进程
     * 2. 如果是 vivo 设备且启用自动确认，执行自动确认流程
     * 3. 等待安装完成
     * 4. 输出剩余日志
     *
     * @param apkFile APK 文件
     * @return 安装是否成功
     */
    fun installWithAutoConfirm(apkFile: File): Boolean {
        // 启动后台安装进程
        val process = adbHelper.startInstallProcess(apkFile)

        // 如果是 vivo 设备且启用自动确认，执行自动确认流程
        if (isVivoDevice() && config.enabled) {
            performAutoConfirm()
        }

        // 等待安装进程完成并输出剩余日志
        adbHelper.collectProcessOutput(process) { line ->
            logger.lifecycle(line)
        }

        val exitCode = adbHelper.waitForProcess(process)
        if (exitCode != 0) {
            logger.lifecycle("安装失败，退出码: $exitCode")
            return false
        }

        return true
    }

    // ----------------------------------------
    // 自动确认流程
    // ----------------------------------------
    /**
     * 执行自动确认流程
     *
     * 时序：
     * 1. 等待指定时间（让安装拦截界面出现）
     * 2. 点击复选框两次（确保勾选）
     * 3. 等待 500ms
     * 4. 点击安装按钮两次（确保点击）
     * 5. 等待 1500ms
     */
    private fun performAutoConfirm() {
        logger.lifecycle("开始自动确认流程...")

        // 1. 等待安装界面出现（额外增加 5 秒确保界面完全加载）
        val waitTime = config.waitTime + 5
        logger.lifecycle("等待 $waitTime 秒让安装界面出现...")
        Thread.sleep(waitTime * 1000)

        // 获取屏幕尺寸以计算按钮位置
        val deviceInfo = adbHelper.getDeviceInfo()

        // 2. 点击复选框
        logger.lifecycle("点击复选框 (${config.checkboxX}, ${config.checkboxY})...")
        adbHelper.tap(config.checkboxX, config.checkboxY)

        // 3. 等待 500ms
        Thread.sleep(500)

        // 4. 点击安装按钮
        val buttonX = if (deviceInfo.hasScreenSize()) deviceInfo.screenWidth / 2 else 540
        val buttonY = if (deviceInfo.hasScreenSize()) {
            (deviceInfo.screenHeight * config.buttonYPercent).toInt()
        } else {
            (config.buttonYPercent * 2400).toInt()
        }
        logger.lifecycle("点击安装按钮 ($buttonX, $buttonY)...")
        adbHelper.tap(buttonX, buttonY)

        // 5. 等待 1500ms
        Thread.sleep(1500)

        logger.lifecycle("自动确认流程完成")
    }
}
