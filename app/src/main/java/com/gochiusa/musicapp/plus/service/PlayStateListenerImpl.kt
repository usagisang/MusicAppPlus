package com.gochiusa.musicapp.plus.service

import android.os.Handler
import android.os.Message

class PlayStateListenerImpl(private val handler: Handler): IPlayerStateListener.Stub() {

    override fun onPrepared() {
        handler.sendMessage(Message.obtain(handler, ON_PREPARED))
    }

    override fun onCompletion() {
        handler.sendMessage(Message.obtain(handler, ON_COMPLETION))
    }

    override fun onError(errorCode: Int) {
        handler.sendMessage(Message.obtain(handler, ON_ERROR, errorCode))
    }

    override fun returnPreviousSong() {
       handler.sendMessage(Message.obtain(handler, RETURN_PREVIOUS_SONG))
    }

    override fun skipNextSong() {
        handler.sendMessage(Message.obtain(handler, SKIP_NEXT_SONG))
    }

    override fun stopSelf() {
        handler.sendMessage(Message.obtain(handler, STOP_SELF))
    }

    companion object {
        const val ON_PREPARED = 1
        const val ON_COMPLETION = 2
        const val ON_ERROR = 3
        const val RETURN_PREVIOUS_SONG = 4
        const val SKIP_NEXT_SONG = 5
        const val STOP_SELF = 6
    }
}