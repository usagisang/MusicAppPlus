package com.gochiusa.musicapp.plus.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.entity.UserPlaylist
import com.gochiusa.musicapp.plus.widget.RoundImageView
import com.squareup.picasso.Picasso

class UserPlaylistAdapter(itemList: MutableList<UserPlaylist>)
    : FootViewAdapter<UserPlaylist, UserPlaylistAdapter.ViewHolder>(itemList) {


    override fun createContentView(parent: ViewGroup): NormalViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.item_child_user_list, parent, false), CONTENT_TYPE)
    }

    override fun createFootView(parent: ViewGroup): NormalViewHolder {
        // 覆盖默认的尾布局，使用白色尾布局
        footView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_empty_foot_view, parent, false)
        return FootViewHolder(footView)
    }

    override fun onBindContentViewHolder(holder: NormalViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.nameTextView.text = getItem(position).name
            holder.countTextView.text = getItem(position).songCount.toString()
            Picasso.get().load(getItem(position).coverImgUrl).fit()
                .error(R.drawable.ic_widget_album).into(holder.coverImageView)
        }
    }


    class ViewHolder(itemView: View, type: Int) : FootViewAdapter.NormalViewHolder(itemView, type) {
        val nameTextView: TextView = itemView.findViewById(R.id.tv_child_user_list_name)
        val countTextView: TextView = itemView.findViewById(
            R.id.tv_child_user_list_song_count)
        val coverImageView: RoundImageView = itemView.findViewById(
            R.id.iv_child_user_list_cover)
    }
}