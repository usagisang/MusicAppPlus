package com.example.repository.bean

/**
 * 获取用户的歌单时返回的Gson.
 * 接口地址：baseUrl/user/playlist?uid=1333207903
 * */
class UserPlayListJson{
    var playlist : MutableList<PlayList>? = null
    
    class PlayList{
        var id : Long? = null
        var name : String? = null
        var coverImgUrl : String? = null
        var trackCount : Int? = null
    }
}