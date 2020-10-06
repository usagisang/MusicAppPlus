package com.gochiusa.musicapp.library.impl

import com.gochiusa.musicapp.library.util.DataUtil
import com.example.repository.RequestCallBack
import com.example.repository.api.ClientLoginApi
import com.example.repository.bean.UserJson
import com.gochiusa.musicapp.library.util.Md5Calculator
import com.gochiusa.musicapp.library.util.defaultSubscribe

class ClientLoginApiImpl: ClientLoginApi {

    private val loginApi: LoginApi = DataUtil.retrofit.create<LoginApi>(
        LoginApi::class.java)

    override fun login(username: String, password: String, callback: RequestCallBack<UserJson>) {
        defaultSubscribe(loginApi.login(username, Md5Calculator.stringToMD5(password)), callback)
    }

    override fun refreshLogin() {
        loginApi.refreshLogin()
    }

    override fun getLoginStatus(callback: RequestCallBack<UserJson>) {
        defaultSubscribe(loginApi.getLoginStatus(), callback)
    }

    override fun logout() {
        loginApi.logout()
    }

}