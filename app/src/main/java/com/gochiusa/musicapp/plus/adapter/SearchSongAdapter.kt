package com.gochiusa.musicapp.plus.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.entity.EventMessage
import com.gochiusa.musicapp.plus.entity.Song
import com.gochiusa.musicapp.plus.tasks.main.MainActivity.Companion.PREPARE_MUSIC
import com.gochiusa.musicapp.plus.util.PlaylistManager
import org.greenrobot.eventbus.EventBus

class SearchSongAdapter(list: MutableList<Song>) :
    FootViewAdapter<Song, SearchSongAdapter.ContentViewHolder>(list) {

    override fun createContentView(parent: ViewGroup): NormalViewHolder {
        return ContentViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.item_adapter_music_card, parent, false))
    }

    override fun onBindContentViewHolder(holder: NormalViewHolder, position: Int) {
        if (holder is ContentViewHolder) {
            val song = getItem(position)
            holder.songNameTextView.text = song.name
            holder.singerNameTextView.text = song.getAllArtistString()
            holder.albumNameTextView.text = song.albumName
            holder.itemView.setOnClickListener {
                PlaylistManager.removeAllSong()
                PlaylistManager.addAllSongToPlaylist(getReadOnlyList())
                PlaylistManager.songPlayingPosition = position
                EventBus.getDefault().post(EventMessage(PREPARE_MUSIC))
            }
        }
    }

    class ContentViewHolder(itemView: View) :
        FootViewAdapter.NormalViewHolder(itemView, CONTENT_TYPE) {
        val songNameTextView: TextView = itemView.findViewById(R.id.tv_adapter_music_name)
        val singerNameTextView: TextView = itemView.findViewById(R.id.tv_adapter_singer_name)
        val albumNameTextView: TextView = itemView.findViewById(R.id.tv_adapter_album_name)
    }
}