package com.gochiusa.musicapp.plus.widget

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.github.authorfu.lrcparser.parser.Sentence
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.util.TimeCalculator
import com.gochiusa.musicapp.plus.util.WidgetUtil
import kotlin.math.abs
import kotlin.math.sqrt

class LyricView(context: Context, attrs: AttributeSet?, defStyleAttr: Int):
    View(context, attrs, defStyleAttr)  {

    constructor(context: Context): this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    /**
     *  缓存所有歌词的集合
     */
    private val lyricSentenceList: MutableList<Sentence> = mutableListOf()

    /**
     * 当前高亮显示的歌词位置索引，即为当前播放位置
     */
    private var currentLine: Int = 0

    /**
     *  指示线指示的歌词位置索引
     */
    private var indicatorLine = 0

    /**
     *  歌词显示区域两边的留白，单位为px
     */
    var contentPadding: Float = 0f
        set(value) {
            field = WidgetUtil.dpToPx(value)
        }

    /**
     *  两句歌词之间的间距，单位为px
     */
    var lyricTextMargin = 0f
        set(value) {
            field = WidgetUtil.dpToPx(value)
        }

    /**
     *  绘制的进度组件和播放组件与屏幕边缘的间距，单位为px
     */
    var drawPadding = 0f
        set(value) {
            field = WidgetUtil.dpToPx(value)
        }

    /**
     *  标志变量，是否正在加载歌词
     */
    var loadingLyric: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    /**
     *  点击播放图案后的点击事件
     */
    var onPlayClickListener: OnPlayClickListener? = null

    /**
     *  字体sp与px的比例
     */
    private val fontScale: Float = context.resources.displayMetrics.scaledDensity

    /**
     *  绘制歌词文字的画笔
     */
    private val normalTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    /**
     *  指示线绘制画笔
     */
    private val indicatorPaint: Paint = Paint()

    /**
     * 绘制指示进度的文字画笔
     */
    private val progressPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    /**
     *  绘制自定义按钮的画笔
     */
    private val playButtonPaint = Paint()

    /**
     *  高亮颜色
     */
    private val highlightColor = ResourcesCompat.getColor(resources, R.color.colorBlue,
        getContext().theme)

    /**
     *  默认颜色
     */
    private val normalColor = ResourcesCompat.getColor(resources, R.color.colorBlack,
        getContext().theme)

    /**
     *  标志变量，是否需要绘制中间的指示线
     */
    private var needDrawIndicator: Boolean = false

    /**
     * 标志变量，是否为拖动模式
     */
    private var dragMode: Boolean = false

    /**
     *  标志变量，是否允许提交歌词复位任务
     */
    private var canRedraw: Boolean = true

    /**
     *  缓存每一行歌词的底部坐标
     */
    private val lyricHeightList = mutableListOf<Float>()


    /**
     * 缓存StaticLayout的集合
     */
    private val lyricStaticLayoutMap: MutableMap<String, StaticLayout> = mutableMapOf()


    /**
     *  手指抬起后需要执行的任务
     */
    private val pointerUpRunnable: () -> Unit = {
        // 只有在不处于拖拽模式才允许复位和隐藏指示线
        if (! dragMode) {
            // 重置指示线位置
            indicatorLine = currentLine
            // 隐藏指示线
            needDrawIndicator = false
            // 先重绘
            invalidate()
            // 创建复位动画、指定持续时间
            if (lyricHeightList.isNotEmpty()) {
                scrollAnimator = ScrollAnimator(currentLine).setDuration(300L)
                scrollAnimator?.start()
            }
        }
        // 允许提交复位任务
        canRedraw = true
    }

    /**
     *  上次点击屏幕的位置
     */
    private val lastClickPoint: PointF = PointF()

    /**
     *  绘制播放按钮使用到的路径描述类
     */
    private val playButtonPath = Path()

    /**
     * 手势检测器
     */
    private val gestureDetector: GestureDetector

    /**
     * 惯性滑动动画
     */
    private var flingAnimator: FlingAnimator? = null

    /**
     *  歌词跳转动画
     */
    private var scrollAnimator: ValueAnimator? = null


    /**
     *  缓存画播放按钮的坐标的矩形
     */
    private val playButtonRectF = RectF()


    init {
        // 设置画笔的颜色、字体大小
        normalTextPaint.color = ResourcesCompat.getColor(resources, R.color.colorBlack,
            getContext().theme)
        normalTextPaint.textSize = WidgetUtil.spToPx(NORMAL_TEXT_SIZE, fontScale)

        indicatorPaint.color = ResourcesCompat.getColor(resources, R.color.colorGrey,
            getContext().theme)

        progressPaint.color = ResourcesCompat.getColor(resources, R.color.colorBlack,
            getContext().theme)
        progressPaint.textSize = WidgetUtil.spToPx(DEFAULT_PROGRESS_TEXT_SIZE, fontScale)

        playButtonPaint.style = Paint.Style.FILL
        playButtonPaint.color = ResourcesCompat.getColor(resources, R.color.colorGrey,
            getContext().theme)

        // 初始化控件的内容显示边界信息
        contentPadding = DEFAULT_LYRIC_PADDING
        lyricTextMargin = DEFAULT_LYRIC_TEXT_MARGIN
        drawPadding = DEFAULT_DRAW_MARGIN
        // 初始化手势控制器
        gestureDetector = GestureDetector(context,
            object: GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?, e2: MotionEvent?,
                    velocityX: Float, velocityY: Float
                ): Boolean {
                    // 取消所有动画动作
                    cancelAllAnimation()
                    if (hasLyric()) {
                        flingAnimator = FlingAnimator(-velocityY)
                        flingAnimator?.start()
                    }
                    return true
                }

                override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
                    return performClick()
                }
            })
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null || this.visibility != VISIBLE) {
            return
        }
        if (loadingLyric) {
            drawHintText(canvas, DEFAULT_LOADING_LYRIC_TIP)
            return
        }
        if (!hasLyric()) {
            drawHintText(canvas, DEFAULT_LYRIC_NULL_TIP)
            return
        }
        val halfHeight: Float = (height / 2).toFloat()
        // 标志变量，限制每一次重绘，只更新一次指示线行数索引
        var smallerThan = true
        // 当前绘制的歌词的坐标
        var dy = halfHeight
        for (index in lyricSentenceList.indices) {
            dy += if (index == currentLine) {
                // 重置画笔颜色
                normalTextPaint.color = highlightColor
                drawLyricText(canvas, lyricSentenceList[index].content, normalTextPaint, dy)
            } else {
                // 重置画笔颜色
                normalTextPaint.color = normalColor
                drawLyricText(canvas, lyricSentenceList[index].content, normalTextPaint, dy)
            }
            dy += lyricTextMargin
            // 更新指示线行数索引
            if (smallerThan && dy >= scrollY + halfHeight) {
                indicatorLine = index
                // 重置变量
                smallerThan = false
            }
            // 缓存歌词的坐标
            lyricHeightList.add(index, dy)
        }
        if (needDrawIndicator) {
            drawIndicator(canvas)
        }
    }

    /**
     *  设置当前播放位置，并启动滚动动画来平滑移动
     */
    fun scrollToLine(line: Int) {
        // 检查移动是否越界
        if (line > lyricSentenceList.size - 1 || line < 0) {
            return
        }
        // 如果行数没有发生变化，不需要滚动
        if (currentLine == line) {
            return
        }
        // 检查是否被拖动，需要复位才允许滚动，避免冲突
        if (indicatorLine == currentLine) {
            // 如果没有测量过歌词的高度，无法创建动画
            if (lyricHeightList.size == 0) {
                return
            }
            val animator = ScrollAnimator(line)
                .setDuration(300L)
            scrollAnimator?.let {
                if (it.isRunning) {
                    animator.startDelay = 300L
                }
            }
            scrollAnimator = animator
            scrollAnimator?.start()
        }
        currentLine = line
        indicatorLine = line
    }

    /**
     * 重置LyricView的状态
     */
    fun reset() {
        lyricSentenceList.clear()
        currentLine = 0
        indicatorLine = 0
        lyricHeightList.clear()
        lyricStaticLayoutMap.clear()
        scrollY = 0
    }

    /**
     * 添加需要显示的歌词
     */
    fun addLyric(lyricList: List<Sentence>) {
        lyricSentenceList.addAll(lyricList)
        invalidate()
    }

    fun getSentenceList(): List<Sentence> {
        return lyricSentenceList
    }

    private fun drawHintText(canvas: Canvas, text: String) {
        // 定位歌词显示的横坐标
        normalTextPaint.color = highlightColor
        val dx = (width - normalTextPaint.measureText(text)) / 2
        canvas.drawText(text, dx, (height / 2).toFloat(), normalTextPaint)
    }

    private fun drawLyricText(canvas: Canvas, text: String,
                              textPaint: TextPaint, dy: Float): Float {
        // 计算文字最大显示宽度
        val textMaxWidth = (width - contentPadding * 2).toInt()
        // 获取StaticLayout
        var staticLayout = lyricStaticLayoutMap[text]
        // 如果StaticLayout为空，创建
        if (staticLayout == null) {
            staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(
                    text, 0, text.length, textPaint,
                    textMaxWidth).setAlignment(Layout.Alignment.ALIGN_CENTER).build()
            } else {
                StaticLayout(
                    text, textPaint, textMaxWidth,
                    Layout.Alignment.ALIGN_CENTER, 1f, 0f, true
                )
            }
            // 缓存StaticLayout
            lyricStaticLayoutMap[text] = staticLayout
        }
        canvas.save()
        // 将画布平移到合适的位置
        canvas.translate(contentPadding, dy)
        staticLayout.draw(canvas)
        canvas.restore()
        return staticLayout.height.toFloat()
    }

    private fun drawIndicator(canvas: Canvas?) {
        // 没有歌词则直接返回
        if (! hasLyric()) {
            return
        }
        // 始终保持指示线和控件处于中间
        val dy = height / 2f + scrollY
        canvas?.let {
            it.drawLine(contentPadding, dy, width - contentPadding, dy, indicatorPaint)
            val progressTextY = dy + WidgetUtil.getTextHeight(progressPaint) / 2
            calculateButtonCoordinate(PLAY_BUTTON_SIZE)
            // 先重置Path对象
            playButtonPath.reset()
            // 移动路径起点
            playButtonPath.moveTo(playButtonRectF.left, playButtonRectF.top)
            // 描述线段
            playButtonPath.lineTo(playButtonRectF.right,
                playButtonRectF.height() / 2 + playButtonRectF.top)
            playButtonPath.lineTo(playButtonRectF.left, playButtonRectF.bottom)
            // 绘制指示线的指示的进度时间
            it.drawText(TimeCalculator.calculateSongDuration(
                lyricSentenceList[indicatorLine].fromTime), drawPadding,
                progressTextY, progressPaint)
            it.drawPath(playButtonPath, playButtonPaint)
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return false
        }
        // 先交给手势处理器处理
        gestureDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                // 取消所有动画
                cancelAllAnimation()
                // 只允许单指拖动
                if (event.pointerCount == 1) {
                    needDrawIndicator = true
                    // 判断点击区域是否在播放按钮范围内
                    if (event.x > width - contentPadding && event.x < width - drawPadding &&
                        event.y > height / 2 - PLAY_BUTTON_SIZE &&
                        event.y < height / 2 + PLAY_BUTTON_SIZE
                    ) {
                        // 设置当前播放位置
                        currentLine = indicatorLine
                        // 触发播放事件
                        onPlayClickListener?.onPlayClick(
                            this,
                            lyricSentenceList[indicatorLine].fromTime
                        )
                    }
                    dragMode = true
                    // 更新点击位置
                    lastClickPoint.set(event.x, event.y)
                    invalidate()
                } else {
                    dragMode = false
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (hasLyric() && dragMode) {
                    translate(event)
                }
            }
            MotionEvent.ACTION_UP -> {
                // 如果有歌词且允许提交重置任务
                if (hasLyric() && canRedraw) {
                    postDelayed(pointerUpRunnable, 3000L)
                    // 阻断接下来重置任务的提交
                    canRedraw = false
                }
                dragMode = false
            }
        }
        return true
    }


    /**
     * 平移操作
     */
    private fun translate(event: MotionEvent) {
        // 计算应当滑动到的位置
        var prepareScrollY = scrollY - (event.y - lastClickPoint.y)
        // 重置触点信息
        lastClickPoint.set(event.x, event.y)
        // 检查边界条件并复位
        if (prepareScrollY < 0) {
            prepareScrollY = 0F
        }
        val lyricTextTotalHeight = calculateLyricTotalHeight()
        if (prepareScrollY > lyricTextTotalHeight) {

            prepareScrollY = lyricTextTotalHeight
        }
        // 执行位移操作
        scrollTo(scrollX, prepareScrollY.toInt())
    }

    /**
     *  计算播放按钮的坐标信息，坐标的前提是播放按钮放置在歌词控件内容右方
     *  @param buttonHeight 期望的按钮高度
     *  @return 记录了按钮范围信息的矩形
     */
    private fun calculateButtonCoordinate(buttonHeight: Float) {
        // 计算按钮宽度
        var buttonWidth = (buttonHeight * sqrt(3.0) * 0.5).toFloat()
        // 计算最大宽度
        val maxWidth = contentPadding - drawPadding
        if (buttonWidth > maxWidth) {
            buttonWidth = maxWidth
        }
        // 计算出左边的坐标并设置
        playButtonRectF.left = (width - contentPadding) + (maxWidth - buttonWidth) / 2
        // 计算出上面的坐标并设置
        playButtonRectF.top = height / 2f + scrollY - buttonWidth / 2
        playButtonRectF.right = playButtonRectF.left + buttonWidth
        playButtonRectF.bottom = playButtonRectF.top + buttonHeight
    }

    private fun hasLyric(): Boolean {
        return lyricSentenceList.isNotEmpty()
    }

    /**
     * 取消所有正在进行的动画
     */
    private fun cancelAllAnimation() {
        flingAnimator?.cancel()
        flingAnimator = null
        scrollAnimator?.cancel()
        scrollAnimator = null
    }

    private fun calculateLyricTotalHeight(): Float{
        return if (lyricHeightList.isEmpty()) {
            0F
        } else {
            lyricHeightList[lyricHeightList.size - 1] - height / 2 - lyricTextMargin
        }
    }


    companion object {
        /**
         *  字体的大小，单位为sp
         */
        private const val NORMAL_TEXT_SIZE: Float = 16f

        /**
         *  歌词显示的范围与屏幕边缘的横向距离，单位为dp
         */
        private const val DEFAULT_LYRIC_PADDING = 55f

        /**
         *  显示播放按钮和进度的边界与屏幕边缘的距离, 单位为dp
         */
        private const val DEFAULT_DRAW_MARGIN = 8f

        /**
         *  每一句歌词之间的间距
         */
        private const val DEFAULT_LYRIC_TEXT_MARGIN = 10f

        /**
         *  需要绘制的播放按钮的高度，单位为px，宽度约定为高度的sqrt(3)/2
         */
        private const val PLAY_BUTTON_SIZE = 50f

        /**
         *  滑动速度衰减速率
         */
        private const val FLING_DAMPING_FACTOR = 0.9f

        private const val DEFAULT_PROGRESS_TEXT_SIZE = 12f
        private const val DEFAULT_LYRIC_NULL_TIP = "这首歌暂无歌词"
        private const val DEFAULT_LOADING_LYRIC_TIP = "歌词加载中……"
    }

    /**
     * 惯性动画
     *
     * 速度逐渐衰减,每帧速度衰减为原来的FLING_DAMPING_FACTOR,当速度衰减到小于1时停止.
     * 当图片不能移动时,动画停止.
     */
    private inner class FlingAnimator(var velocityY: Float) :
        ValueAnimator(), AnimatorUpdateListener {

        /**
         * 初始滑动速度参数单位必须为 像素/帧，否则会导致滑动过快的问题
         */
        init {
            // 设置属性值从0到1变化，但其实没有再使用这个属性
            setFloatValues(0f, 1f)
            // 持续时间设置得比较大，必须通过手动的方式停止动画
            duration = 1000000
            addUpdateListener(this)
            velocityY /= 60
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            // 移动View，返回是否移动的布尔值
            val canMove: Boolean =
                if (scrollY + velocityY >=0 &&
                    scrollY + velocityY < calculateLyricTotalHeight()) {
                    // 执行位移操作
                    scrollBy(0, velocityY.toInt())
                    true
                } else {
                    false
                }
            // 将速度按照比例衰减
            velocityY *= FLING_DAMPING_FACTOR
            // Y轴不能移动，或者速度过小，结束动画
            if (! canMove || abs(velocityY) < 1f) {
                animation.cancel()
            }
        }
    }

    /**
     *  歌词跳转动画
     *  @param toLine 歌词跳转终点
     */
    private inner class ScrollAnimator(toLine: Int) :
        ValueAnimator(), AnimatorUpdateListener {

        init {
            // 计算滚动终点行歌词的高度
            val toLyricHeight: Float = if (toLine <= 0) {
                lyricHeightList[0] - height / 2 - lyricTextMargin
            } else {
                lyricHeightList[toLine] - lyricHeightList[toLine - 1] - lyricTextMargin
            }
            // 设置滚动的范围
            setIntValues(scrollY,
                (lyricHeightList[toLine] - (toLyricHeight + height) / 2).toInt())
            addUpdateListener(this)
        }

        override fun onAnimationUpdate(animation: ValueAnimator?) {
            scrollTo(scrollX, animatedValue as Int)
        }
    }

    /**
     * 播放按钮点击监听器，点击后应该跳转到指定播放位置
     */
    interface OnPlayClickListener {
        /**
         * 播放按钮被点击，应该跳转到指定播放位置
         *
         * @param view 歌词控件
         * @param time 选中播放进度
         */
        fun onPlayClick(view: LyricView, time: Long)
    }
}