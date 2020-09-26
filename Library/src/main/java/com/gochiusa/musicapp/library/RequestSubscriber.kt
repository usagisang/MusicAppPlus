package com.gochiusa.musicapp.library

import com.example.repository.RequestCallBack
import rx.Observer

class RequestSubscriber<T>(private val requestCallBack: RequestCallBack<T>) : Observer<T> {

    override fun onError(e: Throwable?) {
        requestCallBack.error(e.toString())
    }

    override fun onNext(t: T) {
        requestCallBack.callback(t)
    }

    override fun onCompleted() {}
}