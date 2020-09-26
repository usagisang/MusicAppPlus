package com.example.repository.bean

/**
 * 获取音乐url、码率等信息的接口:baseUrl/song/url?id=191528,191527
 * Gson解析的bean类.
 * */
class SongPlayJson {
    var data: MutableList<Data>? = null
    var code: Int? = null

    class Data {
        var id: Long? = null
        var url: String? = null
        var size: Long? = null
        var type: String? = null

        /**
         * 码率.
         * */
        var br: Long? = null
    }
}