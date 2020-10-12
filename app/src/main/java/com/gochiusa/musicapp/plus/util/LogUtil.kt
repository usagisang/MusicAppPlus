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

    fun printError(e: Exception) {
        val elements = e.stackTrace
        Log.d("this", "错误原因" + e.message)
        Log.d("this", "错误有" + elements.size + "行")
        for (i in elements.indices) {
            Log.d(
                "this", "at " + elements[i].className
                        + "." + elements[i].methodName + "  " +
                        elements[i].lineNumber + "行"
            )
        }
    }
}