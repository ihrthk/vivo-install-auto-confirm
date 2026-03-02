package com.zhangls.vivo.install.adb

import com.zhangls.vivo.install.model.ApkInfo
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import java.io.File

// ============================================
// APK 信息解析器
// ============================================
/**
 * APK 信息解析器
 *
 * 使用 aapt 工具从 APK 文件中解析应用信息（包名和启动 Activity）
 *
 * @property aaptPath aapt 可执行文件路径
 * @property logger 日志记录器
 */
class ApkParser(
    private val aaptPath: String,
    private val logger: Logger
) {

    // ----------------------------------------
    // APK 解析
    // ----------------------------------------
    /**
     * 解析 APK 文件获取应用信息
     *
     * 使用 aapt dump badging 命令解析 APK，提取包名和启动 Activity
     *
     * @param apkFile APK 文件
     * @return APK 信息（包名和启动 Activity）
     * @throws GradleException 解析失败时抛出异常
     */
    fun parse(apkFile: File): ApkInfo {
        val process = ProcessBuilder(aaptPath, "dump", "badging", apkFile.absolutePath)
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            throw GradleException("aapt 解析 APK 失败: $output")
        }

        // 解析包名：package: name='com.xxx.xxx' ...
        val packagePattern = """package:\s+name='([^']+)'""".toRegex()
        val packageName = packagePattern.find(output)?.groupValues?.get(1)
            ?: throw GradleException("无法从 APK 中解析包名")

        // 解析启动 Activity：launchable-activity: name='com.xxx.xxx.MainActivity' ...
        val activityPattern = """launchable-activity:\s+name='([^']+)'""".toRegex()
        val launchableActivity = activityPattern.find(output)?.groupValues?.get(1)
            ?: throw GradleException("无法从 APK 中解析启动 Activity")

        return ApkInfo(packageName, launchableActivity)
    }
}
