package com.gochiusa.musicapp.plus.tasks.main

import android.app.DownloadManager
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.broadcast.DownloadReceiver
import com.gochiusa.musicapp.plus.tasks.stage.StageActivity
import com.gochiusa.musicapp.plus.util.FragmentManageUtil
import com.gochiusa.musicapp.plus.widget.BottomMusicWidget


class MainActivity : AppCompatActivity() {

    private lateinit var bottomMusicWidget: BottomMusicWidget
    private lateinit var downloadReceiver: DownloadReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initChildView()
        // 初始化碎片管理工具类
        FragmentManageUtil.fragmentManager = supportFragmentManager
        // 将主碎片切换上去
        FragmentManageUtil.fragmentManager.beginTransaction()
            .add(R.id.frame_layout_main_page, MainPageFragment()).commit()
        registerBroadcast()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(downloadReceiver)
    }

    private fun initChildView() {
        bottomMusicWidget = findViewById(R.id.widget_bottom_music)
        bottomMusicWidget.setOnClickListener {
            StageActivity.startThisActivity(this, bottomMusicWidget.pause)
        }
    }

    /**
     * 注册广播
     */
    private fun registerBroadcast() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        downloadReceiver = DownloadReceiver()
        registerReceiver(downloadReceiver, intentFilter)
    }
}