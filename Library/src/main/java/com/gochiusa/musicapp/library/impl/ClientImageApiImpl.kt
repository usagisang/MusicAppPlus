package com.gochiusa.musicapp.library.impl

import com.gochiusa.musicapp.library.util.DataUtil
import com.example.repository.RequestCallBack
import com.example.repository.api.ClientImageApi
import com.example.repository.bean.BannerJson
import com.gochiusa.musicapp.library.util.defaultSubscribe

class ClientImageApiImpl : ClientImageApi {

    private val imageApi = DataUtil.retrofit.create(ImageApi::class.java)

    override fun getBanners(type: Int, callBack: RequestCallBack<BannerJson>) {
        defaultSubscribe(imageApi.getBanners(type.toString()), callBack)
    }

}