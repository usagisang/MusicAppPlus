package com.gochiusa.musicapp.plus.adapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.entity.MusicControlHolder
import com.gochiusa.musicapp.plus.entity.PlayPattern
import com.gochiusa.musicapp.plus.widget.MusicControlView


class MusicControlAdapter(private val context: Context): MusicControlView.Adapter() {

    /**
     * 主要的控件
     */
    private var playOrPauseButton: Button? = null
    private var playModelButton: Button? = null
    private var playListButton: Button? = null
    private var nextSongButton: Button? = null
    private var lastSongButton: Button? = null

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
    var playPattern: PlayPattern = readPlayPatternData()
        private set(value) {
            field = value
            refreshPlayPatternButton(field)
            writePlayPatternToFile()
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
                    lastSongButtonClickListener?.let {
                        holder.button.setOnClickListener(it)
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
                    nextSongButtonClickListener?.let {
                        holder.button.setOnClickListener(it)
                    }
                }
                4 -> {
                    playListButton = holder.button
                    holder.button.setBackgroundResource(R.drawable.ic_widget_playlist)
                    playListButtonClickListener?.let {
                        holder.button.setOnClickListener(it)
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
            playOrPauseButton?.setBackgroundResource(R.drawable.ic_widget_play_circle)
        } else {
            playOrPauseButton?.setBackgroundResource(R.drawable.ic_widget_pause_circle)
        }
    }

    /**
     *  从持久化数据源中读取播放模式的信息
     */
    private fun readPlayPatternData(): PlayPattern {
        val patternPreferences: SharedPreferences = context.getSharedPreferences(
            PATTERN_SAVE_NAME,Context.MODE_PRIVATE)
        return when (patternPreferences.getString(PATTERN_NAME_SAVE_KEY, "")) {
            PlayPattern.SINGLE_SONG_LOOP.name -> {
                PlayPattern.SINGLE_SONG_LOOP
            }
            PlayPattern.LIST_LOOP.name -> {
                PlayPattern.LIST_LOOP
            }
            PlayPattern.RANDOM.name -> {
                PlayPattern.RANDOM
            }
            else -> {
                PlayPattern.LIST_LOOP
            }
        }
    }

    /**
     *  将播放模式信息写出到持久化数据源中
     */
    private fun writePlayPatternToFile() {
        val editor: SharedPreferences.Editor =
            context.getSharedPreferences(PATTERN_SAVE_NAME, Context.MODE_PRIVATE).edit()
        editor.putString(PATTERN_NAME_SAVE_KEY, playPattern.name)
        editor.apply()
    }

    /**
     *  刷新显示播放模式的按钮
     */
    private fun refreshPlayPatternButton(pattern: PlayPattern) {
        when (pattern) {
            PlayPattern.RANDOM -> {
                playModelButton?.setBackgroundResource(R.drawable.ic_widget_shuffle)
            }
            PlayPattern.LIST_LOOP -> {
                playModelButton?.setBackgroundResource(R.drawable.ic_widget_repeat_list)
            }
            PlayPattern.SINGLE_SONG_LOOP -> {
                playModelButton?.setBackgroundResource(R.drawable.ic_widget_repeat_one)
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



    companion object {
        /**
         *  持久化储存播放模式的文件名
         */
        private const val PATTERN_SAVE_NAME = "playPattern"

        /**
         *  持久化储存播放模式的键
         */
        private const val PATTERN_NAME_SAVE_KEY = "playPatternKey"
    }

    interface PatternButtonClickListener {
        fun onClick(playPattern: PlayPattern)
    }

    /**
     *  当播放按钮被点击之后会被回调的接口
     */
    interface PlayOrPauseClickListener {
        /**
         * 播放按钮被点击后回调
         * @param isPause 播放按钮当前的显示状态，false表示显示为暂停键（处于播放状态），true则相反
         *                注意回调时状态已经切换完毕，如果正在播放音乐，{@code isPause}为false，
         *                点击之后，{@code isPause}改变为true（变为暂停状态）并按顺序回调各个接口
         */
        fun onClick(isPause: Boolean)
    }
}