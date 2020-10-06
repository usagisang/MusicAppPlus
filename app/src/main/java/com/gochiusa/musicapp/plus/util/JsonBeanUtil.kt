package com.gochiusa.musicapp.plus.util

import com.example.repository.bean.SearchSongJson
import com.example.repository.bean.SongDetailJson
import com.gochiusa.musicapp.plus.entity.Artist
import com.gochiusa.musicapp.plus.entity.Song
import java.lang.StringBuilder


fun SongDetailJson.toSongList(): List<Song> {
    val songList = mutableListOf<Song>()
    this.songs?.let {
        for(rawSong: SongDetailJson.Song in it) {
            val artistList = mutableListOf<Artist>()
            rawSong.ar?.let {ar ->
                for (artist in ar) {
                    artistList.add(Artist(artist.id ?: 0, artist.name ?: ""))
                }
            }
            songList.add(Song(rawSong.id ?: 0, rawSong.name,
                rawSong.al?.id ?: 0, rawSong.al?.name, rawSong.al?.picUrl, artistList))
        }
    }
    return songList
}

fun toSongIdsParam(result: SearchSongJson.Result): String {
    val builder = StringBuilder()
    result.songs?.let {
        val sign = ","
        for (index in it.indices) {
            if (index < it.size - 1) {
                builder.append(it[index].id)
                builder.append(sign)
            } else {
                builder.append(it[index].id)
            }
        }
    }
    return builder.toString()
}