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
 *   ./gradlew assembleDebug                               仅构建，不安装
 *   ./gradlew assembleDebug -Pvivo-auto-install           构建后自动安装
 *   ./gradlew assembleDebug -Pvivo-auto-install -Pvivo-auto-launch  安装后自动启动
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

        // 只有添加 -Pvivo-auto-install 参数时才注册任务
        if (project.hasProperty("vivo-auto-install")) {
            val autoLaunch = project.hasProperty("vivo-auto-launch")

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
                            task.autoLaunch.set(autoLaunch)
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
