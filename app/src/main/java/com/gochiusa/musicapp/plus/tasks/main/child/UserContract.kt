package com.gochiusa.musicapp.plus.tasks.main.child

import com.gochiusa.musicapp.plus.base.BasePresenter
import com.gochiusa.musicapp.plus.base.BaseView
import com.gochiusa.musicapp.plus.entity.User
import com.gochiusa.musicapp.plus.entity.UserPlaylist

interface UserContract {
    interface View: BaseView {
        /**
         *  登录成功回调的方法
         */
        fun loginSuccess(user: User)

        /**
         *  重置界面为未登录状态
         */
        fun resetInterface()

        /**
         * 指示界面上的加载状态，必须在主线程调用
         * @param loading 如果为true，界面则会显示正在集在的一些控件，
         *                以提示用户正在进行加载，传入false将会隐藏这些控件
         */
        fun showLoading(loading: Boolean)

        /**
         * 歌单信息加载成功后回调的方法
         */
        fun loadUserPlaylistSuccess(userPlaylist: MutableList<UserPlaylist>)
    }
    interface Presenter: BasePresenter {
        /**
         * 检查登陆状态
         */
        fun checkLogin()

        /**
         *  登录操作
         */
        fun loginRequest(phone: String, password: String)

        /**
         *  退出登陆
         */
        fun logout()

        /**
         * 请求用户总的歌单信息
         */
        fun requestUserPlaylist(userId: Long)
    }
}