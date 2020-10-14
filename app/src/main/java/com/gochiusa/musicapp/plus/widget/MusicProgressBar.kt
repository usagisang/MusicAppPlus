package com.gochiusa.musicapp.plus.widget

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.tasks.stage.StageActivity
import com.gochiusa.musicapp.plus.util.TimeCalculator

class MusicProgressBar(context: Context, attributeSet: AttributeSet?,
                       defStyleAttr: Int, defStyleRes: Int):
    RelativeLayout(context, attributeSet, defStyleAttr, defStyleRes) {

    private val seekBar: SeekBar
    private val musicDurationText: TextView
    private val musicProgressText: TextView

    private var lyricView: LyricView? = null

    private var animator: SeekBarAnimator? = null

    constructor(context: Context): this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?):
            this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int):
            this(context, attributeSet, defStyleAttr, 0)


    /**
     * 这个标志是为了防止定时任务干扰进度条拖动
     */
    var seekBarChanging = false

    init {
        // 将XML转换为View并关联到这个父控件
        LayoutInflater.from(context).inflate(R.layout.widget_progress_bar, this)
        seekBar = findViewById(R.id.bar_music_progress)
        musicDurationText = findViewById(R.id.tv_music_full_length)
        musicProgressText = findViewById(R.id.tv_music_progress)
        // 预设SeekBar的最大进度为0
        setSeekBarMax(0)
    }

    /**
     * 更新进度条的进度
     */
    fun setSeekBarProgress(progress: Int) {
        seekBar.progress = progress
        setProgressText(progress)
    }

    /**
     * 更新显示进度的文本
     */
    fun setProgressText(progress: Int) {
        musicProgressText.text = TimeCalculator.calculateSongDuration(progress)
    }

    /**
     *  设置进度条的最大进度
     */
    fun setSeekBarMax(max: Int) {
        seekBar.max = max
        musicDurationText.text = TimeCalculator.calculateSongDuration(max)
    }

    fun setSeekBarChangeListener(listener: OnSeekBarChangeListener?) {
        seekBar.setOnSeekBarChangeListener(listener)
    }

    fun bindLyricView(lyricView: LyricView) {
        this.lyricView = lyricView
    }

    fun startSeekBarAnimator(connection: StageActivity.MusicServiceConnection) {
        if (animator == null) {
            animator = SeekBarAnimator(connection)
        }
        animator?.start()
    }

    fun pauseSeekBarAnimator() {
        animator?.pause()
    }


    fun cancelSeekBarAnimator() {
        animator?.cancel()
        animator = null
    }

    fun reset() {
        cancelSeekBarAnimator()
        setSeekBarProgress(0)
        setSeekBarMax(0)
    }


    private inner class SeekBarAnimator(val connection: StageActivity.MusicServiceConnection):
        ValueAnimator(), ValueAnimator.AnimatorUpdateListener {

        init {
            // 先进行一次更新
            onAnimationUpdate(this)
            // 设置持续时间
            duration = connection.binderInterface?.duration?.toLong() ?: Long.MAX_VALUE
            // 设置更新的值
            setIntValues(1, 10000)
            // 500ms刷新一次
            setFrameDelay(500L)
            // 使用线性插值器
            interpolator = LinearInterpolator()
            addUpdateListener(this)
        }

        override fun onAnimationUpdate(animation: ValueAnimator?) {
            connection.binderInterface?.let {
                val progress = it.progress
                // 如果SeekBar没有被拖动，更新SeekBar的进度
                if (! seekBarChanging) {
                    setSeekBarProgress(progress)
                }
                lyricView?.let {view ->
                    view.scrollToLine(
                        TimeCalculator.getIndexWithProgress(progress, view.getSentenceList()))
                }
            }
        }
    }
}