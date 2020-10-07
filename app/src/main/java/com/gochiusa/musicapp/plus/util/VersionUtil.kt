package com.gochiusa.musicapp.plus.util

import android.os.Build
import com.gochiusa.musicapp.library.util.ContextProvider

object VersionUtil {
    fun getVersionCode(): Int {
        val info = ContextProvider.context.packageManager.getPackageInfo(
                ContextProvider.context.packageName, 0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode.toInt()
        } else {
            info.versionCode
        }
    }

    fun getVersionName(): String {
        return ContextProvider.context.packageManager.getPackageInfo(
            ContextProvider.context.packageName, 0
        ).versionName
    }
}