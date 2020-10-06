package com.gochiusa.musicapp.library.impl

import okhttp3.Cookie
import okhttp3.HttpUrl

interface CookieStore {

    /**
     *  将一个指定的Cookie与服务器地址关联
     */
    fun add(httpUrl: HttpUrl, cookie: Cookie)

    /**
     *  重新将一组指定的Cookie与服务器地址关联
     */
    fun set(httpUrl: HttpUrl, cookies: MutableList<Cookie>)

    /**
     *  获取指定服务器地址的所有的Cookie
     */
    fun get(httpUrl: HttpUrl): MutableList<Cookie>

    /**
     *  获取被管理的所有Cookie
     */
    fun getAllCookies(): MutableList<Cookie>

    /**
     *  移除指定服务器地址下的一个Cookie
     */
    fun remove(httpUrl: HttpUrl, cookie: Cookie)

    /**
     * 移除所有Cookie
     */
    fun removeAllCookies()

}