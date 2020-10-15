package com.gochiusa.musicapp.library.impl

import com.example.repository.bean.UserJson
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import rx.Observable

interface LoginApi {

    @POST("login/cellphone")
    @FormUrlEncoded
    fun login(@Field("phone") phone: String,
              @Field("md5_password") password: String): Observable<UserJson>

    @GET("login/refresh")
    fun refreshLogin(): Observable<Unit>
    @GET("login/status")
    fun getLoginStatus(): Observable<UserJson>
    @GET("logout")
    fun logout(): Call<Unit>
}