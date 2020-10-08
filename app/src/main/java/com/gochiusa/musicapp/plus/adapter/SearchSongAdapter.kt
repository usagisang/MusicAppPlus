package com.gochiusa.musicapp.plus.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.entity.Song
import com.gochiusa.musicapp.plus.util.StringContract

class SearchSongAdapter(list: MutableList<Song>, val context: Context) :
    FootViewAdapter<Song, SearchSongAdapter.ContentViewHolder>(list) {

    override fun createContentView(parent: ViewGroup?): NormalViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_adapter_music_card,
            parent, false)
        return ContentViewHolder(itemView)
    }

    override fun onBindContentViewHolder(holder: NormalViewHolder, position: Int) {
        if (holder is ContentViewHolder) {
            val song = getItem(position)
            holder.songNameTextView.text = song.name
            holder.singerNameTextView.text = song.artists?.joinToString(
                separator = StringContract.COMMA_SEPARATOR) {
                it.name ?: ""
            }
            holder.albumNameTextView.text = song.albumName
        }
    }

    class ContentViewHolder(itemView: View) :
        FootViewAdapter.NormalViewHolder(itemView, CONTENT_TYPE) {
        val songNameTextView: TextView = itemView.findViewById(R.id.tv_adapter_music_name)
        val singerNameTextView: TextView = itemView.findViewById(R.id.tv_adapter_singer_name)
        val albumNameTextView: TextView = itemView.findViewById(R.id.tv_adapter_album_name)
    }
}