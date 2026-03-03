package com.zhangls.vivo.install

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Vivo ADB 自动安装 Gradle 插件
 *
 * 使用方式：
 *   ./gradlew assembleDebug -Pvivo-install    构建后自动安装并启动
 *
 *   在 build.gradle.kts 中配置：
 *   vivoInstall {
 *       autoLaunch.set(true)   // 默认 true
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

        // 设置自动确认默认值
        extension.autoConfirmWaitTime.convention(5L)
        extension.checkboxX.convention(365)
        extension.checkboxY.convention(2270)
        extension.buttonYPercent.convention(0.93f)  // 2455/2640 ≈ 93%

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
                            (task as VivoInstallTask).variantName = variantName
                            (task as VivoInstallTask).extension = extension
                            task.group = "vivo"
                            task.description = "安装并启动 ${variantName} APK 到设备"
                        }

                        // 让 assemble 任务依赖安装任务
                        assembleTask.finalizedBy(installTask)
                    }
            }
        }
    }
}