package com.gochiusa.musicapp.plus.util

/**
 * 辅助计算偏移量的工具类
 */
class OffsetCalculator(var offset: Int, var pageLimit: Int, var totalCount: Int) {

    /**
     * 使偏移量递增
     * @return 偏移量到达最大值，返回false，表示递增失败，否则返回true代表偏移量递增成功
     */
    fun increaseOffset(): Boolean {
        offset += pageLimit
        return if (offset < totalCount) {
            true
        } else {
            offset = totalCount
            false
        }
    }

    /**
     * 获取当前的页码数
     */
    val page: Int
        get() = offset / pageLimit

}