package com.example.repository.api

import com.example.repository.RequestCallBack
import com.example.repository.bean.BannerJson

interface ClientImageApi {
    /**
     * 获取轮播图.
     * */
    fun getBanners(type : Int ,callBack: RequestCallBack<BannerJson>)
}