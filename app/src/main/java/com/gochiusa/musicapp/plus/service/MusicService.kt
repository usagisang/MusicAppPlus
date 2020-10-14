package com.gochiusa.musicapp.plus.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.widget.RemoteViews
import com.example.repository.RequestCallBack
import com.example.repository.bean.SongPlayJson
import com.gochiusa.musicapp.library.util.DataUtil
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.entity.Song
import com.gochiusa.musicapp.plus.tasks.stage.StageActivity
import com.gochiusa.musicapp.plus.util.LogUtil
import com.squareup.picasso.Picasso

class MusicService : Service() {

    /**
     * 音乐播放器
     */
    private val mediaPlayer = MediaPlayer()

    private val binder = MusicBinder()
    /**
     * 保存另一端进程的监听器接口的一个特殊集合
     */
    private val remoteCallbackList = RemoteCallbackList<IPlayerStateListener>()

    /**
     * 通知显示的界面
     */
    private lateinit var bigRemoteViews: RemoteViews
    private lateinit var smallRemoteViews: RemoteViews

    /**
     * 通知管理器
     */
    private lateinit var notificationManager: NotificationManager

    /**
     * 创建的前台通知
     */
    private lateinit var notification: Notification

    /**
     * 与主线程绑定的Handler
     */
    private val mainHandler: Handler = Handler(Looper.getMainLooper(), Callback())

    /**
     * 缓存播放地址的Map
     */
    private val playUrlMap = mutableMapOf<Long, String>()

    /**
     * 标志变量，MediaPlayer是否处于Prepared状态
     */
    private var prepared = false

    /**
     * 歌曲缓存变量
     */
    @Volatile
    private var cacheSong: Song? = null

    /**
     * 正在异步加载的歌曲
     */
    @Volatile
    private var preparingSong: Song? = null


    override fun onCreate() {
        super.onCreate()
        // 发送通知
        sendNotification()
        // 初始化MediaPlayer
        initMediaPlayer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 读取Intent的意图，如果读取失败，设置意图为-1，这不会执行任何事件
        when (intent?.getIntExtra(STATUS_KEY, -1)) {
            MUSIC_PLAY_OR_PAUSE -> {
                if (prepared && ! mediaPlayer.isPlaying) {
                    binder.playMusic()
                } else {
                    binder.pauseMusic()
                }
            }
            MUSIC_PREVIOUS -> {
                listenerReturnPreviousUpdate()
            }
            MUSIC_NEXT -> {
                listenerSkipNextUpdate()
            }
            CLOSE_APP -> {
                stopForeground(true)
                listenerStopSelfUpdate()
                stopSelf()
            }
        }
        // 不需要重启服务
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放MediaPlayer
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder.asBinder()
    }

    /**
     * 发送通知，将此变为前台服务
     */
    private fun sendNotification() {
        // 获取通知管理器
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // 初始化RemoteViews
        initRemoteViews()
        val notificationBuilder: Notification.Builder
        // 分版本调用api创建Notification.Builder，如果高于O版本则创建并注册通知渠道
        notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 创建通知渠道
            val channel = NotificationChannel(CHANNEL_SERVICE_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            // 取消振动效果
            channel.enableVibration(false)
            notificationManager.createNotificationChannel(channel)
            Notification.Builder(this, CHANNEL_SERVICE_ID)
        } else {
            Notification.Builder(this)
        }
        // 设置通知的一些通用属性
        initNotification(notificationBuilder)
        // 创建通知
        notification = notificationBuilder.build()

        // 第一次将通知显示在通知栏
        startForeground(PLAY_NOTIFICATION_ID, notification)
    }

    /**
     * 初始化通知栏上显示的View，并设置上面的控件的点击事件，
     * 这些点击事件都将发送一个Intent并回调onStartCommand()
     */
    private fun initRemoteViews() {
        // 初始化通知显示的view
        smallRemoteViews = RemoteViews(packageName, R.layout.layout_service_small_notification)
        bigRemoteViews = RemoteViews(packageName, R.layout.layout_service_big_notification)
        // 点击播放/暂停按钮时发送的Intent，以下的代码类似
        smallRemoteViews.setOnClickPendingIntent(R.id.iv_service_small_play_or_pause,
            getServicePendingIntent(createServiceIntent(MUSIC_PLAY_OR_PAUSE)))
        smallRemoteViews.setOnClickPendingIntent(R.id.iv_service_small_prev,
            getServicePendingIntent(createServiceIntent(MUSIC_PREVIOUS)))
        smallRemoteViews.setOnClickPendingIntent(R.id.iv_service_small_next,
            getServicePendingIntent(createServiceIntent(MUSIC_NEXT)))
        smallRemoteViews.setOnClickPendingIntent(R.id.iv_service_small_close,
            getServicePendingIntent(createServiceIntent(CLOSE_APP)))

        bigRemoteViews.setOnClickPendingIntent(R.id.iv_service_play_or_pause,
            getServicePendingIntent(createServiceIntent(MUSIC_PLAY_OR_PAUSE)))
        bigRemoteViews.setOnClickPendingIntent(R.id.iv_service_prev,
            getServicePendingIntent(createServiceIntent(MUSIC_PREVIOUS)))
        bigRemoteViews.setOnClickPendingIntent(R.id.iv_service_next,
            getServicePendingIntent(createServiceIntent(MUSIC_NEXT)))
        bigRemoteViews.setOnClickPendingIntent(R.id.iv_service_close,
            getServicePendingIntent(createServiceIntent(CLOSE_APP)))
    }

    /**
     * 辅助方法，初始化发送的通知的属性
     */
    private fun initNotification(builder: Notification.Builder) {
        // 取消显示发送时间，并设置通知的标题和图片
        builder.setShowWhen(false)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentTitle("")
        // 分版本调用api
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setCustomBigContentView(bigRemoteViews)
                .setCustomContentView(smallRemoteViews)
        } else {
            builder.setContent(smallRemoteViews)
        }
        // 创建打开音乐播放界面的PendingIntent
        builder.setContentIntent(getActivityPendingIntent())
    }

    /**
     * 使用不同的代表了按钮的事件的常量作为Extras部分
     * 产生一个指向这个Service的Intent。这些Intent仅在Extras部分存在差异
     * @param clickItemCode 代表点击了哪个ImageView按钮的常量
     * @return 指向这个Service的Intent
     */
    private fun createServiceIntent(clickItemCode: Int): Intent {
        val intent = Intent(SERVICE_START_ACTION)
        intent.addCategory(SERVICE_START_CATEGORY)
        intent.putExtra(STATUS_KEY, clickItemCode)
        return intent
    }

    /**
     * 使用传入的Intent，生成一个PendingIntent，
     * 这个PendingIntent的上下文Context被设置为这个服务
     * @param intent 包含了PendingIntent的意图和包含的数据
     * @return 使用FLAG_UPDATE_CURRENT模式的发送广播的PendingIntent。
     * 如果传入的Intent和上一个Intent仅存在Extras上的不同，新的Intent将覆盖上一个Intent
     */
    private fun getServicePendingIntent(intent: Intent): PendingIntent? {
        // 使用Intent里面的数据作为请求码(请求码不同PendingIntent也会变得不同)
        return PendingIntent.getService(this,
            intent.getIntExtra(STATUS_KEY, -1), intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * 获取指示点击通知后的意图的PendingIntent
     */
    private fun getActivityPendingIntent(): PendingIntent {
        return PendingIntent.getActivity(
            this, 0,
            StageActivity.getStartIntent(this, ! mediaPlayer.isPlaying),
            PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     * 设置控制栏上显示的歌曲信息
     * @param songName 歌曲名
     * @param singer 歌曲的艺术家
     */
    private fun setNotificationViewText(songName: String, singer: String) {
        smallRemoteViews.setTextViewText(R.id.tv_service_small_song_name, songName)
        smallRemoteViews.setTextViewText(R.id.tv_service_small_singer, singer)
        bigRemoteViews.setTextViewText(R.id.tv_service_song_name, songName)
        bigRemoteViews.setTextViewText(R.id.tv_service_singer, singer)
    }

    /**
     * 从歌曲实体类中读取信息，并更新到通知上，如果传入的歌曲为空，则清除通知上的信息
     */
    private fun resetNotification(song: Song?) {
        if (song != null) {
            setNotificationViewText(song.name ?: "", song.getAllArtistString())
            // 使用框架加载专辑位图
            Picasso.get().load(song.albumPicUrl + SMALL_BITMAP_PARAM)
                .error(R.drawable.ic_widget_album).into(
                    smallRemoteViews, R.id.iv_service_small_song_album,
                    PLAY_NOTIFICATION_ID, notification
                )
            Picasso.get().load(song.albumPicUrl + BIG_BITMAP_PARAM)
                .error(R.drawable.ic_widget_album).into(
                    bigRemoteViews, R.id.iv_service_album_image,
                    PLAY_NOTIFICATION_ID, notification
                )
        } else {
            setNotificationViewText("", "")
            smallRemoteViews.setImageViewResource(R.id.iv_service_small_song_album,
                R.drawable.ic_widget_album)
            bigRemoteViews.setImageViewResource(R.id.iv_service_album_image,
                R.drawable.ic_widget_album)
        }
    }

    /**
     * 根据歌曲播放状态的改变，改变播放/暂停按钮的外观，并改变PendingIntent
     * @param play 若需要转换到播放时按钮的状态，请传入true，否则传入false
     */
    private fun setButtonPlayOrPause(play: Boolean) {
        if (play) {
            smallRemoteViews.setImageViewResource(R.id.iv_service_small_play_or_pause,
                R.drawable.ic_widget_pause_circle)
            bigRemoteViews.setImageViewResource(R.id.iv_service_play_or_pause,
                R.drawable.ic_widget_pause_circle)
        } else {
            smallRemoteViews.setImageViewResource(R.id.iv_service_small_play_or_pause,
                R.drawable.ic_widget_play_circle)
            bigRemoteViews.setImageViewResource(R.id.iv_service_play_or_pause,
                R.drawable.ic_widget_play_circle)
        }
        notification.contentIntent = getActivityPendingIntent()
    }

    /**
     * 使用通知管理器刷新显示的通知
     */
    private fun updateNotification() {
        notificationManager.notify(PLAY_NOTIFICATION_ID, notification)
    }

    private fun initMediaPlayer() {
        // 设置播放的音频的属性：音频类型以及播放音频的原因（场景）
        mediaPlayer.setAudioAttributes(AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build())
        mediaPlayer.setOnPreparedListener {
            preparingSong = null
            // 如果没有缓存的歌曲
            if (cacheSong == null) {
                // 进入Prepared状态
                prepared = true
                // 通知监听器，歌曲加载完毕
                listenerPreparedUpdate()
            } else {
                val song = cacheSong
                // 缓存置空
                cacheSong = null
                binder.prepareMusic(song)
            }
        }
        mediaPlayer.setOnCompletionListener {
            setButtonPlayOrPause(false)
            updateNotification()
            listenerCompletionUpdate()
        }
        mediaPlayer.setOnErrorListener { _: MediaPlayer, what: Int, _: Int ->
            preparingSong = null
            resetMediaPlayer()
            setButtonPlayOrPause(false)
            updateNotification()
            listenerErrorUpdate(what)
            true
        }
    }

    private fun resetMediaPlayer() {
        prepared = false
        mediaPlayer.reset()
    }

    private fun loadPlayUrlSync(song: Song) {
        DataUtil.clientMusicApi.getSongsPlay(song.id.toString(),
            object : RequestCallBack<SongPlayJson> {
                override fun callback(data: SongPlayJson) {
                    if (data.data == null || data.data!![0].url.isNullOrEmpty()) {
                        listenerErrorUpdate(PLAY_URL_REQUEST_ERROR)
                        return
                    }
                    // 将播放地址缓存起来
                    playUrlMap[song.id] = data.data!![0].url!!
                    if (preparingSong == null) {
                        resetMediaPlayer()
                        preparingSong = song
                        mediaPlayer.setDataSource(playUrlMap[song.id])
                        mediaPlayer.prepareAsync()
                    }
                }

                override fun error(errorMsg: String) {
                    LogUtil.printToConsole(errorMsg)
                    listenerErrorUpdate(PLAY_URL_REQUEST_ERROR)
                }
            })
    }

    /**
     * 向所有注册的监听器广播失败信息
     */
    private fun listenerErrorUpdate(errorCode: Int) {
        remoteCallbackList.beginBroadcast()
        val count = remoteCallbackList.registeredCallbackCount
        for (index in 0 until count) {
            remoteCallbackList.getBroadcastItem(index).onError(errorCode)
        }
        remoteCallbackList.finishBroadcast()
    }

    /**
     * 向所有注册的监听器广播准备完毕的信息
     */
    private fun listenerPreparedUpdate() {
        remoteCallbackList.beginBroadcast()
        val count = remoteCallbackList.registeredCallbackCount
        for (index in 0 until count) {
            remoteCallbackList.getBroadcastItem(index).onPrepared()
        }
        remoteCallbackList.finishBroadcast()
    }

    /**
     * 向所有注册的监听器广播播放完毕的信息
     */
    private fun listenerCompletionUpdate() {
        remoteCallbackList.beginBroadcast()
        val count = remoteCallbackList.registeredCallbackCount
        for (index in 0 until count) {
            remoteCallbackList.getBroadcastItem(index).onCompletion()
        }
        remoteCallbackList.finishBroadcast()
    }

    /**
     * 向所有注册的监听器广播跳转下一曲的信息
     */
    private fun listenerSkipNextUpdate() {
        remoteCallbackList.beginBroadcast()
        val count = remoteCallbackList.registeredCallbackCount
        for (index in 0 until count) {
            remoteCallbackList.getBroadcastItem(index).skipNextSong()
        }
        remoteCallbackList.finishBroadcast()
    }

    /**
     * 向所有注册的监听器广播返回上一首的信息
     */
    private fun listenerReturnPreviousUpdate() {
        remoteCallbackList.beginBroadcast()
        val count = remoteCallbackList.registeredCallbackCount
        for (index in 0 until count) {
            remoteCallbackList.getBroadcastItem(index).returnPreviousSong()
        }
        remoteCallbackList.finishBroadcast()
    }

    private fun listenerStopSelfUpdate() {
        remoteCallbackList.beginBroadcast()
        val count = remoteCallbackList.registeredCallbackCount
        for (index in 0 until count) {
            remoteCallbackList.getBroadcastItem(index).stopSelf()
        }
        remoteCallbackList.finishBroadcast()
    }


    inner class MusicBinder: IBinderInterface.Stub() {
        override fun getDuration(): Int {
            // Error状态下不允许调用
            return mediaPlayer.duration
        }

        override fun setScreenOn(screenOn: Boolean) {
            // 允许Error状态下使用
            mediaPlayer.setScreenOnWhilePlaying(screenOn)
        }

        override fun getProgress(): Int {
            // Error状态下不允许调用
            return mediaPlayer.currentPosition
        }

        override fun prepared(): Boolean {
            return prepared
        }

        override fun reset() {
            resetMediaPlayer()
            // 重置通知信息
            resetNotification(null)
            setButtonPlayOrPause(false)
            updateNotification()
        }

        override fun registerPlayerStateListener(listener: IPlayerStateListener?) {
            remoteCallbackList.register(listener)
        }

        override fun playMusic() {
            if (prepared) {
                mediaPlayer.start()
                setButtonPlayOrPause(true)
                updateNotification()
            }
        }

        override fun pauseMusic() {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                setButtonPlayOrPause(false)
                updateNotification()
            }
        }

        override fun setLooping(looping: Boolean) {
            // Error状态下不允许调用
            mediaPlayer.isLooping = looping
        }

        override fun setProgress(progress: Int) {
            if (prepared) {
                mediaPlayer.seekTo(progress)
            }
        }

        override fun unregisterPlayerStateListener(listener: IPlayerStateListener?) {
            remoteCallbackList.unregister(listener)
        }

        override fun prepareMusic(song: Song?) {
            if (song == null) {
                resetMediaPlayer()
                listenerErrorUpdate(PREPARE_SONG_NULL)
                return
            }
            mainHandler.sendMessage(Message.obtain(mainHandler, RESET_NOTIFICATION, song))
            // 如果没有正在加载的歌曲
            if (preparingSong == null) {
                resetMediaPlayer()
                // 先尝试加载本地音乐
                when {
                    song.localUriString != null -> {
                        mediaPlayer.setDataSource(
                            this@MusicService,
                            Uri.parse(song.localUriString))
                        preparingSong = song
                        mediaPlayer.prepareAsync()
                    }
                    playUrlMap[song.id] != null -> {
                        mediaPlayer.setDataSource(playUrlMap[song.id])
                        preparingSong = song
                        mediaPlayer.prepareAsync()
                    }
                    else -> {
                        // 无法找到播放地址，则异步加载播放地址
                        loadPlayUrlSync(song)
                    }
                }
            } else {
                // 否则将歌曲缓存起来
                cacheSong = song
            }

        }
    }

    /**
     * 处理发送至主线程的刷新通知信息
     */
    private inner class Callback: Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            return when (msg.what) {
                RESET_NOTIFICATION -> {
                    resetNotification(msg.obj as Song)
                    updateNotification()
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    companion object {
        /**
         * 通知栏上通知的id
         */
        private const val PLAY_NOTIFICATION_ID = 2048

        /**
         * 通知渠道的id
         */
        private const val CHANNEL_SERVICE_ID = "com.gochiusa.musicapp.plus.service.MusicService"

        /**
         * 通知渠道的名字
         */
        private const val CHANNEL_NAME = "音乐播放"

        /**
         * 储存状态（意图）常量时应当使用的键值
         */
        private const val STATUS_KEY = "statusKey"

        /**
         * Message携带的状态，指明需要准备音乐
         */
        private const val RESET_NOTIFICATION = 33

        /**
         * 较小的通知需要的专辑图片的大小的参数
         */
        private const val SMALL_BITMAP_PARAM = "?param=180y180"

        /**
         * 较大的通知需要的专辑图片的大小的参数
         */
        private const val BIG_BITMAP_PARAM = "?param=360y360"

        /**
         * 启动服务的action
         */
        const val SERVICE_START_ACTION =
            "com.gochiusa.musicapp.plus.service.MusicService.SERVICE_START_ACTION"

        /**
         * 启动服务的category
         */
        const val SERVICE_START_CATEGORY =
            "com.gochiusa.musicapp.plus.service.MusicService.SERVICE_START_CATEGORY"

        /**
         * Intent携带的常量信息，这代表了通知栏的哪个功能性ImageView被点击
         */
        const val MUSIC_PLAY_OR_PAUSE = 10
        const val MUSIC_PREVIOUS = 20
        const val MUSIC_NEXT = 30
        const val CLOSE_APP = 40

        /**
         * 错误代码，无法获取播放地址
         */
        const val PLAY_URL_REQUEST_ERROR = 8
        /**
         * 错误代码，尝试播放的歌曲为null
         */
        const val PREPARE_SONG_NULL = 9
    }
}
