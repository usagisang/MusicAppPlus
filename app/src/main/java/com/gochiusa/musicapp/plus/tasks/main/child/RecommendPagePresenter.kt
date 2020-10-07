package com.gochiusa.musicapp.plus.tasks.main.child

import android.app.DownloadManager
import android.net.Uri
import android.os.Environment
import com.example.repository.RequestCallBack
import com.gochiusa.musicapp.library.update.UpdateVersionJson
import com.gochiusa.musicapp.library.util.ContextProvider
import com.gochiusa.musicapp.library.util.DataUtil
import com.gochiusa.musicapp.plus.application.MainApplication
import com.gochiusa.musicapp.plus.base.BasePresenterImpl
import com.gochiusa.musicapp.plus.entity.UpdateInformation
import com.gochiusa.musicapp.plus.util.LogUtil
import com.gochiusa.musicapp.plus.util.VersionUtil


class RecommendPagePresenter(view: RecommendContract.View):
    BasePresenterImpl<RecommendContract.View>(view), RecommendContract.Presenter {
    override fun requestUpdate() {
        DataUtil.clientUpdateApi.getUpdateInformation(object : RequestCallBack<UpdateVersionJson> {
            override fun callback(data: UpdateVersionJson) {
                val nowVersion = VersionUtil.getVersionCode()
                if (nowVersion < (data.versionCode ?: 0)) {
                    view?.canUpdate(UpdateInformation(data))
                } else {
                    view?.notUpdate()
                }
            }
            override fun error(errorMsg: String) {
                LogUtil.printToConsole(errorMsg)
                view?.showToast(UPDATE_ERROR)
            }

        })
    }

    override fun downloadUpdate() {
        // 创建下载请求
        val request = DownloadManager.Request(Uri.parse(UPDATE_DOWNLOAD_URL))
        // 下载中和下载完后都显示通知栏
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        // 使用系统默认的下载路径
        val apkName = "MusicAppPlus.apk"
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName)
        // 通知栏标题
        request.setTitle(apkName)
        // 通知栏描述信息
        request.setDescription(UPDATE_NOTIFICATION_DESCRIBE + apkName)
        // 设置MIME类型
        request.setMimeType("application/vnd.android.package-archive")

        MainApplication.downloadManager.enqueue(request)
    }

    companion object {
        private const val UPDATE_ERROR = "检查更新失败"
        private const val UPDATE_DOWNLOAD_URL =
            "http://gochiusa.top:8901/update/download?name=music_app_plus"
        private const val UPDATE_NOTIFICATION_DESCRIBE = "正在下载: "
    }

}