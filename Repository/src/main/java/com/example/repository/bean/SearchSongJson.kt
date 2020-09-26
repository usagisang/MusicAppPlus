package com.example.repository.bean

/**
 * baseUrl/search?keywords= 富士山下
 * 接口：获取搜索的关键词的信息.
 * */
class SearchSongJson {
    var result: Result? = null

    class Result {
        var songs: MutableList<Song>? = null

        class Song {
            var id: Long? = null
            var name: String? = null
            var artists: MutableList<Artist>? = null

            class Artist {
                var id: Long? = null
                var name: String? = null
            }
        }
    }
}