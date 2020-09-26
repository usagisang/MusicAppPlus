package com.example.repository

interface RequestCallBack<T>{
    fun callback(data : T)
    
    fun error(errorMsg : String)
}