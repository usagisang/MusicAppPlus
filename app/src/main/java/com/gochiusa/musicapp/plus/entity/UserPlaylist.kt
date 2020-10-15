package com.gochiusa.musicapp.plus.entity

import com.example.repository.bean.UserPlayListJson

data class UserPlaylist(val id: Long?, val name: String?,
                        val coverImgUrl: String?, val songCount: Int?) {
    constructor(playlist: UserPlayListJson.PlayList?): this(
        playlist?.id, playlist?.name, playlist?.coverImgUrl, playlist?.trackCount)
}