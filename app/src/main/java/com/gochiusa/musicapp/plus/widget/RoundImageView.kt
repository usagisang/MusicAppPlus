package com.gochiusa.musicapp.plus.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class RoundImageView constructor(context: Context,
                                 attrs: AttributeSet? = null,
                                 defStyleAttr: Int = 0,
                                 private val scale: Boolean = false):
    AppCompatImageView(context, attrs, defStyleAttr) {

    /**
     *  裁切范围的路径
     */
    private val circlePath = Path()

    /**
     *  图片缩放矩阵
     */
    private val scaleMatrix = Matrix()

    /**
     *  绘制图片的画笔
     */
    private val bitmapPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        circlePath.addCircle((width / 2).toFloat(), (height / 2).toFloat(),
            (width / 2).coerceAtMost(height / 2).toFloat(), Path.Direction.CW)
    }

    override fun onDraw(canvas: Canvas?) {
        if (drawable is BitmapDrawable) {
            // 绘制出圆形的Bitmap
            drawRoundBitmap((drawable as BitmapDrawable).bitmap, canvas)
        } else {
            // 按照默认的形式绘制
            super.onDraw(canvas)
        }
    }

    /**
     *  辅助方法，尝试在View上绘制圆形的Bitmap
     */
    private fun drawRoundBitmap(bitmap: Bitmap, canvas: Canvas?) {
        if (scale) {
            scaleBitmap(bitmap)
        }
        canvas?.let {
            it.save()
            it.clipPath(circlePath)
            it.drawBitmap(bitmap, scaleMatrix, bitmapPaint)
            it.restore()
        }
        // 重置矩阵
        scaleMatrix.reset()

    }

    /**
     *  辅助方法，计算缩放图片以适应view的宽高需要的比例，并反应在Matrix上
     */
    private fun scaleBitmap(bitmap: Bitmap){
        // 计算缩放比例
        val scale: Int = (width / bitmap.width).coerceAtMost(
            height / bitmap.height)
        // 缩放操作
        scaleMatrix.postScale(scale.toFloat(), scale.toFloat())
    }

    class RotateAnimator(val imageMatrix: Matrix): ValueAnimator() {

    }
}
