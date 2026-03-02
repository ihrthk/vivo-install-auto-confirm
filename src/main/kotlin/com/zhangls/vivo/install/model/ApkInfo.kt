package com.zhangls.vivo.install.model

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
