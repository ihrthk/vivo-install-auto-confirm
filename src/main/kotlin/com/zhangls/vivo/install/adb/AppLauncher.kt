package com.zhangls.vivo.install.adb

import com.zhangls.vivo.install.model.ApkInfo
import org.gradle.api.logging.Logger
import java.io.File

// ============================================
// 应用启动器
// ============================================
/**
 * 应用启动器
 *
 * 通过 ADB 启动已安装应用的主页面
 *
 * @property adbPath ADB 可执行文件路径
 * @property logger 日志记录器
 */
class AppLauncher(
    private val adbPath: String,
    private val logger: Logger
) {

    // ----------------------------------------
    // 应用启动
    // ----------------------------------------
    /**
     * 通过 ADB 启动应用主页面
     *
     * 使用 adb shell am start 命令启动应用
     *
     * @param apkInfo APK 应用信息
     * @return 启动是否成功
     */
    fun launch(apkInfo: ApkInfo): Boolean {
        logger.lifecycle("正在启动应用: ${apkInfo.packageName}")

        val process = ProcessBuilder(
            adbPath, "shell", "am", "start",
            "${apkInfo.packageName}/${apkInfo.launchableActivity}"
        )
            .redirectErrorStream(true)
            .start()

        // 输出 ADB 启动日志
        process.inputStream.bufferedReader().use { reader ->
            reader.lines().forEach { line ->
                logger.lifecycle(line)
            }
        }

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            logger.warn("启动应用失败，退出码: $exitCode")
            return false
        }
        return true
    }
}
