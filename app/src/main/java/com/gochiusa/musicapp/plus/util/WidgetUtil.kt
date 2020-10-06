package com.gochiusa.musicapp.plus.util

import android.graphics.Paint

object WidgetUtil {
    /**
     *  sp转px的工具方法
     */
    fun spToPx(sp: Float, fontScale: Float): Float {
        return sp * fontScale + 0.5f
    }

    /**
     *  dp转px
     */
    fun dpToPx(dp: Float, density: Float): Float {
        return dp * density
    }

    /**
     *  计算文字的高度
     */
    fun getTextHeight(textPaint: Paint): Float {
        return textPaint.descent() - textPaint.ascent()
    }
}