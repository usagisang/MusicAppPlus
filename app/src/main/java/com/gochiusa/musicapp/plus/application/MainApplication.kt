package com.gochiusa.musicapp.plus.application

import android.app.Application
import android.content.Context
import android.view.inputmethod.InputMethodManager
import com.gochiusa.musicapp.library.util.ContextProvider

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        ContextProvider.context = applicationContext
        inputMethodManager = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager
    }

    companion object {
        lateinit var inputMethodManager: InputMethodManager
            private set
    }
}