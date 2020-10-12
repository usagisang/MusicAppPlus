package com.gochiusa.musicapp.plus.util

import android.content.Context
import android.content.SharedPreferences
import com.gochiusa.musicapp.library.util.ContextProvider.context
import com.gochiusa.musicapp.plus.entity.PlayPattern
import com.gochiusa.musicapp.plus.entity.Song
import java.util.*

object PlaylistManager {
    /**
     * 播放列表
     */
    val playlist: MutableList<Song> = mutableListOf()

    /**
     *  当前播放模式，默认是列表循环
     */
    var playPattern: PlayPattern = readPlayPatternData()
        set(value) {
            field = value
            writePlayPatternToFile()
        }

    /**
     * 当前歌曲位置，默认为0
     */
    var songPlayingPosition: Int = 0
       set(value) {
           if (isPositionReasonable(value)) {
               // 检验播放位置的合理性，避免错误改变播放位置
               field = value
           }
       }

    val isPlayListEmpty: Boolean
        get() = playlist.isEmpty()
    /**
     * 下一首播放队列
     */
    private val playAtNextDeque: ArrayDeque<Int> = ArrayDeque()

    /**
     *  持久化储存播放模式的文件名
     */
    private const val PATTERN_SAVE_NAME = "playPattern"

    /**
     *  持久化储存播放模式的键
     */
    private const val PATTERN_NAME_SAVE_KEY = "playPatternKey"

    /**
     * 判断传入的位置在播放列表中是否合理（不越界）
     * @return 合理（不越界）返回true，不合理（越界）返回false。事实上如果列表为空，传入0也会返回false
     */
    fun isPositionReasonable(checkPosition: Int): Boolean {
        return checkPosition >= 0 && checkPosition < playlist.size
    }

    /**
     * 为播放器返回上一首歌曲信息用于播放
     * @return 上一首播放的曲目
     */
    fun previousSong(): Song? {
        return if (isPlayListEmpty) {
            null
        } else {
            playlist[lastPlayPosition]
        }
    }

    /**
     * 得到目前播放位置处的曲目
     * @return 播放位置处的曲目
     */
    fun nowSong() : Song? {
        return if (playlist.isEmpty()) {
            // 如果播放列表为空，则返回null
            null
        } else  {
            playlist[songPlayingPosition]
        }
    }

    /**
     * 为播放器返回下一首歌曲信息用于播放
     * @return 下一首要播放的曲目
     */
    fun nextSong(): Song? {
        return if (playlist.isEmpty()) {
            null
        } else {
            playlist[nextPlayPosition]
        }
    }

    fun addAllSongToPlaylist(otherList: List<Song>) {
        playlist.addAll(otherList)
    }

    /**
     * 移除列表的所有歌曲
     */
    fun removeAllSong() {
        if (! isPlayListEmpty) {
            playlist.clear()
            // 重置播放位
            songPlayingPosition = 0
        }
    }
    /**
     * 比较列表是否和播放列表相等
     */
    fun listIsEqual(arrayList: List<Song?>): Boolean {
        return playlist == arrayList
    }

    /**
     * 将歌曲添加到播放队列，同时标识下一首播放队列需要播放的位置
     */
    fun addSongForPlayAtNext(playAtNextList: List<Song>) {
        if (isPlayListEmpty) {
            // 如果列表为空，直接将歌曲全部加入播放列表
            playlist.addAll(playAtNextList)
            for (i in playAtNextList.indices) {
                // 在队列尾部添加下一首播放的位置
                playAtNextDeque.add(i)
            }
        } else {
            for (i in 1..playAtNextList.size) {
                // 将歌曲添加进播放列表
                playlist.add(songPlayingPosition + i, playAtNextList[i - 1])
                // 在队列尾部添加下一首播放的位置
                playAtNextDeque.add(songPlayingPosition + i)
            }
        }
    }

    /**
     * 根据播放模式的不同计算上一首歌曲的位置
     */
    private val lastPlayPosition: Int
        get() {
            val lastPosition: Int = when (playPattern) {
                PlayPattern.RANDOM -> {
                    calculatorRandomLast()
                }
                PlayPattern.SINGLE_SONG_LOOP, PlayPattern.LIST_LOOP -> {
                    calculatorListLoopLast()
                }
            }
            // 更新当前位置
            songPlayingPosition = lastPosition
            return lastPosition
        }


    /**
     * 根据播放模式的不同计算下一首歌曲的位置
     */
    private val nextPlayPosition: Int
        get() {
            var nextPosition: Int? = playAtNextDeque.poll()
            if (nextPosition == null) {
                // 如果下一首播放的队列弹出null，按照当前的播放模式计算下一首歌曲位置
                nextPosition = when (playPattern) {
                    PlayPattern.RANDOM -> {
                        calculatorRandomNext()
                    }
                    PlayPattern.SINGLE_SONG_LOOP, PlayPattern.LIST_LOOP -> {
                        calculatorListLoopNext()
                    }
                }
            }
            // 更新当前位置
            songPlayingPosition = nextPosition
            return nextPosition
        }


    /**
     * 计算列表循环播放模式下的上一首的位置（单曲循环使用一样的逻辑）
     * @return 上一首的位置
     */
    private fun calculatorListLoopLast(): Int {
        return if (songPlayingPosition > 0) {
            // 如果不处于列表的第一个位置
            songPlayingPosition - 1
        } else {
            // 如果在列表的第一个位置
            playlist.size - 1
        }
    }
    /**
     * 计算列表循环播放模式下的下一首的位置（单曲循环使用一样的逻辑）
     * @return 下一首的位置
     */
    private fun calculatorListLoopNext(): Int {
        var nextPosition = 0
        // 不在列表末尾才继续累加位置，否则预设的0就是下一首的位置
        if (songPlayingPosition < playlist.size - 1) {
            nextPosition = songPlayingPosition + 1
        }
        return nextPosition
    }

    /**
     * 计算随机模式播放下的上一首位置
     * @return 上一首的位置
     */
    private fun calculatorRandomLast(): Int {
        var lastPosition: Int
        // 如果播放列表只有一首歌，使用列表循环的逻辑处理(防止下面的随机逻辑产生bug)
        if (playlist.size == 1) {
            return calculatorListLoopLast()
        }
        // 如果随机数是当前位置就再生成一遍
        do {
            lastPosition = (Math.random() * playlist.size).toInt()
        } while (lastPosition == songPlayingPosition)
        return lastPosition
    }
    /**
     * 计算随机模式播放下的下一首位置
     * @return 下一首的位置
     */
    private fun calculatorRandomNext(): Int {
        var nextPosition: Int
        // 如果播放列表只有一首歌，使用列表循环的逻辑处理(防止下面的随机逻辑产生bug)
        if (playlist.size == 1) {
            return calculatorListLoopNext()
        }
        // 如果随机数是当前位置就再生成一遍
        do {
            nextPosition = (Math.random() * playlist.size).toInt()
        } while (nextPosition == songPlayingPosition)
        return nextPosition
    }

    /**
     *  从持久化数据源中读取播放模式的信息
     */
    private fun readPlayPatternData(): PlayPattern {
        val patternPreferences: SharedPreferences = context.getSharedPreferences(
            PATTERN_SAVE_NAME, Context.MODE_PRIVATE)
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
    fun writePlayPatternToFile() {
        val editor: SharedPreferences.Editor = context.getSharedPreferences(PATTERN_SAVE_NAME,
            Context.MODE_PRIVATE).edit()
        editor.putString(PATTERN_NAME_SAVE_KEY, playPattern.name)
        editor.apply()
    }
}