package com.gochiusa.musicapp.plus.tasks.main.child

import com.example.repository.RequestCallBack
import com.example.repository.bean.UserJson
import com.gochiusa.musicapp.library.util.DataUtil
import com.gochiusa.musicapp.plus.base.BasePresenterImpl
import com.gochiusa.musicapp.plus.entity.User
import com.gochiusa.musicapp.plus.util.LogUtil

class UserPagePresenter(view: UserContract.View):
    BasePresenterImpl<UserContract.View>(view), UserContract.Presenter {
    override fun checkLogin() {
        DataUtil.loginApi.getLoginStatus(object: RequestCallBack<UserJson> {
            override fun callback(data: UserJson) {
                view?.showLoading(false)
                data.profile?.let {
                    view?.loginSuccess(User(it.userId ?: 0L,
                        it.nickname ?: "", it.avatarUrl ?: ""))
                }
            }
            override fun error(errorMsg: String) {
                LogUtil.printToConsole(errorMsg)
                view?.resetInterface()
            }
        })
    }

    override fun loginRequest(phone: String, password: String) {
        DataUtil.loginApi.login(phone, password, object: RequestCallBack<UserJson> {
            override fun callback(data: UserJson) {
                data.profile?.let {
                    view?.showLoading(false)
                    view?.loginSuccess(User(it.userId ?: 0L,
                        it.nickname, it.avatarUrl))
                }
            }

            override fun error(errorMsg: String) {
                LogUtil.printToConsole(errorMsg)
                view?.let {
                    it.showLoading(false)
                    it.showToast(LOGIN_ERROR_TIP)
                }
            }
        })
    }

    override fun logout() {
        DataUtil.loginApi.logout()
        DataUtil.clearAllCookies()
    }


    companion object {
        private const val LOGIN_ERROR_TIP = "用户名或密码错误，请重试"
    }
}