package com.gochiusa.musicapp.plus.application

import android.app.Application
import android.content.Context
import android.view.inputmethod.InputMethodManager

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager
    }

    companion object {
        lateinit var context: Context
            private set

        var inputMethodManager: InputMethodManager? = null
            private set
    }
}