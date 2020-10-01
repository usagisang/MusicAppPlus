package com.gochiusa.musicapp.plus.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager


/**
 * 一个简单的全局碎片管理工具类
 */
object FragmentManageUtil {
    lateinit var fragmentManager: FragmentManager

    /**
     * 将新的碎片显示在界面上，隐藏旧的碎片，显示新的碎片，并将碎片添加到返回栈
     * @param resId 显示碎片的控件
     * @param newFragment 需要显示的新的碎片
     * @param oldFragment 需要隐藏的旧的碎片
     */
    fun addFragmentToBackStack(resId: Int, newFragment: Fragment, oldFragment: Fragment) {
        fragmentManager.beginTransaction().add(resId, newFragment).hide(oldFragment)
            .addToBackStack(null).commit()
    }
}