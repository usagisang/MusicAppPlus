package com.gochiusa.musicapp.plus.base

interface BasePresenter {
    /**
     * View是否与Presenter保持连接
     * @return 如果View保持连接返回true,否则返回false
     */
    fun isViewAttach(): Boolean

    /**
     * 移除View和Presenter的连接
     */
    fun removeAttach()
}