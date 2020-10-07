package com.gochiusa.musicapp.library.update

import com.example.repository.RequestCallBack
import com.gochiusa.musicapp.library.RequestConstant
import com.gochiusa.musicapp.library.util.defaultSubscribe
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ClientUpdateApiImpl: ClientUpdateApi {

    private val retrofit = Retrofit.Builder()
        .baseUrl(RequestConstant.UPDATE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .build()

    private val updateApi = retrofit.create<UpdateApi>(UpdateApi::class.java)

    override fun getUpdateInformation(requestCallBack: RequestCallBack<UpdateVersionJson>) {
        defaultSubscribe(updateApi.getUpdateInformation(
            "music_app_plus"), requestCallBack)
    }
}