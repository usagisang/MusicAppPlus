package com.example.repository.api

import com.example.repository.RequestCallBack
import com.example.repository.bean.*


/**
 * 暴露给用户使用的，与音乐有关的Api.
 * */
interface ClientMusicApi {
    /**
     * 获取用户的歌单的详细信息，传入用户id.
     * */
    fun getUserPlayList(uid: Long, callBack: RequestCallBack<UserPlayListJson>)

    /**
     * 传入歌单id，获取歌单包含的歌曲.
     * */
    fun getSongListDetail(id: Long, callBack: RequestCallBack<SongIdsJson>)

    /**
     * 传入歌曲的多个id，获取歌曲详情，歌曲详情有的信息：歌曲名字、歌手名字、专辑图片...等信息
     * */
    fun getSongsDetail(ids: String, callBack: RequestCallBack<SongDetailJson>)

    /**
     * 获取歌曲url、码率等信息，传入歌曲的单个id
     * */
    fun getSongPlay(id: Long, callBack: RequestCallBack<SongPlayJson>)

    /**
     * 获取歌曲url、码率等信息，传入歌曲的多个id
     * */
    fun getSongsPlay(ids: String, callBack: RequestCallBack<SongPlayJson>)

    /**
     * 获取歌曲的歌词.
     * */
    fun getSongLyric(id: Long, callBack: RequestCallBack<LyricJson>)

}