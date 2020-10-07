package com.gochiusa.musicapp.library.update

import retrofit2.http.GET
import retrofit2.http.Query
import rx.Observable

interface UpdateApi {

    @GET("update/checkVersion")
    fun getUpdateInformation(@Query("name") appName: String): Observable<UpdateVersionJson>
}