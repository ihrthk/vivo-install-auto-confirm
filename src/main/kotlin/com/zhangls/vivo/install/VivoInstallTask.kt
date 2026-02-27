package com.zhangls.vivo.install

import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option
import java.io.File

// ============================================
// APK 信息数据类
// ============================================
/**
 * APK 应用信息
 *
 * @param packageName 应用包名
 * @param launchableActivity 启动 Activity 完整路径
 */
data class ApkInfo(
    val packageName: String,
    val launchableActivity: String
)

// ============================================
// 安装任务
// ============================================
/**
 * ADB 安装任务
 *
 * 执行 APK 安装和可选的应用启动
 */
abstract class VivoInstallTask : BaseVivoTask() {

    // ----------------------------------------
    // 任务输入属性
    // ----------------------------------------
    /**
     * 构建变体名称（如 "Debug"、"Release"）
     */
    @get:Input
    abstract val variantName: Property<String>

    /**
     * 是否在安装后自动启动应用
     */
    @get:Input
    @get:Optional
    abstract val autoLaunch: Property<Boolean>

    /**
     * 安装后到启动前的等待时间（秒）
     */
    @get:Input
    @get:Optional
    abstract val waitTime: Property<Long>

    // ----------------------------------------
    // 任务执行逻辑
    // ----------------------------------------
    @TaskAction
    fun install() {
        // 查找 APK 文件
        val apkFile = findApkFile(variantName.get())
        if (apkFile == null) {
            project.logger.warn("未找到 ${variantName.get()} 变体的 APK 文件，跳过安装")
            return
        }

        // 安装 APK 并获取应用信息
        val apkInfo = installApk(apkFile)

        // 如果启用自动启动，则等待后启动应用
        if (autoLaunch.getOrElse(false)) {
            val waitSeconds = waitTime.getOrElse(30L)
            project.logger.lifecycle("等待 ${waitSeconds} 秒后启动应用...")
            Thread.sleep(waitSeconds * 1000)
            launchApp(apkInfo)
        }
    }
}

// ============================================
// 基础任务类（包含工具方法）
// ============================================
/**
 * 基础任务类，提供通用的 ADB 和 APK 处理方法
 */
abstract class BaseVivoTask : org.gradle.api.DefaultTask() {

    // ----------------------------------------
    // 配置属性
    // ----------------------------------------
    /**
     * Android SDK 根路径
     */
    @get:Internal
    abstract val sdkRoot: Property<String>

    // ----------------------------------------
    // 获取 SDK 路径
    // ----------------------------------------
    /**
     * 获取 Android SDK 根路径
     *
     * 优先使用配置值，其次从环境变量 ANDROID_SDK_ROOT、ANDROID_HOME 获取，
     * 若都未设置则抛出异常
     */
    protected fun resolveEffectiveSdkRoot(): String {
        return sdkRoot.orNull
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
    // AAPT 相关
    // ----------------------------------------
    /**
     * 获取 aapt 可执行文件路径
     *
     * @return aapt 完整路径
     */
    protected fun resolveAaptPath(): String {
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

    /**
     * 使用 aapt 从 APK 文件中解析应用信息
     *
     * @param apkFile APK 文件
     * @return APK 信息（包名和启动 Activity）
     */
    protected fun getApkInfo(apkFile: File): ApkInfo {
        val aaptPath = resolveAaptPath()

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

    // ----------------------------------------
    // APK 文件查找
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
    // ADB 操作
    // ----------------------------------------
    /**
     * 获取 ADB 可执行文件路径
     */
    protected fun resolveAdbPath(): String {
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

    /**
     * 通过 ADB 安装 APK 到设备
     *
     * @param apkFile 要安装的 APK 文件
     * @return APK 应用信息
     * @throws GradleException 安装失败时抛出异常
     */
    protected fun installApk(apkFile: File): ApkInfo {
        val adbPath = resolveAdbPath()

        project.logger.lifecycle("正在安装: ${apkFile.name}")

        val process = ProcessBuilder(adbPath, "install", "-r", apkFile.absolutePath)
            .redirectErrorStream(true)
            .start()

        // 输出 ADB 安装日志
        process.inputStream.bufferedReader().use { reader ->
            reader.lines().forEach { line ->
                project.logger.lifecycle(line)
            }
        }

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw GradleException("adb 安装失败，退出码: $exitCode")
        }

        // 从 APK 解析应用信息
        return getApkInfo(apkFile)
    }

    /**
     * 通过 ADB 启动应用主页面
     *
     * @param apkInfo APK 应用信息
     * @return 启动是否成功
     */
    protected fun launchApp(apkInfo: ApkInfo): Boolean {
        val adbPath = resolveAdbPath()

        project.logger.lifecycle("正在启动应用: ${apkInfo.packageName}")

        val process = ProcessBuilder(
            adbPath, "shell", "am", "start",
            "${apkInfo.packageName}/${apkInfo.launchableActivity}"
        )
            .redirectErrorStream(true)
            .start()

        // 输出 ADB 启动日志
        process.inputStream.bufferedReader().use { reader ->
            reader.lines().forEach { line ->
                project.logger.lifecycle(line)
            }
        }

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            project.logger.warn("启动应用失败，退出码: $exitCode")
            return false
        }
        return true
    }
}
