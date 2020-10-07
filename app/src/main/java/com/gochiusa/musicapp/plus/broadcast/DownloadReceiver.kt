package com.gochiusa.musicapp.plus.broadcast

import android.app.DownloadManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import com.gochiusa.musicapp.library.util.ContextProvider
import com.gochiusa.musicapp.plus.application.MainApplication
import com.gochiusa.musicapp.plus.util.StringContract
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class DownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            // 如果已经下载完成，尝试获取Uri
            val apkUri = MainApplication.downloadManager.getUriForDownloadedFile(
                intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1))

            ContextProvider.context.contentResolver.openInputStream(apkUri)?.let {
                installPackage(ContextProvider.context, StringContract.INSTALL_SESSION_NAME,
                    StringContract.PACKAGE_NAME, it)
            }
        }
    }

    /**
     * 安装软件包
     */
    private fun installPackage(context: Context, installSessionName: String, packageName: String,
                               apkStream: InputStream) {
        val packageInstaller = context.packageManager.packageInstaller
        // 创建会话需要的参数
        val params = SessionParams(SessionParams.MODE_FULL_INSTALL)
        params.setAppPackageName(packageName)
        val sessionId = packageInstaller.createSession(params)
        // 创建会话
        val session: PackageInstaller.Session = packageInstaller.openSession(sessionId)
        // 打开输出流
        val out: OutputStream = session.openWrite(
            installSessionName,
            0, -1
        )
        val buffer = ByteArray(1024)
        // 将数据写入到输出流
        while (apkStream.read(buffer) > 0) {
            out.write(buffer, 0, buffer.size)
        }
        // 确保给定流的所有未完成数据已提交到磁盘
        session.fsync(out)
        out.close()
        // 尝试提交广播发送
        val intent = Intent(Intent.ACTION_PACKAGE_ADDED)
        session.commit(
            PendingIntent.getBroadcast(
                context, sessionId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            ).intentSender
        )
        session.close()
    }
}
