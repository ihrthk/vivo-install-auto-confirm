package com.zhangls.vivo.install.adb

import com.zhangls.vivo.install.model.DeviceInfo
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import java.io.File

// ============================================
// ADB 命令执行辅助类
// ============================================
/**
 * ADB 命令执行辅助类
 *
 * 封装常用的 ADB 命令操作，提供简洁的 API 用于设备信息和安装控制
 *
 * @property adbPath ADB 可执行文件路径
 * @property logger Gradle 日志记录器
 */
class AdbHelper(
    private val adbPath: String,
    private val logger: Logger
) {

    // ----------------------------------------
    // 基础命令执行
    // ----------------------------------------
    /**
     * 执行 ADB 命令
     *
     * @param args 命令参数列表
     * @return 命令输出内容
     * @throws GradleException 命令执行失败时抛出异常
     */
    fun execute(vararg args: String): String {
        val process = ProcessBuilder(listOf(adbPath) + args)
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            throw GradleException("ADB 命令执行失败: ${args.joinToString(" ")}\n输出: $output")
        }

        return output.trim()
    }

    /**
     * 执行 ADB shell 命令
     *
     * @param args shell 命令参数列表
     * @return 命令输出内容
     */
    fun executeShell(vararg args: String): String {
        return execute("shell", *args)
    }

    // ----------------------------------------
    // 设备信息获取
    // ----------------------------------------
    /**
     * 获取当前连接的设备 ID
     *
     * @return 设备 ID，如果获取失败则抛出异常
     */
    fun getDeviceId(): String {
        val output = execute("devices")
        val lines = output.lines().filter { it.isNotBlank() }

        // 跳过标题行，获取第一个设备
        for (line in lines) {
            if (!line.startsWith("List")) {
                val parts = line.split("\\s+".toRegex())
                if (parts.isNotEmpty()) {
                    return parts[0]
                }
            }
        }

        throw GradleException("未检测到已连接的设备")
    }

    /**
     * 获取设备制造商
     *
     * @return 设备制造商名称
     */
    fun getDeviceManufacturer(): String {
        return executeShell("getprop", "ro.product.manufacturer")
    }

    /**
     * 获取设备屏幕尺寸
     *
     * @return 屏幕尺寸（宽度，高度）
     */
    fun getScreenSize(): Pair<Int, Int> {
        val output = executeShell("wm", "size")
        // 输出格式: Physical size: 1080x2400
        val pattern = """Physical size:\s+(\d+)x(\d+)""".toRegex()
        val match = pattern.find(output)
            ?: throw GradleException("无法解析屏幕尺寸: $output")

        val width = match.groupValues[1].toInt()
        val height = match.groupValues[2].toInt()
        return Pair(width, height)
    }

    /**
     * 获取完整设备信息
     *
     * @return 设备信息对象
     */
    fun getDeviceInfo(): DeviceInfo {
        val deviceId = getDeviceId()
        val manufacturer = getDeviceManufacturer()

        return try {
            val (width, height) = getScreenSize()
            DeviceInfo(deviceId, manufacturer, width, height)
        } catch (e: GradleException) {
            // 如果无法获取屏幕尺寸，返回默认值
            DeviceInfo(deviceId, manufacturer)
        }
    }

    // ----------------------------------------
    // 屏幕操作
    // ----------------------------------------
    /**
     * 在设备屏幕上执行点击操作
     *
     * @param x X 坐标
     * @param y Y 坐标
     */
    fun tap(x: Int, y: Int) {
        executeShell("input", "tap", "$x", "$y")
    }

    // ----------------------------------------
    // 安装操作
    // ----------------------------------------
    /**
     * 后台启动安装进程
     *
     * 使用 adb install -r 后台安装 APK，安装过程会显示 vivo 的风险检测对话框
     *
     * @param apkFile APK 文件
     * @return 安装进程对象
     */
    fun startInstallProcess(apkFile: File): Process {
        val processBuilder = ProcessBuilder(
            adbPath, "install", "-t", "-r", "-g", "-d", apkFile.absolutePath
        )
        processBuilder.redirectErrorStream(true)

        return processBuilder.start()
    }

    // ----------------------------------------
    // 日志输出辅助
    // ----------------------------------------
    /**
     * 收集进程输出直到进程结束
     *
     * @param process 进程对象
     * @param onLine 每行输出的回调函数
     */
    fun collectProcessOutput(
        process: Process,
        onLine: (String) -> Unit
    ) {
        process.inputStream.bufferedReader().use { reader ->
            reader.lines().forEach { line ->
                onLine(line)
            }
        }
    }

    /**
     * 等待进程结束并返回退出码
     *
     * @param process 进程对象
     * @return 退出码
     */
    fun waitForProcess(process: Process): Int {
        return process.waitFor()
    }
}
