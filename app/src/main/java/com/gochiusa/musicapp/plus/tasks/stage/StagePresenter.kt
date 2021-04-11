package com.gochiusa.musicapp.plus.tasks.stage

import android.app.DownloadManager
import android.net.Uri
import android.os.Environment
import com.example.repository.RequestCallBack
import com.example.repository.bean.LyricJson
import com.example.repository.bean.SongPlayJson
import com.gochiusa.musicapp.library.util.DataUtil
import com.gochiusa.musicapp.plus.application.MainApplication
import com.gochiusa.musicapp.plus.base.BasePresenterImpl
import com.gochiusa.musicapp.plus.entity.Song
import com.gochiusa.musicapp.plus.tasks.main.child.RecommendPagePresenter
import com.gochiusa.musicapp.plus.util.LogUtil
import com.gochiusa.musicapp.plus.util.StringContract

class StagePresenter(view: StageContract.View)
    : BasePresenterImpl<StageContract.View>(view), StageContract.Presenter {
    override fun requestLyric(id: Long) {
        DataUtil.clientMusicApi.getSongLyric(id, object : RequestCallBack<LyricJson> {
            override fun callback(data: LyricJson) {
                data.lrc?.lyric?.let {
                    view?.loadLyricSuccess(data)
                }?: error("请求的数据为空")
            }
            override fun error(errorMsg: String) {
                LogUtil.printToConsole(errorMsg)
                view?.loadLyricError(errorMsg)
            }
        })

    }

    override fun downloadSong(song: Song) {
        DataUtil.clientMusicApi.getSongsPlay(song.id.toString(),
            object : RequestCallBack<SongPlayJson> {
            override fun callback(data: SongPlayJson) {
                data.data?.get(0)?.url?.let{
                    // 创建下载请求
                    val request = DownloadManager.Request(Uri.parse(it))
                    // 下载中和下载完后都显示通知栏
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    val name = "${song.getArtistsString()} - ${song.name}.mp3"
                    // 使用系统默认的下载路径
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name)
                    // 通知栏标题
                    request.setTitle(name)
                    // 通知栏描述信息
                    request.setDescription("正在下载: $name")
                    // 设置MIME类型
                    request.setMimeType("audio/mpeg")
                    MainApplication.downloadManager.enqueue(request)
                } ?: error("无法获取到下载链接")
            }

            override fun error(errorMsg: String) {
                LogUtil.printToConsole(errorMsg)
            }
        })

    }

    private fun Song.getArtistsString(): String {
        return this.artists?.joinToString(separator = "、") {
            it.name ?: ""
        } ?: "未知"
    }

}