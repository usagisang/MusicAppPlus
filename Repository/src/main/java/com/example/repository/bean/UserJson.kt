package com.example.repository.bean

/**
 * User的GsonBean类
 * 接口地址：baseUrl/login/cellphone?phone=XXX&password=XXX
 * */
class UserJson {
    var profile : Profile? = null
    
    class Profile{
        var avatarUrl : String? = null
        var nickname : String? = null
        var userId : Long? = null
    }
}