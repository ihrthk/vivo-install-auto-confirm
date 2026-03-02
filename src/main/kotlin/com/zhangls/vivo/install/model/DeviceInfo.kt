package com.zhangls.vivo.install.model

// ============================================
// 设备信息数据类
// ============================================
/**
 * 设备信息数据类
 *
 * @property deviceId 设备 ID
 * @property制造商
 * @property screenWidth 屏幕宽度（像素）
 * @property screenHeight 屏幕高度（像素）
 */
data class DeviceInfo(
    val deviceId: String,
    val manufacturer: String,
    val screenWidth: Int = 0,
    val screenHeight: Int = 0
) {
    /**
     * 判断是否为 vivo 设备
     * @return 如果制造商为 vivo 则返回 true
     */
    fun isVivoDevice(): Boolean = manufacturer.lowercase() == "vivo"

    /**
     * 判断是否已获取到有效的屏幕尺寸
     * @return 如果屏幕宽高均大于 0 则返回 true
     */
    fun hasScreenSize(): Boolean = screenWidth > 0 && screenHeight > 0
}
