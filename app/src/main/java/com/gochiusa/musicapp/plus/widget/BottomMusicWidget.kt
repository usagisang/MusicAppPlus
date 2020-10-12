package com.gochiusa.musicapp.plus.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.base.PlayOrPauseClickListener

class BottomMusicWidget(context: Context, attributeSet: AttributeSet?,
                        defStyleAttr: Int, defStyleRes: Int):
    RelativeLayout(context, attributeSet, defStyleAttr, defStyleRes) {

    constructor(context: Context): this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?):
            this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int):
            this(context, attributeSet, defStyleAttr, 0)

    val roundImageView: RoundImageView
    val musicNameTextView: TextView
    val artistTextView: TextView
    val playOrPauseButton: Button
    val playlistButton: Button

    /**
     *  是否为暂停状态
     */
    var pause = true
        set(value) {
            field = value
            refreshPlayOrPauseButton(value)
        }

    /**
     * 储存播放按钮点击事件列表的集合
     */
    private val playOrPauseActionList: MutableList<PlayOrPauseClickListener> = mutableListOf()

    init {
        // 将XML转换为View并关联到这个父控件
        LayoutInflater.from(context).inflate(R.layout.widget_bottom_music, this)
        roundImageView = findViewById(R.id.iv_widget_album_image)
        musicNameTextView = findViewById(R.id.tv_widget_scroll_music_name)
        artistTextView = findViewById(R.id.tv_widget_bottom_artist_name)
        playOrPauseButton = findViewById(R.id.btn_widget_play_or_pause)
        playlistButton = findViewById(R.id.btn_widget_playlist)
        // 绑定播放按钮的点击事件
        bindPlayOrPauseButtonListener(playOrPauseButton)
    }

    fun addPlayOrPauseButtonListener(playOrPauseClickListener: PlayOrPauseClickListener) {
        playOrPauseActionList.add(playOrPauseClickListener)
    }

    /**
     *  刷新播放按钮的显示
     */
    private fun refreshPlayOrPauseButton(isPause: Boolean) {
        if (isPause) {
            playOrPauseButton.setBackgroundResource(R.drawable.ic_widget_play_circle)
        } else {
            playOrPauseButton.setBackgroundResource(R.drawable.ic_widget_pause_circle)
        }
    }

    private fun bindPlayOrPauseButtonListener(button: Button) {
        button.setOnClickListener {
            // 状态变量取反
            pause = !pause
            for (listener in playOrPauseActionList) {
                listener.onClick(pause)
            }
        }
    }
}