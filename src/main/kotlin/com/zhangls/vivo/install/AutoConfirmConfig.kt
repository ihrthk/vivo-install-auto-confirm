package com.zhangls.vivo.install

// ============================================
// 自动确认配置类
// ============================================
/**
 * 自动确认配置类
 *
 * 用于配置 vivo 设备安装时的自动确认行为
 *
 * @property enabled 是否启用自动确认，默认 true
 * @property waitTime 等待安装界面出现的时间（秒），默认 30 秒
 * @property checkboxX 复选框点击 X 坐标，默认 365
 * @property checkboxY 复选框点击 Y 坐标，默认 2270
 * @property buttonYPercent 安装按钮 Y 坐标占屏幕高度的百分比，默认 0.93（93%）
 */
data class AutoConfirmConfig(
    val enabled: Boolean = true,
    val waitTime: Long = 30L,
    val checkboxX: Int = 365,
    val checkboxY: Int = 2270,
    val buttonYPercent: Float = 0.93f
)
