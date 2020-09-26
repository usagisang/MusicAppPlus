package com.gochiusa.musicapp.library.impl

import com.example.repository.bean.*
import retrofit2.http.GET
import retrofit2.http.Query
import rx.Observable

interface MusicApi {

    @GET("user/playlist")
    fun getUserPlayList(@Query("uid") uid: String): Observable<UserPlayListJson>

    @GET("playlist/detail")
    fun getSongListDetail(@Query("id") id: String): Observable<SongIdsJson>

    @GET("song/detail")
    fun getSongsDetail(@Query("ids") ids: String): Observable<SongDetailJson>

    /**
     *  注意获取多个播放地址和获取一个播放地址是一样的
     */
    @GET("song/url")
    fun getSongPlay(@Query("id") id: String): Observable<SongPlayJson>


    @GET("lyric")
    fun getSongLyric(@Query("id") id: String): Observable<LyricJson>
}