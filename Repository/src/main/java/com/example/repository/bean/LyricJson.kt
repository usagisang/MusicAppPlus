package com.example.repository.bean

/**
 * 获取歌词.
 * */
class LyricJson {
    var lrc: LRCText? = null

    class LRCText {
        var lyric: String? = null
    }
}