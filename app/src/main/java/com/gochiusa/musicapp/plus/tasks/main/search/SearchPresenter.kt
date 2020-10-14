package com.gochiusa.musicapp.plus.tasks.main.search

import com.example.repository.RequestCallBack
import com.example.repository.bean.SearchDefaultJson
import com.example.repository.bean.SearchSongJson
import com.example.repository.bean.SongDetailJson
import com.gochiusa.musicapp.library.util.DataUtil
import com.gochiusa.musicapp.plus.base.BasePresenterImpl
import com.gochiusa.musicapp.plus.util.LogUtil
import com.gochiusa.musicapp.plus.util.OffsetCalculator
import com.gochiusa.musicapp.plus.util.toSongIdsParam
import com.gochiusa.musicapp.plus.util.toSongList

class SearchPresenter(view: SearchContract.View):
    BasePresenterImpl<SearchContract.View>(view), SearchContract.Presenter {

    private val offsetCalculator = OffsetCalculator(0, DEFAULT_PAGE_LIMIT, 0)

    private var lastKeyword: String = ""


    override fun getDefaultKeyword() {
        DataUtil.clientSearchApi.getDefaultKeywords(object: RequestCallBack<SearchDefaultJson> {
            override fun callback(data: SearchDefaultJson) {
                if (data.data != null) {
                    view?.setDefaultKeyword(data.data!!.realkeyword ?: "")
                } else {
                    LogUtil.printToConsole("Data is null.")
                }
            }
            override fun error(errorMsg: String) {
                LogUtil.printToConsole(errorMsg)
            }

        })
    }

    override fun showMore() {
        // 如果已经不能再递增偏移量
        if (! offsetCalculator.increaseOffset()) {
            // 没有下一页的数据了
            view?.searchFailure(false)
            return
        }
        DataUtil.clientSearchApi.getSearchSongs(offsetCalculator.pageLimit, offsetCalculator.offset,
            1, lastKeyword, object : RequestCallBack<SearchSongJson> {
                override fun callback(data: SearchSongJson) {
                    data.result?.let { result ->
                        // 进行获取歌曲详细信息的步骤
                        DataUtil.clientMusicApi.getSongsDetail(toSongIdsParam(result), object :
                            RequestCallBack<SongDetailJson> {
                            override fun callback(data: SongDetailJson) {
                                view?.searchCallback(data.toSongList())
                            }

                            override fun error(errorMsg: String) {
                                defaultError(errorMsg)
                                // 重置偏移量
                                offsetCalculator.offset -= offsetCalculator.pageLimit
                            }
                        })
                    }
                }
                override fun error(errorMsg: String) {
                    defaultError(errorMsg)
                    // 重置偏移量
                    offsetCalculator.offset -= offsetCalculator.pageLimit
                }
            })
    }

    override fun refresh() {
        if (lastKeyword.isEmpty()) {
            view?.searchFailure(false)
            return
        }
        offsetCalculator.offset = 0
        requestSongDetail()
    }

    override fun submitNewSearch(keyword: String) {
        // 重设偏移量和总数
        offsetCalculator.offset = 0
        offsetCalculator.totalCount = 0
        // 缓存搜索关键字
        lastKeyword = keyword
        requestSongDetail()
    }

    /**
     * 请求歌曲信息的完整过程
     */
    private fun requestSongDetail() {
        DataUtil.clientSearchApi.getSearchSongs(offsetCalculator.pageLimit, offsetCalculator.offset,
            1, lastKeyword, object : RequestCallBack<SearchSongJson> {
                override fun callback(data: SearchSongJson) {
                    data.result?.let {
                        offsetCalculator.totalCount = it.songCount ?: 0
                        // 进行获取歌曲详细信息的步骤
                        DataUtil.clientMusicApi.getSongsDetail(toSongIdsParam(it), object :
                            RequestCallBack<SongDetailJson> {
                            override fun callback(data: SongDetailJson) {
                                // 移除正在显示的所有歌曲
                                view?.removeAllSongs()
                                view?.searchCallback(data.toSongList())
                            }

                            override fun error(errorMsg: String) {
                                defaultError(errorMsg)
                            }
                        })
                    }

                }
                override fun error(errorMsg: String) {
                    defaultError(errorMsg)
                }
            })
    }


    /**
     *  默认的异常操作
     */
    private fun defaultError(errorMsg: String) {
        LogUtil.printToConsole(errorMsg)
        view?.let {
            it.showToast(DEFAULT_ERROR_TIP)
            it.searchFailure(true)
        }
    }

    companion object {
        private const val DEFAULT_PAGE_LIMIT = 20
        private const val DEFAULT_ERROR_TIP = "加载失败"
    }
}