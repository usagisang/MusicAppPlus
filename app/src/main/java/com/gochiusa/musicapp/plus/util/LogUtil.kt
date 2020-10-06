package com.gochiusa.musicapp.plus.util

import android.util.Log

object LogUtil {

    private const val LOG_TAG = "this"

    fun printToConsole(message: String) {
        Log.d(LOG_TAG, message)
    }

    fun printToConsole(message: Any) {
        printToConsole(message.toString())
    }
}