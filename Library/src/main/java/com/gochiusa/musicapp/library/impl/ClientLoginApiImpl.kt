package com.gochiusa.musicapp.library.impl

import com.gochiusa.musicapp.library.util.DataUtil
import com.example.repository.RequestCallBack
import com.example.repository.api.ClientLoginApi
import com.example.repository.bean.UserJson
import com.gochiusa.musicapp.library.util.Md5Calculator
import com.gochiusa.musicapp.library.util.defaultSubscribe
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        loginApi.logout().enqueue(object : Callback<Unit> {
            override fun onFailure(call: Call<Unit>, t: Throwable) {}
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {}
        })
    }
}