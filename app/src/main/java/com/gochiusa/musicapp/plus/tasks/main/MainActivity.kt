package com.gochiusa.musicapp.plus.tasks.main

import android.content.res.Resources
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.util.FragmentManageUtil
import com.gochiusa.musicapp.plus.widget.BottomMusicWidget


class MainActivity : AppCompatActivity() {

    private lateinit var bottomMusicWidget: BottomMusicWidget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initChildView()
        // 初始化碎片管理工具类
        FragmentManageUtil.fragmentManager = supportFragmentManager
        // 将主碎片切换上去
        FragmentManageUtil.fragmentManager.beginTransaction()
            .add(R.id.frame_layout_main_page, MainPageFragment()).commit()
    }

    private fun initChildView() {
        bottomMusicWidget = findViewById(R.id.widget_bottom_music)
    }
}