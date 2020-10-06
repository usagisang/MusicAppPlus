package com.gochiusa.musicapp.plus.tasks.main.search

import com.gochiusa.musicapp.plus.base.BasePresenter
import com.gochiusa.musicapp.plus.base.BaseView
import com.gochiusa.musicapp.plus.entity.Song

interface SearchContract {
    interface View: BaseView {
        /**
         * 默认搜索词获取成功后被回调
         */
        fun setDefaultKeyword(defaultKeyword: String)

        /**
         * 搜索数据获取成功后被回调
         */
        fun searchCallback(songList: List<Song>)

        /**
         * 获取更多搜索信息失败后被回调
         * @param hasMore 是否还有下一页的数据
         */
        fun searchFailure(hasMore: Boolean)

        /**
         *  通知View移除正在显示的所有歌曲的信息
         */
        fun removeAllSongs()
    }
    interface Presenter: BasePresenter {
        /**
         * 获取默认搜索关键词
         */
        fun getDefaultKeyword()

        /**
         * 提交一次新的搜索请求
         */
        fun submitNewSearch(keyword: String)

        fun showMore()
        fun refresh()
    }
}