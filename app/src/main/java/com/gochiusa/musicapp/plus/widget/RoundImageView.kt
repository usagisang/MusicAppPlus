package com.gochiusa.musicapp.plus.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatImageView


class RoundImageView(context: Context, attrs: AttributeSet?, defStyleAttr: Int,
                     private val scale: Boolean = false):
    AppCompatImageView(context, attrs, defStyleAttr) {

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    /**
     *  图片缩放矩阵
     */
    private val scaleMatrix = Matrix()

    /**
     *  绘制图片的画笔
     */
    private val bitmapPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     *  旋转动画
     */
    private var rotateAnimator: ObjectAnimator? = null

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
        var newBitmap = bitmap
        if (scale) {
            newBitmap = scaleBitmap(bitmap)
        }
        bitmapPaint.shader = BitmapShader(newBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        canvas?.let {
            val halfWidth = (width / 2).toFloat()
            val halfHeight = (height / 2 ).toFloat()
            it.drawCircle(halfWidth, halfHeight, halfWidth.coerceAtMost(halfHeight), bitmapPaint)
        }
    }

    /**
     *  辅助方法，计算缩放图片以适应view的宽高需要的比例，并反应在Matrix上
     */
    private fun scaleBitmap(bitmap: Bitmap): Bitmap {
        // 计算缩放比例
        val scale: Float = (width / bitmap.width.toFloat()).coerceAtMost(
            height / bitmap.height.toFloat())
        // 缩放操作
        scaleMatrix.postScale(scale, scale)
        val newBitmap =  Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height,
            scaleMatrix, false)
        scaleMatrix.reset()
        return newBitmap
    }

    /**
     *  开启这个View的旋转动画
     * @param duration 旋转一周的时间，默认25s
     */
    fun startAnimator(duration: Long = 25000L) {
        if (rotateAnimator == null) {
            // 创建新的旋转动画
            rotateAnimator = ObjectAnimator.ofFloat(this,
                "rotation", 0f, 360f)
            rotateAnimator?.let{
                it.duration = duration
                // 使用线性插值器
                it.interpolator = LinearInterpolator()
                it.repeatCount = ObjectAnimator.INFINITE
                it.repeatMode = ObjectAnimator.RESTART
                it.start()
            }
        }
    }

    /**
     *  暂停旋转动画
     */
    fun pauseAnimator() {
        rotateAnimator?.pause()
    }

    /**
     *  停止旋转动画
     */
    fun cancelAnimator() {
        rotateAnimator?.cancel()
        rotateAnimator = null
    }
}
