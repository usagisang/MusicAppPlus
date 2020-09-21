package com.gochiusa.musicapp.plus.base

/**
 *  当播放按钮被点击之后会被回调的接口
 */
interface PlayOrPauseClickListener {
    /**
     * 播放按钮被点击后回调
     * @param isPause 播放按钮当前的显示状态，false表示显示为暂停键（处于播放状态），true则相反
     *                注意回调时状态已经切换完毕，如果正在播放音乐，{@code isPause}为false，
     *                点击之后，{@code isPause}改变为true（变为暂停状态）并按顺序回调各个接口
     */
    fun onClick(isPause: Boolean)
}