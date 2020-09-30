package com.gochiusa.musicapp.plus.base

import android.widget.Toast
import androidx.fragment.app.Fragment

/**
 * 只有需要自行控制查询数据行为的碎片才需要继承这个基类
 */
abstract class BaseFragment<P : BasePresenter> : Fragment(), BaseView {
    private lateinit var backgroundPresenter: P

    val presenter: P
        get() {
            if (! this::backgroundPresenter.isInitialized) {
                backgroundPresenter = onBindPresenter()
            }
            return backgroundPresenter
        }

    /**
     * 交由子类实现如何获得Presenter的方法
     * @return 对应的Presenter
     */
    protected abstract fun onBindPresenter(): P

    /**
     * 活动销毁后取消掉与Presenter的绑定
     */
    override fun onDestroy() {
        super.onDestroy()
        presenter.removeAttach()
    }

    override fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}