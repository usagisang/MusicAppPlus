package com.gochiusa.musicapp.plus.util

import android.graphics.Paint
import com.gochiusa.musicapp.plus.application.MainApplication

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
    fun dpToPx(dp: Float): Float {
        return dp * MainApplication.density
    }

    fun dpToPx(dp: Int): Float {
        return dpToPx(dp.toFloat())
    }

    /**
     *  计算文字的高度
     */
    fun getTextHeight(textPaint: Paint): Float {
        return textPaint.descent() - textPaint.ascent()
    }
}