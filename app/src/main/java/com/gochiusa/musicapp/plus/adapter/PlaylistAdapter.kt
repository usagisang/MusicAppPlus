package com.gochiusa.musicapp.plus.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.gochiusa.musicapp.library.util.ContextProvider
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.entity.EventMessage
import com.gochiusa.musicapp.plus.entity.Song
import com.gochiusa.musicapp.plus.util.PlaylistManager
import org.greenrobot.eventbus.EventBus

class PlaylistAdapter(private val songList: List<Song>)
    : RecyclerView.Adapter<PlaylistAdapter.ItemViewHolder>() {

    var itemDeleteListener: ItemDeleteListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.item_playlist, parent, false))
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.songNameView.text =
            "${songList[position].name} - ${songList[position].getAllArtistString()}"
        if (PlaylistManager.songPlayingPosition == position) {
            holder.songNameView.setTextColor(ResourcesCompat.getColor(
                ContextProvider.context.resources,
                R.color.colorPrimary,
                ContextProvider.context.theme)
            )
        } else {
            holder.songNameView.setTextColor(ResourcesCompat.getColor(
                ContextProvider.context.resources,
                R.color.colorBlack,
                ContextProvider.context.theme)
            )
        }
        holder.deleteButton.setOnClickListener {
            // 如果删除的位置正好是在播放的位置，则发出重新准备歌曲的通知
            val notify = (PlaylistManager.songPlayingPosition == position)
            PlaylistManager.removeSong(position)
            if (notify) {
                EventBus.getDefault().post(EventMessage(REPLAY_NOW_SONG))
            }
            itemDeleteListener?.onDelete()
            // 先通知子项已经被删除
            notifyItemRemoved(position)
            // 如果删除的是播放位置而且此时position已经越界
            if (notify && position > itemCount - 1) {
                // 通知全部位置刷新
                notifyDataSetChanged()
            } else {
                // 从被移除的位置开始通知，刷新后续子项的位置position，使之不错位
                notifyItemRangeChanged(position, itemCount - position)
            }
        }
        holder.itemView.setOnClickListener {
            // 如果点击的位置不在播放的位置
            if (PlaylistManager.songPlayingPosition != position) {
                // 先刷新两个位置的数据
                notifyItemChanged(PlaylistManager.songPlayingPosition)
                notifyItemChanged(position)
                // 更新索引
                PlaylistManager.songPlayingPosition = position
                EventBus.getDefault().post(EventMessage(REPLAY_NOW_SONG))
            }
        }
    }


    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songNameView: TextView = itemView.findViewById(R.id.tv_item_song_name)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btn_widget_delete)
    }

    interface ItemDeleteListener {
        fun onDelete()
    }

    companion object {
        const val REPLAY_NOW_SONG = 5
    }
}