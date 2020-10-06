package com.gochiusa.musicapp.plus.entity


data class Song(val id: Long, val name: String?, val albumId: Long, val albumName: String?,
                val albumPicUrl: String?, val artists: List<Artist>)