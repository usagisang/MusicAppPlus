package com.gochiusa.musicapp.plus.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.util.WidgetUtil
import kotlin.math.floor

class LetterListView(context: Context, attrs: AttributeSet?, defStyleAttr: Int):
    View(context, attrs, defStyleAttr) {

    /**
     *  非触摸位置的文字画笔
     */
    private val normalTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     *  触摸位置的文字画笔
     */
    private val clickTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     *  当前被点击的字母，默认是空串
     */
    private var currentLetter: String = NOT_TOUCH_ANY_LETTER

    /**
     *  字母切换的监听器
     */
    private var letterChangeListener: ((String) -> Unit)? = null

    lateinit var letters: List<String>

    private val fontScale: Float = context.resources.displayMetrics.scaledDensity

    constructor(context: Context): this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)


    init {
        // 获取sp与px的比例
        // 初始化画笔的设置
        normalTextPaint.textSize = WidgetUtil.spToPx(LETTER_SIZE, fontScale)
        normalTextPaint.color = Color.BLACK

        clickTextPaint.textSize = WidgetUtil.spToPx(LETTER_SIZE, fontScale)
        clickTextPaint.color = ResourcesCompat.getColor(resources,
            R.color.colorBlue, getContext().theme)
    }

    public fun setOnLetterChangeListener(listener: OnLetterChangeListener) {
        letterChangeListener = listener
    }

    public fun setOnLetterChangeListener(listener: (String) -> Unit) {
        letterChangeListener = listener
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 总高度可以直接计算
        val totalHeight = (normalTextPaint.descent() - normalTextPaint.ascent() +
                LETTER_BOTTOM_MARGIN) * letters.size + TOP_OR_BOTTOM_PADDING * 2
        // 缓存所有字母中的最大宽度
        var maxWidth = 0f
        // 缓存单字的宽度
        var letterWidth: Float
        for (letter in letters) {
            letterWidth = normalTextPaint.measureText(letter)
            if (letterWidth > maxWidth) {
                maxWidth = letterWidth
            }
        }
        maxWidth += LEFT_OR_RIGHT_PADDING * 2
        setMeasuredDimension(maxWidth.toInt(), totalHeight.toInt())
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // 单个字母的高度
        val letterHeight: Float = normalTextPaint.descent() - normalTextPaint.ascent() +
                LETTER_BOTTOM_MARGIN
        // 缓存当前字母
        var letter: String
        // 缓存单个字母的宽度
        var letterWidth: Float
        // 缓存绘制文字的X轴坐标
        var dx: Float
        // 缓存绘制文字的Y轴坐标
        var dy: Float
        for (index in letters.indices) {
            letter = letters[index]
            letterWidth = normalTextPaint.measureText(letter)
            // 计算绘制文字的Y轴坐标
            dy = letterHeight * (index + 1) + TOP_OR_BOTTOM_PADDING
            // 计算绘制文字的X轴坐标
            dx = (measuredWidth - letterWidth) / 2
            if (letter.equals(currentLetter)) {
                // 如果字符是被点中的
                canvas?.drawText(letter, dx, dy, clickTextPaint)
            } else {
                // 否则使用一般的画笔来绘制
                canvas?.drawText(letters[index], dx, dy, normalTextPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // 如果点击事件为空，那么无法进行任何操作
        if (event == null) {
            return false
        }
        // 计算被点击位置的字母的下标
        val clickLetterIndex: Int = floor(((event.getY(0) - TOP_OR_BOTTOM_PADDING) /
                (measuredHeight - TOP_OR_BOTTOM_PADDING * 2)) * letters.size).toInt()
        // 校验点击位置是否在字母范围内
        if (clickLetterIndex < 0 || clickLetterIndex >= letters.size) {
            return true
        }
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                // 更新当前字母
                currentLetter = letters[clickLetterIndex]
                // 回调监听器
                letterChangeListener?.invoke(letters[clickLetterIndex])
                // 刷新界面
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 延时回调监听器
                this.postDelayed({
                    letterChangeListener?.invoke(NOT_TOUCH_ANY_LETTER)
                }, DELAY_MILLISECOND)
            }
        }
        return true
    }


    companion object {
        /**
         *  字体的大小，单位为sp
         */
        private const val LETTER_SIZE = 10.5f
        /**
         *  左边或右边的内边距
         */
        private const val LEFT_OR_RIGHT_PADDING: Float = 5f

        /**
         *  顶部或底部的内边距
         */
        private const val TOP_OR_BOTTOM_PADDING: Float = 10f

        /**
         *  字母之间的下边距
         */
        private const val LETTER_BOTTOM_MARGIN = 10f

        /**
         *  延迟回调监听的时间，单位为毫秒
         */
        private const val DELAY_MILLISECOND = 1500L

        /**
         *  未点击任何字母时的常量，为空串
         */
        const val NOT_TOUCH_ANY_LETTER = ""
    }

    interface OnLetterChangeListener : (String) -> Unit {
        override fun invoke(letter: String)
    }
}