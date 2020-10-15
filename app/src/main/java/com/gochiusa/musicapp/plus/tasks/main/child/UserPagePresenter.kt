package com.gochiusa.musicapp.plus.tasks.main.child

import com.example.repository.RequestCallBack
import com.example.repository.bean.UserJson
import com.example.repository.bean.UserPlayListJson
import com.gochiusa.musicapp.library.util.DataUtil
import com.gochiusa.musicapp.plus.base.BasePresenterImpl
import com.gochiusa.musicapp.plus.entity.User
import com.gochiusa.musicapp.plus.entity.UserPlaylist
import com.gochiusa.musicapp.plus.util.LogUtil
import com.gochiusa.musicapp.plus.util.UserManager

class UserPagePresenter(view: UserContract.View):
    BasePresenterImpl<UserContract.View>(view), UserContract.Presenter {
    override fun checkLogin() {
        DataUtil.loginApi.getLoginStatus(object: RequestCallBack<UserJson> {
            override fun callback(data: UserJson) {
                view?.showLoading(false)
                data.profile?.let {
                    val user = User(it.userId ?: 0L, it.nickname ?: "",
                        it.avatarUrl ?: "")
                    UserManager.user = user
                    view?.loginSuccess(user)
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
                    val user = User(it.userId ?: 0L, it.nickname ?: "",
                        it.avatarUrl ?: "")
                    UserManager.user = user
                    view?.loginSuccess(user)
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
        UserManager.user = null
        DataUtil.clearAllCookies()
    }

    override fun requestUserPlaylist(userId: Long) {
        DataUtil.clientMusicApi.getUserPlayList(userId, object : RequestCallBack<UserPlayListJson> {
            override fun callback(data: UserPlayListJson) {
                val list = mutableListOf<UserPlaylist>()
                if (data.playlist == null) {
                    error(LOAD_USER_PLAYLIST_FAILED)
                }
                for (eachData in data.playlist!!) {
                    list.add(UserPlaylist(eachData))
                }
                view?.loadUserPlaylistSuccess(list)
            }

            override fun error(errorMsg: String) {
                LogUtil.printToConsole(errorMsg)
            }

        })
    }


    companion object {
        private const val LOGIN_ERROR_TIP = "用户名或密码错误，请重试"
        private const val LOAD_USER_PLAYLIST_FAILED = "用户歌单数据为空"
    }
}