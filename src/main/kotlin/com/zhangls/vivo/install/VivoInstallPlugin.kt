package com.zhangls.vivo.install

import org.gradle.api.Plugin
import org.gradle.api.Project

// ============================================
// ADB 自动安装插件
// ============================================
/**
 * Vivo ADB 自动安装 Gradle 插件
 *
 * 使用方式：
 *   ./gradlew assembleDebug -Pvivo-install    构建后自动安装并启动
 *
 *   在 build.gradle.kts 中配置：
 *   vivoInstall {
 *       autoLaunch.set(true)   // 默认 true
 *       waitTime.set(30L)      // 默认 30秒
 *   }
 */
class VivoInstallPlugin : Plugin<Project> {

    // ----------------------------------------
    // 插件入口
    // ----------------------------------------
    override fun apply(project: Project) {
        // 创建扩展配置
        val extension = project.extensions.create(
            "vivoInstall",
            VivoInstallExtension::class.java
        )

        // 设置默认值
        extension.autoLaunch.convention(true)
        extension.waitTime.convention(30L)

        // 只有添加 -Pvivo-install 参数时才注册任务
        if (project.hasProperty("vivo-install")) {
            project.afterEvaluate {
                project.tasks
                    .filter { it.name.startsWith("assemble") }
                    .forEach { assembleTask ->
                        val variantName = assembleTask.name.removePrefix("assemble")

                        // 创建安装任务
                        val taskName = "vivoInstall${variantName}"
                        val installTask = project.tasks.register(
                            taskName,
                            VivoInstallTask::class.java
                        )

                        // 配置任务
                        installTask.configure { task ->
                            task.variantName.set(variantName)
                            task.sdkRoot.set(extension.sdkRoot)
                            task.autoLaunch.set(extension.autoLaunch)
                            task.waitTime.set(extension.waitTime)
                            task.group = "vivo"
                            task.description = "安装 ${variantName} APK 到设备"
                        }

                        // 让 assemble 任务依赖安装任务
                        assembleTask.finalizedBy(installTask)
                    }
            }
        }
    }
}
