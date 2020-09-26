package com.example.repository.api

import com.example.repository.RequestCallBack
import com.example.repository.bean.UserJson

/**
 * 暴露给用户使用的Api.
 * */
interface ClientLoginApi{
    fun login(username: String, password: String, callback : RequestCallBack<UserJson>)
    fun refreshLogin()
    fun getLoginStatus(callback : RequestCallBack<UserJson>)
    fun logout()
}