package com.gochiusa.musicapp.plus.widget

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.adapter.PlaylistAdapter
import com.gochiusa.musicapp.plus.util.PlaylistManager


class PopupPlaylist(private val window: Window, contentView: View, width: Int, height: Int) {

    private val popupWindow = PopupWindow(contentView, width, height)

    var clearButtonClickListener: View.OnClickListener? = null
    /**
     * 以下是作为播放列表ContentView内的控件
     */
    private val clearButton: ImageButton = contentView.findViewById(R.id.btn_list_clear_all)
    private val songCountTextView: TextView = contentView.findViewById(R.id.tv_list_song_count)

    private val songRecyclerView: RecyclerView = contentView.findViewById(R.id.rv_list_play_content)
    private val playlistAdapter = PlaylistAdapter(PlaylistManager.getPlaylist())

    val isShowing: Boolean
        get() = popupWindow.isShowing


    init {
        popupWindow.isTouchable = true
        popupWindow.isOutsideTouchable = true
        // 让PopupWindow获得焦点，这会屏蔽外部点击事件
        popupWindow.isFocusable = true
        // 设置弹出的平移动画
        popupWindow.animationStyle = R.style.PopupAnimation
        // 创建、设置圆角背景
        val background = GradientDrawable()
        background.setColor(Color.WHITE)
        background.cornerRadius = 50F
        popupWindow.setBackgroundDrawable(background)
        // 消失时透明度恢复
        popupWindow.setOnDismissListener {
            setAlpha(1F)
        }
        // 初始化RecyclerView
        songRecyclerView.layoutManager = LinearLayoutManager(contentView.context)
        songRecyclerView.addItemDecoration(DefaultDecoration
            (10, 35, 10, 35))
        songRecyclerView.adapter = playlistAdapter
        // 设置子项被删除后的监听器
        playlistAdapter.itemDeleteListener = object : PlaylistAdapter.ItemDeleteListener {
            override fun onDelete() {
                songCountTextView.text = playlistAdapter.itemCount.toString()
            }
        }
        // 绑定清空按钮的点击事件
        clearButton.setOnClickListener {
            clearButtonClickListener?.onClick(it)
            songCountTextView.text = "0"
        }
    }

    fun show(parent: View, gravity: Int, x: Int, y: Int) {
        setAlpha(0.3F)
        popupWindow.showAtLocation(parent, gravity, x, y)
        playlistAdapter.notifyDataSetChanged()
        songCountTextView.text = playlistAdapter.itemCount.toString()
    }

    fun dismiss() {
        popupWindow.dismiss()
    }

    /**
     * 辅助方法，设置窗口的透明度
     */
    private fun setAlpha(alpha: Float) {
        val layoutParams: WindowManager.LayoutParams = window.attributes
        layoutParams.alpha = alpha
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = layoutParams
    }
}