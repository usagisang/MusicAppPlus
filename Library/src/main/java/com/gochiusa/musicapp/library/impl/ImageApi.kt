package com.gochiusa.musicapp.library.impl

import com.example.repository.bean.BannerJson
import retrofit2.http.GET
import retrofit2.http.Query
import rx.Observable

interface ImageApi {

    @GET("banner")
    fun getBanners(@Query("type") type : String): Observable<BannerJson>
}