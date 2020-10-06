package com.gochiusa.musicapp.library.impl

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Cookie
import okhttp3.HttpUrl

class CookieStoreImpl(val context: Context): CookieStore {

    /**
     *  缓存在内存的Cookie
     */
    private val memoryCookies: MutableMap<String, MutableList<Cookie>> = mutableMapOf()

    /**
     * 空的Cookie表
     */
    private val emptyCookieList = mutableListOf<Cookie>()

    init {
        readCookieFromFile()
    }

    override fun add(httpUrl: HttpUrl, cookie: Cookie) {
        // 忽略失效的Cookie
        if (isCookieExpired(cookie)) {
            return
        }
        val hostString = getHost(httpUrl)
        // 如果还不存在这个键
        if (! memoryCookies.containsKey(hostString)) {
            memoryCookies[getHost(httpUrl)] = mutableListOf()
        }
        memoryCookies[hostString]?.add(cookie)
        writeAllCookieToFile()
    }

    override fun set(httpUrl: HttpUrl, cookies: MutableList<Cookie>) {
        memoryCookies[getHost(httpUrl)] = cookies
        writeAllCookieToFile()
    }

    override fun get(httpUrl: HttpUrl): MutableList<Cookie> =
        memoryCookies[getHost(httpUrl)] ?: emptyCookieList

    /**
     *  获取所有的Cookie，如果不管理任何Cookie，则返回空表
     */
    override fun getAllCookies(): MutableList<Cookie> {
        return if (memoryCookies.isEmpty()) {
            emptyCookieList
        } else {
            val allCookieList = mutableListOf<Cookie>()
            for (cookieList: MutableList<Cookie> in memoryCookies.values) {
                allCookieList.addAll(cookieList)
            }
            allCookieList
        }
    }

    override fun remove(httpUrl: HttpUrl, cookie: Cookie) {
        memoryCookies[getHost(httpUrl)]?.remove(cookie)
        writeAllCookieToFile()
    }

    override fun removeAllCookies() {
        memoryCookies.clear()
        writeAllCookieToFile()
    }

    /**
     *  将当前管理的所有Cookie写入文件
     */
    private fun writeAllCookieToFile() {
        var editor: SharedPreferences.Editor
        // 如果列表为空
        if (memoryCookies.isEmpty()) {
            editor = context.getSharedPreferences(
                FILE_NAME_PREFIX + 0, Context.MODE_PRIVATE).edit()
            editor.putBoolean(HAS_NEXT_COOKIE_KEY, false)
            editor.putString(HOST_KEY, null)
            editor.apply()
        } else {
            // 文件后缀
            var fileIndex = 0
            // Cookie列表总大小
            val totalCount = getAllCookies().size
            for (key: String in memoryCookies.keys) {
                // 获取键对应的Cookie列表
                val cookieList = memoryCookies[key] ?: continue
                for (cookie: Cookie in cookieList) {
                    editor = context.getSharedPreferences(
                        FILE_NAME_PREFIX + fileIndex, Context.MODE_PRIVATE).edit()
                    // 是否还有下一个数据
                    editor.putBoolean(HAS_NEXT_COOKIE_KEY, fileIndex != totalCount - 1)
                    editor.putString(HOST_KEY, key)

                    editor.putString(COOKIE_NAME_KEY, cookie.name())
                    editor.putString(COOKIE_VALUE_KEY, cookie.value())
                    editor.putLong(EXPIRES_AT_KEY, cookie.expiresAt())
                    editor.putString(DOMAIN_KEY, cookie.domain())
                    editor.putString(PATH_KEY, cookie.path())
                    editor.putBoolean(SECURE_KEY, cookie.secure())
                    editor.putBoolean(HTTP_ONLY_KEY, cookie.httpOnly())
                    editor.putBoolean(HOST_ONLY_KEY, cookie.hostOnly())

                    editor.apply()
                    fileIndex ++
                }
            }
        }
    }

    /**
     * 判断cookie是否失效
     * @return 若返回true代表该Cookie失效
     */
    private fun isCookieExpired(cookie: Cookie): Boolean {
        return cookie.expiresAt() < System.currentTimeMillis()
    }

    /**
     *  辅助方法，将{@code HttpUrl}映射为String
     */
    private fun getHost(httpUrl: HttpUrl): String = httpUrl.host()


    /**
     *  从持久化数据源中获取数据
     */
    private fun readCookieFromFile() {
        var sharedPreferences = context.getSharedPreferences(FILE_NAME_PREFIX + 0,
            Context.MODE_PRIVATE)
        // 如果没办法获取到第一个文件的HOST信息，说明不存在cookie信息，结束方法
        if (sharedPreferences.getString(HOST_KEY, null) == null) {
            return
        }
        // 文件后缀
        var i = 0
        // Cookie构造器
        val builder: Cookie.Builder = Cookie.Builder()
        var hostString: String
        do {
            sharedPreferences = context.getSharedPreferences(FILE_NAME_PREFIX + i,
                Context.MODE_PRIVATE)
            // 获取Host信息
            hostString = sharedPreferences.getString(HOST_KEY, DEFAULT_HOST)!!

            builder.name(sharedPreferences.getString(COOKIE_NAME_KEY, DEFAULT_NAME)!!)
            builder.value(sharedPreferences.getString(COOKIE_VALUE_KEY, DEFAULT_VALUE)!!)
            builder.expiresAt(sharedPreferences.getLong(EXPIRES_AT_KEY, 0L))
            builder.path(sharedPreferences.getString(PATH_KEY, DEFAULT_PATH)!!)
            if (sharedPreferences.getBoolean(SECURE_KEY, false)) {
                builder.secure()
            }
            if (sharedPreferences.getBoolean(HTTP_ONLY_KEY, false)) {
                builder.httpOnly()
            }
            if (sharedPreferences.getBoolean(HOST_ONLY_KEY, false)) {
                builder.hostOnlyDomain(sharedPreferences.getString(DOMAIN_KEY, DEFAULT_DOMAIN)!!)
            } else {
                builder.domain(sharedPreferences.getString(DOMAIN_KEY, DEFAULT_DOMAIN)!!)
            }

            // 如果这个域名的列表尚不存在，则创建列表
            if (memoryCookies[hostString] == null) {
                memoryCookies[hostString] = mutableListOf()
            }
            memoryCookies[hostString]?.add(builder.build())
            // 递增后缀
            i++
        } while (sharedPreferences.getBoolean(HAS_NEXT_COOKIE_KEY, false))
    }

    companion object {
        /**
         *  持久化数据的文件名前缀
         */
        private const val FILE_NAME_PREFIX = "COOKIE_"

        // 以下是储存在文件中各种信息的键
        /**
         *  是否存在下一个Cookie
         */
        private const val HAS_NEXT_COOKIE_KEY = "hasNextCookie"

        /**
         * cookie对应的HOST
         */
        private const val HOST_KEY = "host"

        /**
         *  cookie名字
         */
        private const val COOKIE_NAME_KEY = "cookieName"

        /**
         * cookie的值
         */
        private const val COOKIE_VALUE_KEY = "cookieValue"

        private const val EXPIRES_AT_KEY = "expiresAt"
        private const val DOMAIN_KEY = "domain"
        private const val PATH_KEY = "path"
        private const val SECURE_KEY = "secure"
        private const val HTTP_ONLY_KEY = "httpOnly"
        private const val HOST_ONLY_KEY = "hostOnly"

        /**
         * 默认的cookie名字
         */
        private const val DEFAULT_NAME = "error"

        /**
         *  默认的cookie值
         */
        private const val DEFAULT_VALUE = "error"
        /**
         * 默认的Path路径
         */
        private const val DEFAULT_PATH = "/"
        /**
         * 默认的主机Host
         */
        private const val DEFAULT_DOMAIN = "127.0.0.1"

        private const val DEFAULT_HOST = "gochiusa.top"
    }
}