package com.gochiusa.musicapp.library.impl

import com.example.repository.bean.SearchDefaultJson
import com.example.repository.bean.SearchSongJson
import retrofit2.http.GET
import retrofit2.http.Query
import rx.Observable

interface SearchApi {

    @GET("search")
    fun getSearchSongs(@Query("limit") limit: Int, @Query("offset") offset: Int,
                       @Query("type") type: Int,
                       @Query("keywords") keyword: String): Observable<SearchSongJson>

    @GET("search/default")
    fun getDefaultKeywords(): Observable<SearchDefaultJson>
}