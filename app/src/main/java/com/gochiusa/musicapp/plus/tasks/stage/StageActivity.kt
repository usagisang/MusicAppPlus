package com.gochiusa.musicapp.plus.tasks.stage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.adapter.MusicControlAdapter
import com.gochiusa.musicapp.plus.widget.LyricView
import com.gochiusa.musicapp.plus.widget.MusicControlView
import com.gochiusa.musicapp.plus.widget.MusicProgressBar
import com.gochiusa.musicapp.plus.widget.RoundImageView

class StageActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var songNameTextView: TextView
    private lateinit var artistTextView: TextView
    private lateinit var roundImageView: RoundImageView
    private lateinit var musicProgressBar: MusicProgressBar
    private lateinit var musicControlView: MusicControlView
    private lateinit var lyricView: LyricView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stage)
        initChildView()
    }

    private fun initChildView() {
        backButton = findViewById(R.id.btn_stage_back)
        songNameTextView = findViewById(R.id.tv_stage_music_name)
        artistTextView = findViewById(R.id.tv_stage_music_artist)
        roundImageView = findViewById(R.id.iv_stage_round_album)
        musicProgressBar = findViewById(R.id.widget_stage_music_progress_bar)
        musicControlView = findViewById(R.id.widget_stage_music_control)
        lyricView = findViewById(R.id.lyric_view)
        // 设置适配器
        musicControlView.adapter = MusicControlAdapter(this)
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
}