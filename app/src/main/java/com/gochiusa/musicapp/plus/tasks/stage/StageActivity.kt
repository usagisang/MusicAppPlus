package com.gochiusa.musicapp.plus.tasks.stage

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.repository.RequestCallBack
import com.example.repository.bean.LyricJson
import com.github.authorfu.lrcparser.parser.LyricParser
import com.gochiusa.musicapp.library.util.DataUtil
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.adapter.MusicControlAdapter
import com.gochiusa.musicapp.plus.base.PlayOrPauseClickListener
import com.gochiusa.musicapp.plus.entity.EventMessage
import com.gochiusa.musicapp.plus.entity.PlayPattern
import com.gochiusa.musicapp.plus.entity.Song
import com.gochiusa.musicapp.plus.service.IBinderInterface
import com.gochiusa.musicapp.plus.service.MusicService
import com.gochiusa.musicapp.plus.service.PlayStateListenerImpl
import com.gochiusa.musicapp.plus.service.PlayStateListenerImpl.Companion.ON_COMPLETION
import com.gochiusa.musicapp.plus.service.PlayStateListenerImpl.Companion.ON_ERROR
import com.gochiusa.musicapp.plus.service.PlayStateListenerImpl.Companion.ON_PREPARED
import com.gochiusa.musicapp.plus.service.PlayStateListenerImpl.Companion.RETURN_PREVIOUS_SONG
import com.gochiusa.musicapp.plus.service.PlayStateListenerImpl.Companion.SKIP_NEXT_SONG
import com.gochiusa.musicapp.plus.tasks.main.MainActivity.Companion.BUTTON_TURN_TO_PAUSE
import com.gochiusa.musicapp.plus.tasks.main.MainActivity.Companion.BUTTON_TURN_TO_PLAY
import com.gochiusa.musicapp.plus.util.LogUtil
import com.gochiusa.musicapp.plus.util.PlaylistManager
import com.gochiusa.musicapp.plus.util.TimeCalculator
import com.gochiusa.musicapp.plus.widget.LyricView
import com.gochiusa.musicapp.plus.widget.MusicControlView
import com.gochiusa.musicapp.plus.widget.MusicProgressBar
import com.gochiusa.musicapp.plus.widget.RoundImageView
import com.squareup.picasso.Picasso
import org.greenrobot.eventbus.EventBus
import java.io.BufferedReader
import java.io.StringReader

class StageActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var songNameTextView: TextView
    private lateinit var artistTextView: TextView
    private lateinit var roundImageView: RoundImageView
    private lateinit var musicProgressBar: MusicProgressBar
    private lateinit var musicControlView: MusicControlView
    private lateinit var lyricView: LyricView

    /**
     * 底部控制栏的适配器
     */
    private val controlAdapter = MusicControlAdapter(this)

    /**
     * Service回调监听器
     */
    private val listener = PlayStateListenerImpl(Handler(Looper.getMainLooper(), Callback()))

    private val musicServiceConnection = MusicServiceConnection()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stage)
        initChildView()
        initService()
    }

    override fun onDestroy() {
        super.onDestroy()
        musicProgressBar.cancelSeekBarAnimator()
        musicServiceConnection.binderInterface?.let {
            it.setScreenOn(false)
            it.unregisterPlayerStateListener(listener)
        }
        unbindService(musicServiceConnection)
    }

    private fun initChildView() {
        backButton = findViewById(R.id.btn_stage_back)
        songNameTextView = findViewById(R.id.tv_stage_music_name)
        artistTextView = findViewById(R.id.tv_stage_music_artist)
        roundImageView = findViewById(R.id.iv_stage_round_album)
        musicProgressBar = findViewById(R.id.widget_stage_music_progress_bar)
        musicControlView = findViewById(R.id.widget_stage_music_control)
        lyricView = findViewById(R.id.lyric_view)

        musicControlView.adapter = controlAdapter
        // 禁用按钮的点击
        controlAdapter.playOrPauseButton.isClickable = false

        initClickListener()
    }

    private fun initClickListener() {
        // 注册左上角的返回按钮的点击事件
        backButton.setOnClickListener { this.onBackPressed() }
        // 注册切换播放模式的监听器
        controlAdapter.patternButtonClickListener = object :
            MusicControlAdapter.PatternButtonClickListener {
            override fun onClick(playPattern: PlayPattern) {
                PlaylistManager.playPattern = playPattern
                musicServiceConnection.binderInterface?.setLooping(playPattern ==
                        PlayPattern.SINGLE_SONG_LOOP)
            }
        }
        // 注册切换上一首、下一首按钮的点击事件
        controlAdapter.nextSongButtonClickListener = View.OnClickListener {
            switchSong(PlaylistManager.nextSong()) }
        controlAdapter.lastSongButtonClickListener = View.OnClickListener {
           switchSong(PlaylistManager.previousSong())
        }
        // 注册播放/暂停按钮的点击事件
        controlAdapter.addPlayOrPauseButtonListener(object : PlayOrPauseClickListener {
            override fun onClick(isPause: Boolean) {
                if (isPause) {
                    // 通知服务暂停音乐
                    musicServiceConnection.binderInterface?.pauseMusic()
                    // 暂停圆形ImageView的旋转动画
                    roundImageView.pauseAnimator()
                    // 暂停进度条刷新
                    musicProgressBar.pauseSeekBarAnimator()
                    // 发送信息
                    EventBus.getDefault().post(EventMessage(BUTTON_TURN_TO_PAUSE))
                } else {
                    musicServiceConnection.binderInterface?.playMusic()
                    roundImageView.startAnimator()
                    musicProgressBar.startSeekBarAnimator(musicServiceConnection)
                    EventBus.getDefault().post(EventMessage(BUTTON_TURN_TO_PLAY))
                }

            }
        })
        // 注册专辑封面的点击事件
        roundImageView.setOnClickListener {
            it.visibility = View.GONE
            lyricView.visibility = View.VISIBLE
        }
        // 注册歌词控件的点击事件
        lyricView.setOnClickListener {
            it.visibility = View.GONE
            roundImageView.visibility = View.VISIBLE
        }
        // 注册进度条的监听接口
        musicProgressBar.setSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    lyricView.scrollToLine(TimeCalculator.getIndexWithProgress(progress,
                        lyricView.getSentenceList()))
                    musicServiceConnection.binderInterface?.progress = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                musicProgressBar.seekBarChanging = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                musicProgressBar.seekBarChanging = false
            }

        })
        // 绑定歌词控件的播放点击事件
        lyricView.onPlayClickListener = object : LyricView.OnPlayClickListener {
            override fun onPlayClick(view: LyricView, time: Long) {
                musicServiceConnection.binderInterface?.progress = time.toInt()
                musicProgressBar.setSeekBarProgress(time.toInt())
            }
        }
    }

    /**
     * 辅助方法，开启服务并绑定
     */
    private fun initService() {
        // 绑定播放音乐的服务
        Intent(this, MusicService::class.java).also {
            bindService(it, musicServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    /**
     * 刷新显示的歌曲基本信息
     */
    private fun refreshSongInformation(song: Song) {
        songNameTextView.text = song.name
        artistTextView.text = song.getAllArtistString()
        // 加载专辑图片
        Picasso.get().load(song.albumPicUrl).fit().into(roundImageView)
    }

    /**
     * 刷新控件的状态，开启或关闭一些动画等
     */
    private fun refreshChildView(song: Song, iBinderInterface: IBinderInterface, pause: Boolean) {
        controlAdapter.pause = pause
        if (iBinderInterface.prepared()) {
            // 更新进度条的显示
            musicProgressBar.setSeekBarMax(iBinderInterface.duration)
            musicProgressBar.setSeekBarProgress(iBinderInterface.progress)
            musicProgressBar.startSeekBarAnimator(musicServiceConnection)
            if (pause) {
                roundImageView.pauseAnimator()
                musicProgressBar.pauseSeekBarAnimator()
            } else {
                // 尝试启动封面旋转
                roundImageView.startAnimator()
            }
            prepareLyric(song)
        }
    }

    /**
     * 发起加载歌词请求
     */
    private fun prepareLyric(song: Song) {
        DataUtil.clientMusicApi.getSongLyric(song.id, object : RequestCallBack<LyricJson> {
            override fun callback(data: LyricJson) {
                if (data.lrc?.lyric != null ) {
                    val list = LyricParser.create(
                        BufferedReader(StringReader(data.lrc!!.lyric!!))).sentences
                    lyricView.loadingLyric = false
                    lyricView.addLyric(list)
                    // 为进度条绑定歌词控件
                    musicProgressBar.bindLyricView(lyricView)
                } else {
                    error("请求的数据为空")
                }
            }
            override fun error(errorMsg: String) {
                LogUtil.printToConsole(errorMsg)
                lyricView.loadingLyric = false
                lyricView.reset()
                lyricView.invalidate()
            }
        })
    }

    /**
     * 辅助方法，切换歌曲时应当进行的操作
     */
    private fun switchSong(song: Song?) {
        // 按钮换为暂停状态
        controlAdapter.pause = true
        // 暂时禁止按钮的点击
        controlAdapter.playOrPauseButton.isClickable = false
        lyricView.loadingLyric = true
        lyricView.reset()
        musicProgressBar.reset()
        // 停止旋转
        roundImageView.cancelAnimator()
        // 刷新显示的歌曲基本信息
        song?.let { refreshSongInformation(it) }
        musicServiceConnection.binderInterface?.prepareMusic(song)
    }

    companion object {
        // 启动时包含信息的一个键
        const val IS_MUSIC_PAUSE = "isMusicPause"

        fun getStartIntent(context: Context, pause: Boolean): Intent {
            val intent = Intent(context, StageActivity::class.java)
            // 设置信息
            intent.putExtra(IS_MUSIC_PAUSE, pause)
            return intent
        }

        fun startThisActivity(context: Context, pause: Boolean) {
            context.startActivity(getStartIntent(context, pause))
        }
    }

    inner class MusicServiceConnection: ServiceConnection {
        var binderInterface: IBinderInterface? = null
        override fun onServiceDisconnected(name: ComponentName?) {
            binderInterface = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = IBinderInterface.Stub.asInterface(service)
            binderInterface = binder
            val nowSong = PlaylistManager.nowSong()
            if (nowSong != null) {
                refreshSongInformation(nowSong)
                refreshChildView(nowSong, binder, intent.getBooleanExtra(IS_MUSIC_PAUSE,
                    true))
            }
            if (binder.prepared()) {
                controlAdapter.playOrPauseButton.isClickable = true
            }
            // 注册监听器
            binder.registerPlayerStateListener(listener)
            // 设置循环模式
            binder.setLooping(PlaylistManager.playPattern == PlayPattern.SINGLE_SONG_LOOP)
            binder.setScreenOn(true)
        }
    }

    private inner class Callback: Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            when (msg.what) {
                ON_PREPARED -> {
                    musicServiceConnection.binderInterface?.let {
                        it.playMusic()
                        PlaylistManager.nowSong()?.let {
                                song -> refreshChildView(song, it, false)
                        }
                    }
                    // 允许点击播放按钮
                    controlAdapter.playOrPauseButton.isClickable = true
                }
                ON_ERROR -> {
                    controlAdapter.pause = true
                    lyricView.loadingLyric = false
                    lyricView.reset()
                    // 禁止点击播放按钮
                    controlAdapter.playOrPauseButton.isClickable = false
                }
                ON_COMPLETION, SKIP_NEXT_SONG -> {
                    switchSong(PlaylistManager.nextSong())
                }
                RETURN_PREVIOUS_SONG -> {
                    switchSong(PlaylistManager.previousSong())
                }
            }
            return true
        }

    }
}