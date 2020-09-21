package com.gochiusa.musicapp.plus.util

import java.lang.StringBuilder

/**
 * 将毫秒数换算为进度条显示的时间的一个工具类
 */
object TimeCalculator {
    private const val COLON = ":"
    private const val ZERO = "0"
    private val builder = StringBuilder()

    @JvmStatic
    fun calculateSongDuration(duration: Int): String {
        return calculateSongDuration(duration)
    }

    @JvmStatic
    fun calculateSongDuration(duration: Long): String {
        // 获得总秒数
        val totalSecond = duration / 1000
        // 求分钟
        val minute = totalSecond / 60
        // 求秒
        val second = totalSecond % 60
        if (minute < 10) {
            // 如果分钟为个位数，在其前面加0
            builder.append(ZERO)
        }
        builder.append(minute)
        builder.append(COLON)
        if (second < 10) {
            // 如果秒是个位数，在其前面加0
            builder.append(ZERO)
        }
        builder.append(second)
        // 使用工具类拼接
        return builder.toString()
    }
}