package com.gochiusa.musicapp.library.impl

import com.gochiusa.musicapp.library.util.DataUtil
import com.example.repository.RequestCallBack
import com.example.repository.api.ClientMusicApi
import com.example.repository.bean.*
import com.gochiusa.musicapp.library.util.defaultSubscribe

class ClientMusicApiImpl: ClientMusicApi {

    private val musicApi: MusicApi = DataUtil.retrofit.create(MusicApi::class.java)

    override fun getUserPlayList(uid: Long, callBack: RequestCallBack<UserPlayListJson>) {
        defaultSubscribe(musicApi.getUserPlayList(uid.toString()), callBack)
    }

    override fun getSongListDetail(id: Long, callBack: RequestCallBack<SongIdsJson>) {
        defaultSubscribe(musicApi.getSongListDetail(id.toString()), callBack)
    }

    override fun getSongsDetail(ids: String, callBack: RequestCallBack<SongDetailJson>) {
        defaultSubscribe(musicApi.getSongsDetail(ids), callBack)
    }

    override fun getSongPlay(id: Long, callBack: RequestCallBack<SongPlayJson>) {
        getSongsPlay(id.toString(), callBack)
    }

    override fun getSongsPlay(ids: String, callBack: RequestCallBack<SongPlayJson>) {
        defaultSubscribe(musicApi.getSongPlay(ids), callBack)
    }

    override fun getSongLyric(id: Long, callBack: RequestCallBack<LyricJson>) {
        defaultSubscribe(musicApi.getSongLyric(id.toString()), callBack)
    }
}