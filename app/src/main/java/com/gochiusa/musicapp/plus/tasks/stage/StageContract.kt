package com.gochiusa.musicapp.plus.tasks.stage

import com.example.repository.bean.LyricJson
import com.gochiusa.musicapp.plus.base.BaseView
import com.gochiusa.musicapp.plus.entity.Song

interface StageContract {
    interface View: BaseView {
        fun loadLyricSuccess(data: LyricJson)
        fun loadLyricError(errorMsg: String)
    }
    interface Presenter {
        fun requestLyric(id: Long)
        fun downloadSong(song: Song)
    }
    
}