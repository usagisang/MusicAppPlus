package com.example.repository.bean

/**
 * 获取歌曲详情，包括专辑图片等(无音乐url)：baseUrl/song/detail?ids=191528,191527
 * Gson解析的bean类.
 * */
class SongDetailJson {
    var songs: MutableList<Song>? = null

    class Song {
        var name: String? = null
        var id: Long? = null

        /**
         * 专辑.
         * */
        var al: Album? = null

        /**
         * 艺术家，歌手.
         * */
        var ar: MutableList<Artist>? = null

        /**
         * 专辑的信息.
         * */
        class Album {
            var id: Long? = null
            var name: String? = null
            var picUrl: String? = null
        }

        /**
         * 对应字段ar，艺术家/歌手.
         * */
        class Artist {
            var id: Long? = null
            var name: String? = null
        }
    }
}