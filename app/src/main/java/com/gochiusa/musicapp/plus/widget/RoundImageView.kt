package com.gochiusa.musicapp.plus.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatImageView
import com.gochiusa.musicapp.plus.R
import kotlin.math.abs


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
    private var rotateAnimator: RotateAnimator? = null

    /**
     * 绘制模式，默认为圆形绘制模式
     */
    private var drawType: Int = CIRCLE

    /**
     * 绘制圆角矩形时边角的弧度
     */
    private var roundRectRadius: Float = 50F

    init {
        // 获取属性集合
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView)
        // 获取设置的属性值
        drawType = typedArray.getInt(R.styleable.RoundImageView_draw_type, CIRCLE)
        roundRectRadius = typedArray.getFloat(
            R.styleable.RoundImageView_round_rect_radius, 50F)
        typedArray.recycle()
    }


    override fun onDraw(canvas: Canvas?) {
        if (drawable is BitmapDrawable) {
            // 按照自定义方式绘制Bitmap
            drawBitmap((drawable as BitmapDrawable).bitmap, canvas)
        } else {
            // 按照默认的形式绘制
            super.onDraw(canvas)
        }
    }

    /**
     *  辅助方法，尝试在View上绘制圆形或正方形的Bitmap
     */
    private fun drawBitmap(bitmap: Bitmap, canvas: Canvas?) {
        var newBitmap = bitmap
        if (scale) {
            newBitmap = scaleBitmap(bitmap)
        }
        bitmapPaint.shader = BitmapShader(newBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        canvas?.let {
            val halfWidth = (width / 2).toFloat()
            val halfHeight = (height / 2 ).toFloat()
            if (drawType == CIRCLE) {
                it.drawCircle(
                    halfWidth,
                    halfHeight,
                    halfWidth.coerceAtMost(halfHeight),
                    bitmapPaint
                )
            } else {
                it.drawRoundRect(left.toFloat(), top.toFloat(), right.coerceAtMost(
                    height + left).toFloat(),
                    bottom.coerceAtMost(width + top).toFloat(),
                    roundRectRadius, roundRectRadius, bitmapPaint)
            }
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
     *  开启或继续进行这个View的旋转动画
     * @param duration 旋转一周的时间，默认25s
     */
    fun startAnimator(duration: Long = 25000L) {
        if (rotateAnimator == null) {
           rotateAnimator = RotateAnimator(this, duration)
        }
        rotateAnimator?.start()
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

    private class RotateAnimator(val view: View, duration: Long): ValueAnimator(),
        ValueAnimator.AnimatorUpdateListener {

        var lastRotate = 0F

        init {
            setFloatValues(0F, 360F)
            addUpdateListener(this)
            this.duration = duration
            // 使用线性插值器
            interpolator = LinearInterpolator()
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
        }

        override fun pause() {
            super.pause()
            // 重置旋转信息
            lastRotate = 0F
        }

        override fun onAnimationUpdate(animation: ValueAnimator?) {
            val nowRotate = animatedValue as Float
            var viewRotate = view.rotation
            viewRotate += abs(nowRotate - lastRotate)
            if (viewRotate > 360F) {
                viewRotate -= 360F
            }
            lastRotate = nowRotate
            view.rotation = viewRotate
        }
    }

    companion object {
        const val CIRCLE = 1
        const val ROUND_RECT = 2
    }
}
