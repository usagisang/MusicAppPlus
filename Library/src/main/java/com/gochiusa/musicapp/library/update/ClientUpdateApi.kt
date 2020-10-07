package com.gochiusa.musicapp.library.update

import com.example.repository.RequestCallBack

interface ClientUpdateApi {
    /**
     *  获取版本信息
     */
    fun getUpdateInformation(requestCallBack: RequestCallBack<UpdateVersionJson>)
}