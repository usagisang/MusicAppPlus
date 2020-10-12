package com.gochiusa.musicapp.plus.application

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.view.inputmethod.InputMethodManager
import com.gochiusa.musicapp.library.util.ContextProvider
import com.squareup.picasso.Picasso

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        ContextProvider.context = applicationContext
        Picasso.setSingletonInstance(Picasso.Builder(this).build())
        inputMethodManager = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager
        downloadManager = applicationContext.getSystemService(Context.DOWNLOAD_SERVICE)
                as DownloadManager
    }


    companion object {
        lateinit var inputMethodManager: InputMethodManager
            private set
        lateinit var downloadManager: DownloadManager
            private set
    }
}