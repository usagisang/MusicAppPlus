package com.example.repository.bean

/**
 * baseUrl/playlist/detail?id=2564780673
 * 获取歌单中的歌曲的Id
 * */
class SongIdsJson() {
    var playlist : PlayList? = null
    
    class PlayList(){
        var trackIds : MutableList<TrackId>? = null
        
        class TrackId(){
            var id : Long? = null
        }
    }
}