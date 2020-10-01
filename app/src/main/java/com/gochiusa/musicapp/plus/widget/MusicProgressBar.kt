package com.gochiusa.musicapp.plus.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.gochiusa.musicapp.plus.R

class MusicProgressBar(context: Context, attributeSet: AttributeSet?,
                       defStyleAttr: Int, defStyleRes: Int):
    RelativeLayout(context, attributeSet, defStyleAttr, defStyleRes) {

    private val seekBar: SeekBar
    private val musicDurationText: TextView
    private val musicProgressText: TextView

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
    }

    /**
     * 设置歌曲持续时间的文本
     * @param durationText 歌曲时长（已经经过工具类计算而不是毫秒数）
     */
    fun setMusicDurationText(durationText: String) {
        musicDurationText.text = durationText
    }

    /**
     * 设置当前进度的文本
     * @param progressText  歌曲当前的播放进度（已经经过工具类计算而不是毫秒数）
     */
    fun setProgressText(progressText: String) {
        musicProgressText.text = progressText
    }

    /**
     * 更新进度条的进度
     */
    fun setSeekBarProgress(progress: Int) {
        seekBar.progress = progress
    }

    /**
     *  设置进度条的最大进度
     */
    fun setSeekBarMax(max: Int) {
        seekBar.max = max
    }

    fun setSeekBarChangeListener(listener: OnSeekBarChangeListener?) {
        seekBar.setOnSeekBarChangeListener(listener)
    }
}