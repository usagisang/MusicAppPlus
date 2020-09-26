package com.gochiusa.musicapp.library.util

import com.example.repository.RequestCallBack
import com.gochiusa.musicapp.library.RequestSubscriber
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

fun <T> defaultSubscribe(observable: Observable<T>, callback: RequestCallBack<T>) {
    observable.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread()).subscribe(RequestSubscriber(callback))
}