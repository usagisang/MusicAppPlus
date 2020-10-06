package com.gochiusa.musicapp.plus.base

import java.lang.ref.WeakReference

open class BasePresenterImpl<V : BaseView> (view: V) : BasePresenter {

    // 保存对View的弱引用
    private var viewReference: WeakReference<V>? = WeakReference(view)

    fun attachView(view: V) {
        viewReference = WeakReference(view)
    }

    val view: V?
        get() = viewReference?.get()

    override fun isViewAttach(): Boolean {
        return viewReference?.get() != null
    }

    override fun removeAttach() {
        viewReference?.clear()
        viewReference = null
    }
}