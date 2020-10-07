package com.gochiusa.musicapp.plus.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gochiusa.musicapp.plus.R

class FunctionButtonAdapter(val context: Context):
    RecyclerView.Adapter<FunctionButtonAdapter.ViewHolder>() {

    var onButtonClickListener: OnButtonClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.item_main_function_button, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int = 4

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (position) {
            0 -> {
                holder.imageView.setImageResource(R.drawable.ic_main_download)
                holder.textView.text = "下载管理"
                holder.itemView.setOnClickListener {
                    onButtonClickListener?.onClick(0)
                }
            }
            1 -> {
                holder.imageView.setImageResource(R.drawable.ic_main_local_music)
                holder.textView.text = "本地音乐"
                holder.itemView.setOnClickListener {
                    onButtonClickListener?.onClick(1)
                }
            }
            2 -> {
                holder.imageView.setImageResource(R.drawable.ic_main_user_playlist)
                holder.textView.text = "歌单收藏"
                holder.itemView.setOnClickListener {
                    onButtonClickListener?.onClick(2)
                }
            }
            3 -> {
                holder.imageView.setImageResource(R.drawable.ic_main_update_app)
                holder.textView.text = "检查更新"
                holder.itemView.setOnClickListener {
                    onButtonClickListener?.onClick(3)
                }
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_main_button_item)
        val textView: TextView = itemView.findViewById(R.id.tv_main_button_title)
    }

    interface OnButtonClickListener {
        fun onClick(position: Int)
    }
}