package com.gochiusa.musicapp.plus.tasks.main

import android.app.ActivityManager
import android.app.DownloadManager
import android.content.*
import android.os.*
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.adapter.PlaylistAdapter
import com.gochiusa.musicapp.plus.base.PlayOrPauseClickListener
import com.gochiusa.musicapp.plus.broadcast.DownloadReceiver
import com.gochiusa.musicapp.plus.entity.EventMessage
import com.gochiusa.musicapp.plus.entity.Song
import com.gochiusa.musicapp.plus.service.IBinderInterface
import com.gochiusa.musicapp.plus.service.MusicService
import com.gochiusa.musicapp.plus.service.PlayStateListenerImpl
import com.gochiusa.musicapp.plus.service.PlayStateListenerImpl.Companion.ON_COMPLETION
import com.gochiusa.musicapp.plus.service.PlayStateListenerImpl.Companion.ON_ERROR
import com.gochiusa.musicapp.plus.service.PlayStateListenerImpl.Companion.ON_PREPARED
import com.gochiusa.musicapp.plus.service.PlayStateListenerImpl.Companion.RETURN_PREVIOUS_SONG
import com.gochiusa.musicapp.plus.service.PlayStateListenerImpl.Companion.SKIP_NEXT_SONG
import com.gochiusa.musicapp.plus.service.PlayStateListenerImpl.Companion.STOP_SELF
import com.gochiusa.musicapp.plus.tasks.stage.StageActivity
import com.gochiusa.musicapp.plus.util.FragmentManageUtil
import com.gochiusa.musicapp.plus.util.LogUtil
import com.gochiusa.musicapp.plus.util.PlaylistManager
import com.gochiusa.musicapp.plus.util.WidgetUtil
import com.gochiusa.musicapp.plus.widget.BottomMusicWidget
import com.gochiusa.musicapp.plus.widget.PopupPlaylist
import com.squareup.picasso.Picasso
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private lateinit var bottomMusicWidget: BottomMusicWidget

    private lateinit var playlistView: View

    private lateinit var downloadReceiver: DownloadReceiver

    private val musicServiceConnection = MusicServiceConnection()
    private lateinit var popupPlaylist: PopupPlaylist

    private val listener = PlayStateListenerImpl(Handler(Looper.getMainLooper(), Callback()))


    /**
     * 标识与服务断开之后是否需要重连
     */
    private var needReConnect = true
    /**
     * 标识活动是否可见
     */
    private var foreground = false

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
        initService()
        EventBus.getDefault().register(this)
    }

    override fun onResume() {
        super.onResume()
        foreground = true
    }

    override fun onPause() {
        super.onPause()
        foreground = false
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        needReConnect = false
        musicServiceConnection.binderInterface.unregisterPlayerStateListener(listener)
        unbindService(musicServiceConnection)
        unregisterReceiver(downloadReceiver)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 移除Fragment的数据，避免视图重叠问题
        outState.remove("android:support:fragments")
    }

    override fun onBackPressed() {
        if (popupPlaylist.isShowing) {
            popupPlaylist.dismiss()
        } else {
            super.onBackPressed()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleEvent(eventMessage: EventMessage) {
        when (eventMessage.messageCode) {
            PREPARE_MUSIC -> {
                switchSong(PlaylistManager.nowSong())
            }
            BUTTON_TURN_TO_PAUSE -> {
                bottomMusicWidget.pause = true
            }
            BUTTON_TURN_TO_PLAY -> {
                bottomMusicWidget.pause = false
            }
            PlaylistAdapter.REPLAY_NOW_SONG -> {
                resetChildView()
                refreshSongInformation(PlaylistManager.nowSong())
                // 如果没有显示在前端，不需要执行下面的逻辑
                if (! foreground) {
                    return
                }
                if (PlaylistManager.isPlayListEmpty) {
                    musicServiceConnection.binderInterface.reset()
                } else {
                    musicServiceConnection.binderInterface.prepareMusic(
                        PlaylistManager.nowSong())
                }
            }
        }
    }

    private fun initChildView() {
        bottomMusicWidget = findViewById(R.id.widget_bottom_music)
        playlistView = layoutInflater.inflate(R.layout.layout_playlist, null)
        popupPlaylist = PopupPlaylist(window, playlistView, ViewGroup.LayoutParams.MATCH_PARENT,
            WidgetUtil.dpToPx(400).toInt())
        initBottomWidget()
        // 注册播放列表的View中的清空按钮的事件
        popupPlaylist.clearButtonClickListener = View.OnClickListener {
            PlaylistManager.removeAllSong()
            resetChildView()
            refreshSongInformation(null)
            musicServiceConnection.binderInterface.reset()
        }
    }

    private fun initBottomWidget() {
        bottomMusicWidget.setOnClickListener {
            StageActivity.startThisActivity(this, bottomMusicWidget.pause)
        }
        bottomMusicWidget.addPlayOrPauseButtonListener(object: PlayOrPauseClickListener {
            override fun onClick(isPause: Boolean) {
                if (isPause) {
                    musicServiceConnection.binderInterface.pauseMusic()
                } else {
                    musicServiceConnection.binderInterface.playMusic()
                }
            }
        })
        // 先禁止按钮的点击
        bottomMusicWidget.playOrPauseButton.isClickable = false
        bottomMusicWidget.playlistButton.setOnClickListener {
            popupPlaylist.show(bottomMusicWidget, Gravity.BOTTOM, 0, 0)
        }
    }

    /**
     * 辅助方法，开启服务并绑定
     */
    private fun initService() {
        Intent(this, MusicService::class.java).also {
            bindService(it, musicServiceConnection, Context.BIND_AUTO_CREATE)
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

    /**
     * 刷新或清除显示的歌曲信息
     */
    private fun refreshSongInformation(song: Song?) {
        if (song != null) {
            // 加载专辑图片
            Picasso.get().load(song.albumPicUrl).fit().into(bottomMusicWidget.roundImageView)
            // 刷新底部栏信息
            bottomMusicWidget.artistTextView.text = song.getAllArtistString()
            bottomMusicWidget.musicNameTextView.text = song.name
        } else {
            bottomMusicWidget.roundImageView.setImageResource(R.drawable.ic_widget_album)
            // 刷新底部栏信息
            bottomMusicWidget.artistTextView.text = ""
            bottomMusicWidget.musicNameTextView.text = ""
        }
    }

    /**
     * 获取数据失败或者准备阶段无法获取数据时，重新恢复一些控件的初始状态
     */
    private fun resetChildView() {
        // 按钮换为暂停状态
        bottomMusicWidget.pause = true
        // 暂时禁止按钮的点击
        bottomMusicWidget.playOrPauseButton.isClickable = false
    }

    /**
     * 辅助方法，切换歌曲时应当进行的操作
     */
    private fun switchSong(song: Song?) {
        resetChildView()
        refreshSongInformation(song)
        if (foreground) {
            musicServiceConnection.binderInterface.prepareMusic(song)
        }
    }

    private inner class MusicServiceConnection: ServiceConnection {
        lateinit var binderInterface: IBinderInterface

        override fun onServiceDisconnected(name: ComponentName?) {
            if (needReConnect) {
                initService()
            }
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binderInterface = IBinderInterface.Stub.asInterface(service)
            binderInterface.registerPlayerStateListener(listener)
        }
    }

    private inner class Callback: Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            when (msg.what) {
                ON_PREPARED -> {
                    // 按钮可以点击
                    bottomMusicWidget.playOrPauseButton.isClickable = true
                    // 将按钮重置为播放状态
                    bottomMusicWidget.pause = false
                    if (foreground) {
                        musicServiceConnection.binderInterface.playMusic()
                    } else {
                        refreshSongInformation(PlaylistManager.nowSong())
                    }
                }
                ON_ERROR -> {
                    LogUtil.printToConsole("mediaPlayer something error")
                    when (msg.obj as Int) {
                        MusicService.PLAY_URL_REQUEST_ERROR -> {
                            Toast.makeText(this@MainActivity,
                                PLAY_URL_REQUEST_ERROR_TIP, Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(this@MainActivity,
                                OTHER_ERROR_TIP, Toast.LENGTH_SHORT).show()
                        }
                    }
                    refreshSongInformation(PlaylistManager.nowSong())
                    resetChildView()
                }
                RETURN_PREVIOUS_SONG -> {
                    switchSong(PlaylistManager.previousSong())
                }
                ON_COMPLETION, SKIP_NEXT_SONG -> {
                    switchSong(PlaylistManager.nextSong())
                }
                STOP_SELF -> {
                    // 获取任务栈
                    val appTaskList = (getSystemService(Context.ACTIVITY_SERVICE)
                            as ActivityManager).appTasks
                    // 逐个关闭Activity
                    for (appTask in appTaskList) {
                        appTask.finishAndRemoveTask()
                    }
                    // 结束进程
                    exitProcess(0)
                }
            }
            return true
        }
    }

    companion object {
        private const val PLAY_URL_REQUEST_ERROR_TIP = "因版权问题暂时无法播放"
        private const val OTHER_ERROR_TIP = "播放失败"

        const val PREPARE_MUSIC = 1
        const val BUTTON_TURN_TO_PAUSE = 2
        const val BUTTON_TURN_TO_PLAY = 3
    }
}