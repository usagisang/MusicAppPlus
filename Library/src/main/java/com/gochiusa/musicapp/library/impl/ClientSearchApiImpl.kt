package com.gochiusa.musicapp.library.impl

import com.gochiusa.musicapp.library.util.DataUtil
import com.example.repository.RequestCallBack
import com.example.repository.api.ClientSearchApi
import com.example.repository.bean.SearchDefaultJson
import com.example.repository.bean.SearchSongJson
import com.gochiusa.musicapp.library.util.defaultSubscribe

class ClientSearchApiImpl: ClientSearchApi {

    private val searchApi = DataUtil.retrofit.create(SearchApi::class.java)

    override fun getSearchSongs(
        limit: Int,
        offset: Int,
        type: Int,
        keyword: String,
        callBack: RequestCallBack<SearchSongJson>
    ) {
        defaultSubscribe(searchApi.getSearchSongs(limit, offset, type, keyword), callBack)
    }

    override fun getDefaultKeywords(callBack: RequestCallBack<SearchDefaultJson>) {
        defaultSubscribe(searchApi.getDefaultKeywords(), callBack)
    }
}