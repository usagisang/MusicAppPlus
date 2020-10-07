package com.gochiusa.musicapp.plus.tasks.main.child

import com.gochiusa.musicapp.plus.base.BasePresenter
import com.gochiusa.musicapp.plus.base.BaseView
import com.gochiusa.musicapp.plus.entity.UpdateInformation

interface RecommendContract {
    interface View: BaseView {
        /**
         * 可以升级时回调
         */
        fun canUpdate(updateInformation: UpdateInformation)

        /**
         * 无法升级时回调
         */
        fun notUpdate()
    }
    interface Presenter: BasePresenter {
        /**
         * 请求新版本数据
         */
        fun requestUpdate()

        /**
         * 请求下载新版本apk
         */
        fun downloadUpdate()
    }
}