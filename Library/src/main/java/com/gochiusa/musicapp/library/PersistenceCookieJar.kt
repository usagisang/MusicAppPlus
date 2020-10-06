package com.gochiusa.musicapp.library

import com.gochiusa.musicapp.library.impl.CookieStore
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class PersistenceCookieJar(val cookieStore: CookieStore): CookieJar {

    override fun saveFromResponse(url: HttpUrl, cookies: MutableList<Cookie>) {
        cookieStore.set(url, cookies)
    }

    override fun loadForRequest(url: HttpUrl): MutableList<Cookie> = cookieStore.get(url)

    fun clearCookies() {
        cookieStore.removeAllCookies()
    }
}