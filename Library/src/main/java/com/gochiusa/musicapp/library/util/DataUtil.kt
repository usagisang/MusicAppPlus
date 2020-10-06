package com.gochiusa.musicapp.library.util

import com.example.repository.api.ClientLoginApi
import com.example.repository.api.ClientMusicApi
import com.example.repository.RequestCallBack
import com.example.repository.api.ClientSearchApi
import com.gochiusa.musicapp.library.PersistenceCookieJar
import com.gochiusa.musicapp.library.RequestConstant
import com.gochiusa.musicapp.library.impl.ClientLoginApiImpl
import com.gochiusa.musicapp.library.impl.ClientMusicApiImpl
import com.gochiusa.musicapp.library.impl.ClientSearchApiImpl
import com.gochiusa.musicapp.library.impl.CookieStoreImpl
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import java.util.concurrent.TimeUnit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


object DataUtil {

    private var persistenceCookieJar = PersistenceCookieJar(
        CookieStoreImpl(ContextProvider.context))

    private var okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .cookieJar(persistenceCookieJar)
        .connectTimeout(RequestConstant.CONNECTION_TIME_OUT, TimeUnit.MILLISECONDS)
        .readTimeout(RequestConstant.READ_TIME_OUT, TimeUnit.MILLISECONDS)
        .writeTimeout(RequestConstant.WRITE_TIME_OUT, TimeUnit.MILLISECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(RequestConstant.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .build()


    val loginApi: ClientLoginApi = ClientLoginApiImpl()
    val clientMusicApi: ClientMusicApi = ClientMusicApiImpl()
    val clientSearchApi : ClientSearchApi = ClientSearchApiImpl()
    val clientImageApi: ClientMusicApi = ClientMusicApiImpl()

    /**
     * OkHttp解析，直接返回Json数据.
     * */
    fun getJsonData(url: String, callback: RequestCallBack<String>) {
        val request = Request.Builder()
            .url(url)
            .build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.error(e.message ?: "UnKnown_error")
            }

            override fun onResponse(call: Call, response: Response) {
                callback.callback(response.toString())
            }

        })
    }

    fun clearAllCookies() {
        persistenceCookieJar.clearCookies()
    }

}