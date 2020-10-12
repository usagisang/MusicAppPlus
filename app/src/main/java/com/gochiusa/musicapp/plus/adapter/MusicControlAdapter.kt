package com.gochiusa.musicapp.plus.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.base.PlayOrPauseClickListener
import com.gochiusa.musicapp.plus.entity.MusicControlHolder
import com.gochiusa.musicapp.plus.entity.PlayPattern
import com.gochiusa.musicapp.plus.util.PlaylistManager
import com.gochiusa.musicapp.plus.widget.MusicControlView


class MusicControlAdapter(private val context: Context): MusicControlView.Adapter() {

    /**
     * 主要的控件
     */
    lateinit var playOrPauseButton: Button
    private lateinit var playModelButton: Button
    private lateinit var playListButton: Button
    private lateinit var nextSongButton: Button
    private lateinit var lastSongButton: Button

    /**
     * 缓存控件的一些点击事件
     */
    var playListButtonClickListener: View.OnClickListener? = null
    var nextSongButtonClickListener: View.OnClickListener? = null
    var lastSongButtonClickListener: View.OnClickListener? = null

    var patternButtonClickListener: PatternButtonClickListener? = null

    /**
     * 储存播放按钮点击事件列表的集合
     */
    private val playOrPauseActionList: MutableList<PlayOrPauseClickListener> = mutableListOf()

    var pause = true
        set(value) {
            field = value
            refreshPlayOrPauseButton(value)
        }

    /**
     * 播放模式的枚举
     */
    private var playPattern: PlayPattern = PlaylistManager.playPattern
        private set(value) {
            field = value
            refreshPlayPatternButton(field)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicControlView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_entity_button, parent, false)
        return MusicControlHolder(itemView)
    }

    override fun onBindViewHolder(holder: MusicControlView.ViewHolder, position: Int) {
        if (holder is MusicControlHolder) {
            when (position) {
                0 -> {
                    playModelButton = holder.button
                    // 绑定背景图
                    refreshPlayPatternButton(playPattern)
                    // 绑定点击事件
                    bindPatternButtonListener(holder.button)
                }
                1 -> {
                    lastSongButton = holder.button
                    holder.button.setBackgroundResource(
                        R.drawable.ic_widget_skip_preview)
                    holder.button.setOnClickListener {
                        lastSongButtonClickListener?.onClick(it)
                    }
                }
                2 -> {
                    playOrPauseButton= holder.button
                    refreshPlayOrPauseButton(pause)
                    // 设置点击监听事件
                    bindPlayOrPauseButtonListener(holder.button)
                }
                3 -> {
                    nextSongButton = holder.button
                    holder.button.setBackgroundResource(R.drawable.ic_widget_skip_next)
                    holder.button.setOnClickListener {
                        nextSongButtonClickListener?.onClick(it)
                    }
                }
                4 -> {
                    playListButton = holder.button
                    holder.button.setBackgroundResource(R.drawable.ic_widget_playlist)
                    holder.button.setOnClickListener {
                        playListButtonClickListener?.onClick(it)
                    }

                }

            }
        }
    }

    fun addPlayOrPauseButtonListener(playOrPauseClickListener: PlayOrPauseClickListener) {
        playOrPauseActionList.add(playOrPauseClickListener)
    }

    override fun countItems(): Int {
        return 5
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

    /**
     *  刷新显示播放模式的按钮
     */
    private fun refreshPlayPatternButton(pattern: PlayPattern) {
        when (pattern) {
            PlayPattern.RANDOM -> {
                playModelButton.setBackgroundResource(R.drawable.ic_widget_shuffle)
            }
            PlayPattern.LIST_LOOP -> {
                playModelButton.setBackgroundResource(R.drawable.ic_widget_repeat_list)
            }
            PlayPattern.SINGLE_SONG_LOOP -> {
                playModelButton.setBackgroundResource(R.drawable.ic_widget_repeat_one)
            }
        }
    }

    private fun bindPatternButtonListener(button: Button) {
        button.setOnClickListener {
            // 更改循环模式
            playPattern = when (playPattern) {
                PlayPattern.LIST_LOOP -> {
                    PlayPattern.SINGLE_SONG_LOOP
                }
                PlayPattern.SINGLE_SONG_LOOP -> {
                    PlayPattern.RANDOM
                }
                PlayPattern.RANDOM -> {
                    PlayPattern.LIST_LOOP
                }
            }
            // 回调监听接口
            patternButtonClickListener?.onClick(playPattern)
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

    interface PatternButtonClickListener {
        fun onClick(playPattern: PlayPattern)
    }
}