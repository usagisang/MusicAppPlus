package com.gochiusa.musicapp.library

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RequestConstant {
    const val BASE_URL = "http://gochiusa.top:3000"
    const val UPDATE_URL = "http://gochiusa.top:8901"

    /**
     *  连接超时，单位为毫秒
     */
    const val CONNECTION_TIME_OUT = 8000L

    /**
     *  读取超时，单位为毫秒
     */
    const val READ_TIME_OUT = 8000L

    /**
     *  写入超时，单位为毫秒
     */
    const val WRITE_TIME_OUT = 8000L
}